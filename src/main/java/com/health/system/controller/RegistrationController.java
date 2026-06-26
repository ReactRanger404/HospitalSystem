package com.health.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.common.result.PageResult;
import com.health.system.common.result.Result;
import com.health.system.entity.Appointment;
import com.health.system.entity.Department;
import com.health.system.entity.DoctorSchedule;
import com.health.system.entity.User;
import com.health.system.service.DepartmentService;
import com.health.system.service.RegistrationService;
import com.health.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 门诊挂号与预约控制器
 * 科室查询、医生排班、预约挂号、签到退号
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
@Tag(name = "门诊挂号与预约", description = "科室查询、医生排班、预约挂号、签到退号")
public class RegistrationController {

    private final DepartmentService departmentService;
    private final UserService userService;
    private final RegistrationService registrationService;

    // ====== 科室 ======
    @GetMapping("/departments")
    @Operation(summary = "获取所有科室列表")
    public Result<List<Department>> getDepartments(@RequestParam(required = false) String category) {
        List<Department> list = category != null
                ? departmentService.getByCategory(category)
                : departmentService.getActiveDepartments();
        return Result.success(list);
    }

    @GetMapping("/departments/{id}/doctors")
    @Operation(summary = "获取科室下的医生列表")
    public Result<List<User>> getDoctors(@PathVariable Long id) {
        return Result.success(userService.getDoctorsByDepartment(id));
    }

    // ====== 医生排班（仅管理员和护士长可管理） ======
    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('admin', 'nurse')")
    @Operation(summary = "创建医生排班")
    public Result<DoctorSchedule> createSchedule(@RequestBody DoctorSchedule schedule) {
        return Result.success(registrationService.createSchedule(schedule));
    }

    @GetMapping("/schedules")
    @Operation(summary = "查询排班列表")
    public Result<PageResult<DoctorSchedule>> getSchedules(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate queryDate = date != null ? LocalDate.parse(date) : null;
        IPage<DoctorSchedule> result = registrationService.getSchedules(departmentId, doctorId, queryDate, page, size);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/schedules/available")
    @Operation(summary = "查询可预约排班")
    public Result<List<DoctorSchedule>> getAvailableSchedules(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId) {
        return Result.success(registrationService.getAvailableSchedules(departmentId, doctorId));
    }

    // ====== 预约挂号 ======
    @PostMapping("/appointments")
    @Operation(summary = "创建预约挂号")
    public Result<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return Result.success(registrationService.createAppointment(appointment));
    }

    @GetMapping("/appointments")
    @Operation(summary = "查询我的预约记录（患者端）")
    public Result<PageResult<Appointment>> getMyAppointments(
            @RequestParam Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<Appointment> result = registrationService.getPatientAppointments(patientId, status, page, size);
        return Result.success(PageResult.of(result));
    }

    @PutMapping("/appointments/{id}/checkin")
    @Operation(summary = "患者签到到诊")
    public Result<Appointment> checkin(@PathVariable Long id) {
        return Result.success(registrationService.checkin(id));
    }

    @PutMapping("/appointments/{id}/cancel")
    @Operation(summary = "取消预约/退号")
    public Result<Appointment> cancel(@PathVariable Long id,
                                      @RequestParam(required = false) String reason) {
        return Result.success(registrationService.cancel(id, reason));
    }

    @GetMapping("/appointments/doctor/today")
    @Operation(summary = "医生查看今日预约患者")
    public Result<PageResult<Appointment>> getTodayAppointments(
            @RequestParam Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<Appointment> result = registrationService.getTodayAppointmentsByDoctor(doctorId, status, page, size);
        return Result.success(PageResult.of(result));
    }
}
