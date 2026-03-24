package com.example.ktgk.service;

import com.example.ktgk.model.Patient;
import com.example.ktgk.model.Role;
import com.example.ktgk.repository.PatientRepository;
import com.example.ktgk.repository.RoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(PatientRepository patientRepository, RoleRepository roleRepository) {
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email không tìm thấy từ Google account");
        }

        Patient patient = patientRepository.findByEmail(email)
                .orElseGet(() -> {
                    Patient newPatient = new Patient();
                    newPatient.setUsername(email);
                    newPatient.setEmail(email);
                    newPatient.setPassword("G¬uE!*%" + System.currentTimeMillis()); // password tùy ý vì không dùng
                    Role role = roleRepository.findByName("PATIENT")
                            .orElseGet(() -> roleRepository.save(new Role("PATIENT")));
                    newPatient.getRoles().add(role);
                    return patientRepository.save(newPatient);
                });

        Set<GrantedAuthority> authorities = patient.getRoles().stream()
                .map(Role::getName)
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                .collect(Collectors.toSet());

        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
