package com.example.ktgk.repository;

import com.example.ktgk.model.Appointment;
import com.example.ktgk.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
}