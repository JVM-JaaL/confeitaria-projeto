package com.confeitaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Configura toda a segurança da aplicação: quem pode acessar o quê e como fazer login/logout.
// É usado indiretamente por todos os controllers — qualquer requisição a /admin/** passa por aqui primeiro.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Usuário e senha lidos do application.properties; padrão "admin" / "confeitaria123"
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:confeitaria123}")
    private String adminPassword;

    // Encoder BCrypt: transforma a senha em hash antes de guardar na memória
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cria o único usuário administrador em memória (sem banco de dados de usuários)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.builder()
            .username(adminUsername)
            .password(encoder.encode(adminPassword))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    // Define as regras de acesso:
    // - /admin/login e /admin/logout são públicos
    // - qualquer outra rota /admin/** exige role ADMIN
    // - /h2-console/** exige ADMIN (banco de dados em desenvolvimento)
    // - todo o resto (site público) é liberado
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/logout").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            // Formulário de login personalizado em admin/login.html
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            // Logout redireciona para o site público
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // CSRF desabilitado para o console H2 (ferramenta de dev)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            // Permite iframes do mesmo domínio (necessário para o console H2 funcionar no browser)
            .headers(headers -> headers
                .frameOptions(f -> f.sameOrigin())
            );
        return http.build();
    }
}
