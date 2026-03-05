package com.company.asset.security;

import com.company.asset.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ 핵심:
     * - React 정적 파일(SPA) 접근은 permitAll
     * - API는 /api/auth/**만 permitAll, 나머지는 인증 필요
     * - JWT Filter를 UsernamePasswordAuthenticationFilter 앞에 추가
     * - Session은 Stateless
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API + JWT 기본 세팅
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS (로컬 개발 중 React 5173도 허용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 권한 규칙
                .authorizeHttpRequests(auth -> auth
                        // ✅ SPA 정적 리소스/진입점 허용 (A 방식: Spring이 React 서빙)
                        .requestMatchers(
                                "/", "/index.html",
                                "/assets/**",          // Vite build 결과 js/css 경로
                                "/favicon.ico",
                                "/vite.svg",
                                "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.svg",
                                "/*.css", "/*.js",
                                "/error"
                        ).permitAll()

                        // ✅ Swagger(있으면) 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ✅ 로그인/회원가입 등 인증 관련만 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // (선택) 헬스체크/actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // OPTIONS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 나머지 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 그 외(예: SPA 라우팅)도 permitAll 처리해도 됨.
                        // (SpaForwardController가 index.html로 포워딩하므로)
                        .anyRequest().permitAll()
                )

                // ✅ JWT 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ AuthenticationManager (로그인 시 인증에 사용하면 됨)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * ✅ PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ CORS: 개발 중 React(5173) + 배포(8080 same-origin) 모두 대응
     * - allowCredentials=true 이면 allowedOrigins에 "*" 못 씀 → patterns 사용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 로컬 개발: 5173 허용, 배포는 same-origin이므로 문제 없음
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "*" // 필요하면 유지, 배포에서는 도메인으로 제한 추천
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}