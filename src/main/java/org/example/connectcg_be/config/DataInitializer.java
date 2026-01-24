package org.example.connectcg_be.config;

import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem đã có tài khoản admin chưa
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin2@gmail.com");
            // Mật khẩu là 'admin'
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setIsLocked(false);
            admin.setIsDeleted(false);
            admin.setCreatedAt(Instant.now());

            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản Admin mặc định: admin / admin");
        } else {
            System.out.println(">>> Tài khoản Admin đã tồn tại.");
        }
    }
}
