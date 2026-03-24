package com.example.ktgk;

import com.example.ktgk.model.*;
import com.example.ktgk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(RoleRepository roleRepository,
                           PatientRepository patientRepository,
                           DepartmentRepository departmentRepository,
                           DoctorRepository doctorRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            Role patientRole = roleRepository.findByName("PATIENT").orElseGet(() -> roleRepository.save(new Role("PATIENT")));

            if (patientRepository.findByUsername("admin").isEmpty()) {
                Patient admin = new Patient("admin", passwordEncoder.encode("admin123"), "admin@example.com");
                admin.getRoles().add(adminRole);
                patientRepository.save(admin);
            }

            if (departmentRepository.count() == 0) {
                Department d1 = departmentRepository.save(new Department("Cardiology"));
                Department d2 = departmentRepository.save(new Department("Neurology"));
                Department d3 = departmentRepository.save(new Department("Pediatrics"));

                doctorRepository.save(new Doctor("Dr. Anh", "Heart Specialist", "https://via.placeholder.com/80?text=Anh", d1));
                doctorRepository.save(new Doctor("Dr. Bình", "Brain Specialist", "https://via.placeholder.com/80?text=Binh", d2));
                doctorRepository.save(new Doctor("Dr. Cúc", "Child Specialist", "https://via.placeholder.com/80?text=Cuc", d3));
                doctorRepository.save(new Doctor("Dr. Dũng", "General", "https://via.placeholder.com/80?text=Dung", d1));
                doctorRepository.save(new Doctor("Dr. Em", "Dermatology", "https://via.placeholder.com/80?text=Em", d2));
                doctorRepository.save(new Doctor("Dr. Hà", "Orthopedics", "https://via.placeholder.com/80?text=Ha", d3));
            }
        };
    }
}
