package com.health.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.entity.Appointment;
import com.health.system.entity.DoctorSchedule;

import java.time.LocalDate;

/**
 * 门诊挂号与预约服务接口
 * 管理医生排班、预约挂号、分诊到诊、退号取消
 *
 * @author health-system
 */
public interface RegistrationService {

    // ====== 医生排班 ======
    DoctorSchedule createSchedule(DoctorSchedule schedule);
    IPage<DoctorSchedule> getSchedules(Long departmentId, Long doctorId, LocalDate date, int page, int size);
    java.util.List<DoctorSchedule> getAvailableSchedules(Long departmentId, Long doctorId);

    // ====== 预约挂号 ======
    Appointment createAppointment(Appointment appointment);
    Appointment checkin(Long appointmentId);
    Appointment cancel(Long appointmentId, String reason);
    IPage<Appointment> getPatientAppointments(Long patientId, String status, int page, int size);
    IPage<Appointment> getTodayAppointmentsByDoctor(Long doctorId, String status, int page, int size);
}
