package site.matzip.review.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import site.matzip.base.appConfig.AppConfig;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.domain.MatzipType;
import site.matzip.matzip.dto.MatzipCreationDTO;
import site.matzip.matzip.service.MatzipService;
import site.matzip.member.domain.Member;
import site.matzip.member.service.MemberService;
import site.matzip.review.domain.Review;
import site.matzip.review.dto.ReviewCreationDTO;
import site.matzip.review.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
//    private Member testUser;
//    private Matzip matzip;
//    @Autowired
//    private ReviewService reviewService;
//    @Autowired
//    private MemberService memberService;
//    @Autowired
//    private MatzipService matzipService;
//    @Mock
//    private AppConfig appConfig;
//    @Mock
//    private ReviewRepository reviewRepository;
//    @Mock
//    private HttpServletRequest request;
//    @Mock
//    private HttpServletResponse response;
//    ReviewCreationDTO reviewCreationDTO = ReviewCreationDTO.builder()
//            .rating(4.0)
//            .content("맛있어요")
//            .build();
//
//    MatzipCreationDTO matzipCreationDTO = MatzipCreationDTO.builder()
//            .address("서울 용산구 이태원동 258-7")
//            .kakaoId(7849814L)
//            .matzipName("라쿠치나")
//            .matzipType(String.valueOf(MatzipType.KOREAN))
//            .phoneNumber("02-794-6006")
//            .x(126.99647354065)
//            .y(37.5405570353927)
//            .build();
//
//    @BeforeEach
//    public void setUp() {
//        // Member 생성
//        testUser = memberService.signUp("testUser",
//                "testUser", "1234", "test@email.com");
//
//        // Matzip 생성
//        matzip = matzipService.create(matzipCreationDTO, testUser.getId());
//    }
//
//    @Test
//    void create() {
//        reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//
//        List<Review> reviews = reviewService.findAll();
//        Review fristReview = reviews.get(0);
//
//        assertThat(fristReview.getContent()).isEqualTo("맛있어요");
//        assertThat(fristReview.getAuthor()).isEqualTo(testUser);
//    }
//
//    @Test
//    void remove() {
//        ReviewCreationDTO reviewCreationDTO = ReviewCreationDTO.builder()
//                .rating(4.0)
//                .content("맛있어요")
//                .build();
//
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//
//        testUser.removeReview(review);
//        matzip.removeReview(review);
//
//        reviewService.remove(review);
//
//        List<Review> reviews = reviewService.findAll();
//
//        assertThat(reviews.size()).isEqualTo(0);
//    }
//
//    @Test
//    void modify() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//
//        ReviewCreationDTO modifyReviewCreationDTO = ReviewCreationDTO.builder()
//                .rating(2.0)
//                .content("맛없다")
//                .build();
//
//        reviewService.modify(review, modifyReviewCreationDTO);
//
//        assertThat(review.getContent()).isEqualTo("맛없다");
//    }
//
//    @Test
//    void incrementViewCount() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//        reviewService.incrementViewCount(review);
//
//        Review result = reviewService.findById(review.getId());
//
//        assertThat(result.getViews()).isEqualTo(1);
//    }
//
//    @Test
//    void updateViewCountWithCookie_noCookie() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//        when(request.getCookies()).thenReturn(null);
//
//        reviewService.updateViewCountWithCookie(review, request, response);
//
//        Review result = reviewService.findById(review.getId());
//
//        assertThat(result.getViews()).isEqualTo(1);
//        verify(response, times(1)).addCookie(any(Cookie.class));
//    }
//
//    @Test
//    void updateViewCountWithCookie_withCookie_notInView() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//        Cookie[] cookies = {new Cookie("reviewView", "[]")};
//
//        when(request.getCookies()).thenReturn(cookies);
//
//        reviewService.updateViewCountWithCookie(review, request, response);
//
//        Review result = reviewService.findById(review.getId());
//
//        assertThat(result.getViews()).isEqualTo(1);
//        verify(response, times(1)).addCookie(any(Cookie.class));
//    }
//
//    @Test
//    void updateViewCountWithCookie_withCookie_alreadyInView() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//        Cookie[] cookies = {new Cookie("reviewView", "[" + review.getId() + "]")};
//
//        when(request.getCookies()).thenReturn(cookies);
//
//        reviewService.updateViewCountWithCookie(review, request, response);
//
//        Review result = reviewService.findById(review.getId());
//
//        assertThat(result.getViews()).isEqualTo(0);
//    }
//
//    @Test
//    void rewardPointsForReviews() {
//        long referenceTimeHours = appConfig.getPointRewardReferenceTime();
//        long pointRewardReview = appConfig.getPointRewardReview();
//
//        LocalDateTime referenceTime = LocalDateTime.now().minusHours(referenceTimeHours + 1);
//        Review expiredReview = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//
//        expiredReview.setCreatedDate(referenceTime);
//
//        lenient().when(reviewRepository.findReviewsOlderThan(any())).thenReturn(Collections.singletonList(expiredReview));
//
//        reviewService.rewardPointsForReviews();
//
//        assertThat(testUser.getPoint()).isEqualTo(pointRewardReview);
//    }
//
//    @Test
//    void updateHeartTest() {
//        Review review = reviewService.create(reviewCreationDTO, testUser.getId(), matzip);
//
//        reviewService.updateHeart(testUser.getId(), review.getId());
//
//        assertThat(review.getHearts()).isNotEmpty();
//        assertThat(review.getHearts().size()).isEqualTo(1);
//
//        review.removeHeart(review.getHearts().get(0));
//        reviewService.updateHeart(testUser.getId(), review.getId());
//
//        assertThat(review.getHearts()).isEmpty();
//    }
}