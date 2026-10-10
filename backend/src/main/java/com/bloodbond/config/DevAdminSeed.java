package com.bloodbond.config;

import com.bloodbond.user.Role;
import com.bloodbond.user.User;
import com.bloodbond.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevAdminSeed {
    @Bean
    ApplicationRunner seedAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${bloodbond.admin.email:admin@bloodbond.local}") String email,
                                @Value("${bloodbond.admin.password:BloodBondDevAdmin1!}") String password) {
        return args -> userRepository.findByEmail(email.trim().toLowerCase()).orElseGet(() ->
            userRepository.save(new User(
                "BloodBond Admin",
                email.trim().toLowerCase(),
                passwordEncoder.encode(password),
                Role.ADMIN
            ))
        );
    }
}
