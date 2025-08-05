package site.matzip.config.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.matzip.config.oauth.provider.OAuth2UserInfo;
import site.matzip.member.domain.Member;

import java.util.*;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final Member member;
    private final OAuth2UserInfo oAuth2UserInfo;

    public PrincipalDetails(Member member, OAuth2UserInfo oAuth2UserInfo) {
        this.member = member;
        this.oAuth2UserInfo = oAuth2UserInfo;
    }

    public Long getUserId() {
        return member.getId();
    }

    public String getNickname() {
        return member.getNickname();
    }

    // UserDetails 필수 구현 메서드

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // member.getRole()을 사용하려면 Enum 기반 권한이면 그 값 사용
        // 예: ROLE_MEMBER
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(member.getRole().name()));
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }

    // OAuth2User 구현 메서드

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2UserInfo != null ? oAuth2UserInfo.getAttributes() : Collections.emptyMap();
    }

    @Override
    public String getName() {
        return oAuth2UserInfo != null ? oAuth2UserInfo.getProviderId() : member.getId().toString();
    }

    @Override
    public <A> A getAttribute(String name) {
        return oAuth2UserInfo != null ? (A) oAuth2UserInfo.getAttributes().get(name) : null;
    }
}
