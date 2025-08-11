package site.matzip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import site.matzip.config.auth.CustomAuthSuccessHandler;
import site.matzip.config.auth.UserLoginFailureHandler;
import site.matzip.config.jwt.JwtAuthFilter;
import site.matzip.config.oauth.CustomUserDetailsService;
import site.matzip.config.oauth.OAuth2LoginSuccessHandler;
import site.matzip.config.oauth.PrincipalOAuth2UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final UserLoginFailureHandler userLoginFailureHandler;   // 폼 로그인 실패 핸들러
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthSuccessHandler customAuthSuccessHandler; // ↔ defaultSuccessUrl 중 하나만 사용 권장

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/main", "/usr/member/signup",
                                "/usr/member/login", "/oauth2/**",
                                "/favicon.ico", "/manifest.json", "/robots.txt", "/sitemap.xml",
                                "/error",
                                "/common/**", "/resource/**", "/img/**",
                                "/api/auth/**"                         // 전용 로그인 API 공개
                        ).permitAll()
                        .requestMatchers(
                                org.springframework.boot.autoconfigure.security.servlet.PathRequest
                                        .toStaticResources().atCommonLocations()
                        ).permitAll()
                        // (옵션) 프리플라이트 허용 — SPA/다른 포트에서 호출 시 유용
                        //.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                // JSON 로그인 API는 CSRF 검사에서 제외
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**"))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form
                                .loginPage("/usr/member/login")
                                .loginProcessingUrl("/usr/member/login")
                                .failureHandler(userLoginFailureHandler)
                                .successHandler(customAuthSuccessHandler) // JSON 발급 스타일이면 유지
                        //.defaultSuccessUrl("/main")             // 리다이렉트 방식이면 이걸 쓰고 위 successHandler는 제거
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/usr/member/login")
                        .userInfoEndpoint(u -> u.userService(principalOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(authenticationFailureHandler()) // @Bean 메서드 호출(필드 주입 제거)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/usr/member/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("access_token")
                )
                .authenticationProvider(authenticationProvider()) // 메서드 호출로 변경
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/debug/**")
                        )
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                // 2) SavedRequest(302 후 돌아가기) 막기
                .requestCache(RequestCacheConfigurer::disable);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            exception.printStackTrace();
            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("/login?error=true&message=" + encodedMessage);
        };
    }
}