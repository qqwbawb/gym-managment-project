package com.xq.config.springSecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置（Spring Boot 3 / Spring Security 6）
 *
 * 相对 Boot 2.7 的改动：
 * 1. WebSecurityConfigurerAdapter 在 Security 6 中已移除，改为注册 SecurityFilterChain Bean；
 * 2. @EnableGlobalMethodSecurity 被 @EnableMethodSecurity 取代（@PreAuthorize 用法不变）；
 * 3. antMatchers 被 requestMatchers 取代；
 * 4. 认证来源由 AuthenticationManagerBuilder 改为 AuthenticationConfiguration，
 *    由 Spring Boot 自动装配我们提供的 CustomizedUserDetailService（UserDetailsService）与 PasswordEncoder。
 */
@EnableWebSecurity
@Configuration
@EnableMethodSecurity // 开启 @PreAuthorize 等注解的方法级权限控制
public class SpringSecurityConfig {

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Autowired
    private CustomerAccessDeniedHandler customerAccessDeniedHandler;

    @Autowired
    private CheckTokenFilter checkTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 解决跨域问题
                .cors(cors -> {})
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // 关闭 csrf：本项目使用 JWT（无 Cookie 会话）认证，不需要 CSRF token
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 登录请求、验证码回显请求直接放行
                        .requestMatchers("/api/login/image", "/api/login/login").permitAll()
                        // springdoc / swagger-ui 接口文档放行
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(loginFailureHandler)
                        .accessDeniedHandler(customerAccessDeniedHandler))
                // 基于 Token 的无状态认证：不创建 HttpSession 保存认证信息。
                // 注意：登录验证码仍依赖 HttpSession 暂存，这里仅控制 Spring Security 自身的会话行为。
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // 设置 Token 校验过滤器
        http.addFilterBefore(checkTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 密码加密器的配置
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 暴露 AuthenticationManager 供登录接口使用
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
