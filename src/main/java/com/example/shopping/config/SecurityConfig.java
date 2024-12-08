package com.example.shopping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private AuthSucessHandlerImpl authSucessHandlerImpl;

    @Autowired
    @Lazy
    private AuthFailureHandlerImpl authFailureHandlerImpl;

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean 
    UserDetailsService userDetailsService(){
        return new UserDetailsServiceImpl();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/user/**").hasAnyRole("USER", "STAFF")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/**").permitAll())
                .formLogin(form -> form.loginPage("/signin")
                        .loginProcessingUrl("/login")
                        .failureHandler(authFailureHandlerImpl)
                        .successHandler(authSucessHandlerImpl))
                .logout(logout -> logout.permitAll())
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/403"));
        return http.build();
    }
}
