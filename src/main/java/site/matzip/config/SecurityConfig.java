package site.matzip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import site.matzip.config.auth.UserLoginFailureHandler;
import site.matzip.config.oauth.OAuth2LoginSuccessHandler;
import site.matzip.config.oauth.PrincipalOAuth2UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // 직접 생성하지 않고 주입받음
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final UserLoginFailureHandler userLoginFailureHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(AbstractHttpConfigurer::disable) // 필요시 CSRF 활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/usr/member/login")
                                .failureHandler(userLoginFailureHandler)
                                .defaultSuccessUrl("/main")
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login
                                .loginPage("/usr/member/login")
                                .userInfoEndpoint()
                                .userService(principalOAuth2UserService) // OAuth2.0 로그인 성공 시 실행
                                .and()
                                .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler(authenticationFailureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/usr/member/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("access_token") // 로그아웃 시 `access_token` 쿠키 삭제
                );

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //TODO: 임시 로그인 기능 없어지면 삭제 예정
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            exception.printStackTrace(); // 콘솔에 오류 출력
            String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("/login?error=true&message=" + encodedMessage);
        };
    }
}