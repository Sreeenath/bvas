package com.bvas.bvas.config;

import com.bvas.bvas.model.District;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.UserRole;
import com.bvas.bvas.repository.DistrictRepository;
import com.bvas.bvas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); 
            admin.setFullName("HQ Administrator");
            admin.setEmail("admin@bvas.gov.in");
            admin.setRole(UserRole.HQ_ADMIN);
            admin.setIsActive(true);
            admin.setIsApproved(true);
            userRepository.save(admin);
            System.out.println("Default admin created: username=admin, password=admin123");
        }

        // Create districts if not exists
        if (districtRepository.count() == 0) {
            String[][] districts = {
                {"ALM", "Almora"},
                {"BAG", "Bageshwar"},
                {"CHA", "Chamoli"},
                {"CHP", "Champawat"},
                {"DEH", "Dehradun"},
                {"HAR", "Haridwar"},
                {"NAI", "Nainital"},
                {"PAU", "Pauri Garhwal"},
                {"PIT", "Pithoragarh"},
                {"RUD", "Rudraprayag"},
                {"TEH", "Tehri Garhwal"},
                {"UDH", "Udham Singh Nagar"},
                {"UTT", "Uttarkashi"}
            };

            for (String[] district : districts) {
                District d = new District();
                d.setCode(district[0]);
                d.setName(district[1]);
                d.setIsActive(true);
                districtRepository.save(d);
            }
            System.out.println("13 districts created");
        }
    }
}