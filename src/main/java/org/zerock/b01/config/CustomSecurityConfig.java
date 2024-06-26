package org.zerock.b01.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.zerock.b01.security.handler.Custom403Handler;
import org.zerock.b01.security.handler.CustomerSocialLoginSuccessHandler;


import javax.sql.DataSource;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableMethodSecurity
public class CustomSecurityConfig {

    // Remember-me 서비스를 위해서 DataSource와 CustomUserDetailsService를 주입
    private final DataSource dataSource;

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("-------------------configure------------------------");

        // 사용자 로그인 페이지 설정
        http.formLogin(form -> {
            form.loginPage("/member/login");
        });
        // CSRF설정..
        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());

        // remember-me 설정
        http.rememberMe(httpSecurityRememberMeConfigurer -> {
           httpSecurityRememberMeConfigurer.key("12345678")
                   .tokenRepository(persistentTokenRepository())
                   .userDetailsService(userDetailsService)  // PasswordEncoder에 의한 순환 구조가 발생할 수 있음...
                   .tokenValiditySeconds(60*60*24*30);
        });

        // exceptionHandler 설정
        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler());
        });

        http.oauth2Login(httpSecurityOauth2LoginConfigurer -> {
            httpSecurityOauth2LoginConfigurer.loginPage("/member/login");
            httpSecurityOauth2LoginConfigurer.successHandler(authenticationSuccessHandler());
        });

        return http.build();
    }

    private final PasswordEncoder passwordEncoder;    // 순환 참조를 방지하기 위해 PasswordEncoder 삭제
    // PasswordEncoderConfig 에 있는 passwordEncoder 를 불러옴

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomerSocialLoginSuccessHandler(passwordEncoder);
    }

    // AccessDeniedHandler 빈등록...
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new Custom403Handler();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("-------------------- web configure  -------------------");
        // 정적 리소스 필터링 제외~~~
        return (web -> web.ignoring().requestMatchers(PathRequest.toStaticResources()
                .atCommonLocations()));
    }
    
    // 패스워드 암호화 처리하는 객체  -- 순환참조 문제로 인해서 다른 별개의 Configuration을 생성하여 처리...
    // 순환 구조의 발생 원인은 userDetailsService에서 의존성 주입을 한 PasswordEncoder를 설정한 Configuration에서
    // 다시 불러오는 구조가 되어 순환 구조가 됨.
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    //PersistentTokenRepository
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

}
