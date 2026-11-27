package com.example.helpdesk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile("dev")
public class DevUsersConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder
    ) {

        UserDetails junior = User.builder()
                .username("junior")
                .password(passwordEncoder.encode("junior123"))
                .roles("JUNIOR")
                .build();

        UserDetails senior = User.builder()
                .username("senior")
                .password(passwordEncoder.encode("senior123"))
                .roles("SENIOR")
                .build();

        return new InMemoryUserDetailsManager(
                junior,
                senior
        );
    }
}
