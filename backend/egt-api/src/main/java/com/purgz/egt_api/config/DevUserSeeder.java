package com.purgz.egt_api.config;

import com.purgz.egt_api.model.Role;
import com.purgz.egt_api.model.User;
import com.purgz.egt_api.repository.RoleRepository;
import com.purgz.egt_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevUserSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@dev.local";
    private static final String USER_EMAIL = "user@dev.local";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_USER_PASSWORD = "user123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        final String adminPassword = environment.getProperty("DEV_ADMIN_PASSWORD", DEFAULT_ADMIN_PASSWORD);
        final String userPassword = environment.getProperty("DEV_USER_PASSWORD", DEFAULT_USER_PASSWORD);

        seedUser(ADMIN_EMAIL, adminPassword, Set.of(
                roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow(),
                roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow()));
        seedUser(USER_EMAIL, userPassword, Set.of(
                roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow()));
    }

    private void seedUser(final String email, final String password, final Set<Role> roles) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        userRepository.save(user);
    }
}
