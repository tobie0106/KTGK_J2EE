package com.example.ktgk.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    private String image;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public Doctor() {}

    public Doctor(String name, String specialty, String image, Department department) {
        this.name = name;
        this.specialty = specialty;
        this.image = image;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(name, doctor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}