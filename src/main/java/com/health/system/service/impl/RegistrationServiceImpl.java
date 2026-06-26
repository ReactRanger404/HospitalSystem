package com.health.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.system.common.exception.BusinessException;
import com.health.system.entity.Appointment;
import com.health.system.entity.DoctorSchedule;
import com.health.system.enums.AppointmentStatus;
import com.health.system.mapper.AppointmentMapper;
import com.health.system.mapper.DoctorScheduleMapper;
import com.health.system.service.MessageProducerService;
import com.health.system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 门诊挂号与预约服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final DoctorScheduleMapper scheduleMapper;
    private final AppointmentMapper appointmentMapper;
    private final MessageProducerService messageProducer;

    // ====== 医生排班管理 ======

    @Override
    @Transactional
    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        // 检查是否已存在相同排班
        Long count = scheduleMapper.selectCount(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, schedule.getDoctorId())
                .eq(DoctorSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(DoctorSchedule::getTimeSlot, schedule.getTimeSlot())
                .eq(DoctorSchedule::getIsActive, true));
        if (count > 0) {
            throw new BusinessException("该医生在此日期时段已有排班");
        }

        schedule.setBookedCount(0);
        schedule.setIsActive(true);
        scheduleMapper.insert(schedule);
        log.info("创建排班成功: id={}, doctorId={}, date={}, slot={}",
                schedule.getId(), schedule.getDoctorId(), schedule.getScheduleDate(), schedule.getTimeSlot());
        return schedule;
    }

    @Override
    public IPage<DoctorSchedule> getSchedules(Long departmentId, Long doctorId, LocalDate date, int page, int size) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getIsActive, true);
        if (departmentId != null) {
            wrapper.eq(DoctorSchedule::getDepartmentId, departmentId);
        }
        if (doctorId != null) {
            wrapper.eq(DoctorSchedule::getDoctorId, doctorId);
        }
        if (date != null) {
            wrapper.eq(DoctorSchedule::getScheduleDate, date);
        } else {
            wrapper.ge(DoctorSchedule::getScheduleDate, LocalDate.now());
        }
        wrapper.orderByAsc(DoctorSchedule::getScheduleDate, DoctorSchedule::getTimeSlot);

        return scheduleMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<DoctorSchedule> getAvailableSchedules(Long departmentId, Long doctorId) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getIsActive, true)
                .ge(DoctorSchedule::getScheduleDate, LocalDate.now());
        if (departmentId != null) {
            wrapper.eq(DoctorSchedule::getDepartmentId, departmentId);
        }
        if (doctorId != null) {
            wrapper.eq(DoctorSchedule::getDoctorId, doctorId);
        }
        return scheduleMapper.selectList(wrapper);
    }

    // ====== 预约挂号 ======

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 验证排班
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule == null || !schedule.getIsActive()) {
            throw new BusinessException("排班不存在或已停用");
        }
        if (schedule.getBookedCount() >= schedule.getMaxPatients()) {
            throw new BusinessException("该时段号源已满");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("不能预约过去的日期");
        }

        // 设置排队序号
        appointment.setQueueNumber(schedule.getBookedCount() + 1);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentMapper.insert(appointment);

        // 更新排班预约数
        schedule.setBookedCount(schedule.getBookedCount() + 1);
        scheduleMapper.updateById(schedule);

        log.info("预约成功: id={}, patientId={}, doctorId={}, queueNo={}",
                appointment.getId(), appointment.getPatientId(), appointment.getDoctorId(), appointment.getQueueNumber());

        // 发送Kafka异步通知（用于短信/微信/WebSocket推送）
        messageProducer.sendAppointmentNotification(
                appointment.getPatientId(), appointment.getDoctorId(),
                "患者", "医生",
                appointment.getAppointmentDate().toString(), appointment.getTimeSlot()
        );
        return appointment;
    }

    @Override
    @Transactional
    public Appointment checkin(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("当前状态不允许签到: " + appointment.getStatus().getLabel());
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentMapper.updateById(appointment);
        log.info("签到成功: appointmentId={}, patientId={}", appointmentId, appointment.getPatientId());
        return appointment;
    }

    @Override
    @Transactional
    public Appointment cancel(Long appointmentId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.REFUNDED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("当前状态不允许取消: " + appointment.getStatus().getLabel());
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(oldStatus == AppointmentStatus.PENDING
                ? AppointmentStatus.CANCELLED : AppointmentStatus.REFUNDED);
        appointment.setCancelReason(reason);
        appointmentMapper.updateById(appointment);

        // 释放号源
        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null && schedule.getBookedCount() > 0) {
            schedule.setBookedCount(schedule.getBookedCount() - 1);
            scheduleMapper.updateById(schedule);
        }

        log.info("取消/退号成功: appointmentId={}, oldStatus={}, reason={}", appointmentId, oldStatus, reason);
        return appointment;
    }

    @Override
    public IPage<Appointment> getPatientAppointments(Long patientId, String status, int page, int size) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId)
                .orderByDesc(Appointment::getAppointmentDate);
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        return appointmentMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<Appointment> getTodayAppointmentsByDoctor(Long doctorId, String status, int page, int size) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctorId)
                .eq(Appointment::getAppointmentDate, LocalDate.now())
                .orderByAsc(Appointment::getQueueNumber);
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        return appointmentMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
