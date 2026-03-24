package com.example.ktgk.controller;

import com.example.ktgk.model.Appointment;
import com.example.ktgk.model.Department;
import com.example.ktgk.model.Doctor;
import com.example.ktgk.model.Patient;
import com.example.ktgk.model.Role;
import com.example.ktgk.repository.AppointmentRepository;
import com.example.ktgk.repository.DepartmentRepository;
import com.example.ktgk.repository.DoctorRepository;
import com.example.ktgk.repository.PatientRepository;
import com.example.ktgk.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
@Controller
public class AppController {
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AppController(DoctorRepository doctorRepository,
                         DepartmentRepository departmentRepository,
                         PatientRepository patientRepository,
                         RoleRepository roleRepository,
                         AppointmentRepository appointmentRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword) {
        PageRequest pageable = PageRequest.of(page, 5);
        Page<Doctor> pageDoctors;
        if (keyword != null && !keyword.isBlank()) {
            pageDoctors = doctorRepository.findByNameContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            pageDoctors = doctorRepository.findAll(pageable);
        }

        model.addAttribute("doctors", pageDoctors.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageDoctors.getTotalPages());
        return "home";
    }

    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute Patient patient, Model model) {
        if (patientRepository.findByUsername(patient.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        Role patientRole = roleRepository.findByName("PATIENT").orElseGet(() -> roleRepository.save(new Role("PATIENT")));
        patient.getRoles().add(patientRole);
        patientRepository.save(patient);
        return "redirect:/login?registered";
    }

    @PostMapping("/appointments/book/{doctorId}")
    public String bookAppointment(@PathVariable Long doctorId,
                                  @RequestParam("date") String date,
                                  Principal principal,
                                  Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Patient patient = patientRepository.findByUsername(principal.getName()).orElseThrow();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();

        Appointment appointment = new Appointment(patient, doctor, LocalDate.parse(date));
        appointmentRepository.save(appointment);

        return "redirect:/appointments/my";
    }

    @GetMapping("/appointments/my")
    public String myAppointments(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Patient patient = patientRepository.findByUsername(principal.getName()).orElseThrow();
        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
        model.addAttribute("appointments", appointments);
        return "my_appointments";
    }

    @GetMapping("/admin/doctors")
    public String adminDoctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "admin_doctors";
    }

    @GetMapping("/admin/doctors/create")
    public String createDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin_doctor_form";
    }

    @PostMapping("/admin/doctors/create")
public String createDoctorSubmit(@ModelAttribute Doctor doctor,
                                 @RequestParam("imageFile") MultipartFile file) throws IOException {

    if (!file.isEmpty()) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("src/main/resources/static/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        doctor.setImage(fileName);
    }

    if (doctor.getDepartment() != null && doctor.getDepartment().getId() != null) {
        Department dept = departmentRepository.findById(doctor.getDepartment().getId()).orElse(null);
        doctor.setDepartment(dept);
    }

    doctorRepository.save(doctor);
    return "redirect:/admin/doctors";
}

    @GetMapping("/admin/doctors/edit/{id}")
    public String editDoctorForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow();
        model.addAttribute("doctor", doctor);
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin_doctor_form";
    }

    @PostMapping("/admin/doctors/edit/{id}")
public String editDoctorSubmit(@PathVariable Long id,
                               @ModelAttribute Doctor doctorForm,
                               @RequestParam("imageFile") MultipartFile file) throws IOException {

    Doctor doctor = doctorRepository.findById(id).orElseThrow();

    doctor.setName(doctorForm.getName());
    doctor.setSpecialty(doctorForm.getSpecialty());

    if (!file.isEmpty()) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("src/main/resources/static/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        doctor.setImage(fileName);
    }

    if (doctorForm.getDepartment() != null && doctorForm.getDepartment().getId() != null) {
        Department dept = departmentRepository.findById(doctorForm.getDepartment().getId()).orElse(null);
        doctor.setDepartment(dept);
    }

    doctorRepository.save(doctor);
    return "redirect:/admin/doctors";
}

    @GetMapping("/admin/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorRepository.deleteById(id);
        return "redirect:/admin/doctors";
    }

    @GetMapping("/api/doctors/search")
    @ResponseBody
    public List<Doctor> apiDoctorSearch(@RequestParam(value = "keyword", defaultValue = "") String keyword) {
        return doctorRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(0, 50)).getContent();
    }

    @GetMapping("/enroll/example")
    public String enrollExample() {
        return "enroll";
    }
}
