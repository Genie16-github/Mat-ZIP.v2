package site.matzip.member.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import site.matzip.badge.domain.Badge;
import site.matzip.badge.domain.MemberBadge;
import site.matzip.badge.repository.MemberBadgeRepository;
import site.matzip.base.appConfig.AppConfig;
import site.matzip.base.rsData.RsData;
import site.matzip.friend.dto.FriendDetailDTO;
import site.matzip.friend.domain.Friend;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.domain.MatzipMember;
import site.matzip.matzip.dto.MatzipInfoDTO;
import site.matzip.member.domain.Member;
import site.matzip.member.domain.MemberToken;
import site.matzip.member.dto.*;
import site.matzip.member.repository.MemberRepository;
import site.matzip.member.repository.MemberTokenRepository;
import site.matzip.review.domain.Review;
import site.matzip.review.dto.MyReviewDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfig appConfig;

    @Value("${token.content-type}")
    private String contentType;

    @Value("${token.grant_type}")
    private String grantType;

    @Value("${spring.security.oauth2.client.registration.kakao.clientId}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    public void logout(Long memberId, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        //deleteCookie(servletRequest, servletResponse);
        MemberToken findMemberToken = findMemberToken(memberId);

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + findMemberToken.getAccessToken());

        // 해더와 바디를 하나의 오브젝트로 만들기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(headers);

        // Http 요청하고 리턴값을 response 변수로 받기
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/logout", // Host
                HttpMethod.POST, // Request Method
                kakaoTokenRequest,    // RequestBody
                String.class);    // return Object
        log.info("logout response = {}", response);
    }

    public void unlink(Long memberId) {
        Member findMember = findMember(memberId);
        MemberToken findMemberToken = findMemberToken(memberId);

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + findMemberToken.getAccessToken());

        // 해더와 바디를 하나의 오브젝트로 만들기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(null, headers);

        // Http 요청하고 리턴값을 response 변수로 받기
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v1/user/unlink", // Host
                HttpMethod.POST, // Request Method
                kakaoTokenRequest,    // RequestBody
                String.class);    // return Object
        log.info("unlink response = {}", response);
    }

    private void deleteCookie(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 무효화
        }
        // JSESSIONID 쿠키 삭제
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        servletResponse.addCookie(cookie);
    }

    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    private MemberToken findMemberToken(Long memberId) {
        return memberTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("MemberToken not found"));
    }

    public Member findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    @Transactional
    public Member signUp(String username, String kakao_nickname, String password, String email) {
        password = passwordEncoder.encode(password);
        Member member = Member.builder()
                .username(username)
                .kakao_nickname(kakao_nickname)
                .nickname(kakao_nickname)
                .password(password)
                .email(email)
                .build();
        member = memberRepository.save(member);
        return member;
    }

    public boolean isUsernameAvailable(String username) {
        return !memberRepository.existsByUsername(username);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public RsData<Member> modifyNickname(Member member, NicknameUpdateDTO nicknameUpdateDTO) {
        if (isNicknameTaken(nicknameUpdateDTO.getNickname())) {
            return RsData.of("F-1", "이미 사용중인 닉네임 입니다.");
        }

        member.updateNickname(nicknameUpdateDTO.getNickname());

        memberRepository.save(member); // 변경사항 저장

        return RsData.of("S-1", "닉네임이 변경되었습니다.");
    }

    private boolean isNicknameTaken(String nickname) {
        return memberRepository.findByNickname(nickname).isPresent();
    }

    public List<MemberRankDTO> findAndConvertTopTenMember() {
        List<Member> members = memberRepository.findTop10ByOrderByPointDesc();
        return members.stream().map(this::convertToMemberDTO).collect(Collectors.toList());
    }

    public int findMemberRankByUsername(String username) {
        List<Member> orderedMembers = memberRepository.findAllByOrderByPointDesc();
        for (int rank = 0; rank < orderedMembers.size(); rank++) {
            if (orderedMembers.get(rank).getUsername().equals(username)) {
                return rank + 1;
            }
        }
        return -1;
    }

    // 멤버 주위로 10명(멤버 포함)
    public List<MemberRankDTO> findAndConvertTenMemberAroundMember(Long memberId) {
        List<MemberRankInfoDTO> membersWithRank = getTop10MemberAroundUserWithRank(memberId);
        return membersWithRank.stream().map(this::convertToMemberRankDTO).collect(Collectors.toList());
    }

    private MemberRankDTO convertToMemberRankDTO(MemberRankInfoDTO memberWithRank) {
        Member member = memberWithRank.getMember();

        String profileImageUrl = appConfig.getDefaultProfileImageUrl();

        return MemberRankDTO.builder()
                .rank(memberWithRank.getRank())
                .profileImageUrl(member.getProfileImage() != null ? member.getProfileImage().getImageUrl() : profileImageUrl)
                .nickname(member.getNickname())
                .badgeImage(showMemberBadge(member))
                .point(member.getPoint())
                .build();
    }

    public List<MemberRankInfoDTO> getTop10MemberAroundUserWithRank(Long memberId) {
        Member currentMember = findMember(memberId);

        List<Member> allOrderedMembers = memberRepository.findAllByOrderByPointDesc();

        int currentMemberRank = allOrderedMembers.indexOf(currentMember) + 1;
        int startIndex = Math.max(0, currentMemberRank - 5); // 본인 순위에서 5명 전부터 시작

        // 상위 멤버와 하위 멤버를 합쳐서 10명의 회원을 가져옴
        List<MemberRankInfoDTO> top10MembersWithRank = new ArrayList<>();

        for (int i = startIndex; i < startIndex + 10 && i < allOrderedMembers.size(); i++) {
            Member member = allOrderedMembers.get(i);
            top10MembersWithRank.add(new MemberRankInfoDTO(i + 1, member));
        }

        return top10MembersWithRank;
    }

    public MemberPointDTO convertToMemberPointDTO(Long memberId) {
        Member member = findMember(memberId);

        int rank = findMemberRankByUsername(member.getUsername());

        return MemberPointDTO.builder()
                .point(member.getPoint())
                .rank(rank)
                .build();
    }

    private MemberRankDTO convertToMemberDTO(Member member) {
        String profileImageUrl = appConfig.getDefaultProfileImageUrl();

        return MemberRankDTO.builder()
                .rank(0)
                .profileImageUrl(member.getProfileImage() != null ? member.getProfileImage().getImageUrl() : profileImageUrl)
                .nickname(member.getNickname())
                .badgeImage(showMemberBadge(member))
                .point(member.getPoint())
                .build();
    }

    public List<MatzipInfoDTO> convertToMatzipInfoDTO(Long memberId) {
        Member member = findMember(memberId);
        List<MatzipInfoDTO> matzipInfoDTOS = new ArrayList<>();

        for (MatzipMember matzipMember : member.getMatzipMembers()) {
            Matzip matzip = matzipMember.getMatzip();
            MatzipInfoDTO matzipInfoDTO = new MatzipInfoDTO(matzip);
            matzipInfoDTOS.add(matzipInfoDTO);
        }

        return matzipInfoDTOS;
    }

    public List<MyReviewDTO> converToMyReviewDTO(Long memberId) {
        Member member = findMember(memberId);
        List<MyReviewDTO> myReviewDTOS = new ArrayList<>();

        for (Review review : member.getReviews()) {
            MyReviewDTO myReviewDTO = new MyReviewDTO(review);
            myReviewDTOS.add(myReviewDTO);
        }

        return myReviewDTOS;
    }

    public List<FriendDetailDTO> converToFriendDetailDTO(Long memberId) {
        Member member = findMember(memberId);
        List<FriendDetailDTO> friendDetailDTOS = new ArrayList<>();

        String profileImageUrl = appConfig.getDefaultProfileImageUrl();

        for (Friend friend : member.getFriends1()) {
            FriendDetailDTO friendDetailDTO = FriendDetailDTO.builder()
                    .id(friend.getId())
                    .memberId(friend.getMember2().getId())
                    .profileImageUrl(friend.getMember2().getProfileImage() != null ? friend.getMember2().getProfileImage().getImageUrl() : profileImageUrl)
                    .friendNickname(friend.getMember2().getNickname())
                    .badgeImage(showMemberBadge(friend.getMember2()))
                    .build();
            friendDetailDTOS.add(friendDetailDTO);
        }

        return friendDetailDTOS;
    }

    public MemberInfoCntDTO convertToMemberInfoCntDTO(Long memberId) {
        Member member = findMember(memberId);

        return MemberInfoCntDTO.builder()
                .matzipCnt(member.getMatzipMembers().size())
                .reviewCnt(member.getReviews().size())
                .friendCnt(member.getFriends2().size())
                .point(member.getPoint())
                .build();
    }

    public MemberProfileDTO convertToMemberProfileDTO(String nickname) {
        Member member = findByNickname(nickname);

        String profileImageUrl = appConfig.getDefaultProfileImageUrl();

        return MemberProfileDTO.builder()
                .profileImageUrl(member.getProfileImage() != null ? member.getProfileImage().getImageUrl() : profileImageUrl)
                .nickname(member.getNickname())
                .matzipCount(member.getMatzipMembers().size())
                .reviewCount(member.getReviews().size())
                .friendCount(member.getFriends2().size())
                .point(member.getPoint())
                .build();
    }

    public MemberInfoDTO convertToMemberInfoDTO(Long memberId) {
        Member member = findMember(memberId);

        String profileImageUrl = appConfig.getDefaultProfileImageUrl();

        return MemberInfoDTO.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImage() != null ? member.getProfileImage().getImageUrl() : profileImageUrl)
                .badgeImage(showMemberBadge(member))
                .build();
    }

    public Map<String, String> showMemberBadge(Member member) {
        List<MemberBadge> memberBadges = memberBadgeRepository.findByMember(member);
        Map<String, String> badgeMap = new HashMap<>();

        for (MemberBadge memberBadge : memberBadges) {
            Badge badge = memberBadge.getBadge();
            String imageUrl = badge.getImageUrl();
            String badgeTypeLabel = badge.getBadgeType().label();

            badgeMap.put(imageUrl, badgeTypeLabel);
        }
        return badgeMap;
    }
}