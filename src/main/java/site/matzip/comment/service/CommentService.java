package site.matzip.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.matzip.base.appConfig.AppConfig;
import site.matzip.base.event.EventAfterComment;
import site.matzip.comment.domain.Comment;
import site.matzip.comment.repository.CommentRepository;
import site.matzip.member.domain.Member;
import site.matzip.member.repository.MemberRepository;
import site.matzip.review.domain.Review;
import site.matzip.review.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher publisher;
    private final AppConfig appConfig;

    @Transactional
    public void create(Long reviewId, Long authorId, String content) {
        Review review = reviewRepository.getReferenceById(reviewId);
        Member author = memberRepository.getReferenceById(authorId);
        Comment createdComment = Comment.builder()
                .content(content)
                .build();

        createdComment.addAssociation(review, author);
        commentRepository.save(createdComment);

        if (!review.getAuthor().getNickname().equals(author.getNickname())) {
            // 본인 리뷰에 본인이 댓글을 단 경우를 제외하고 이벤트 발행
            publisher.publishEvent(new EventAfterComment(this, review.getAuthor(), author));
        }
    }

    @Transactional
    public void remove(Long commentId) {
        Comment comment = findById(commentId);

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not Found"));
    }

    @Transactional
    @Scheduled(fixedRate = 10 * 60 * 1000) // 주기 10분
    public void rewardPointsForComments() {
        LocalDateTime referenceTime = LocalDateTime.now().minusHours(appConfig.getPointRewardReferenceTime());
        List<Comment> validComments = commentRepository.findCommentsOlderThan(referenceTime);

        for (Comment comment : validComments) {
            if (!comment.isPointsRewarded()) {
                Member author = comment.getAuthor();
                author.addPoints(appConfig.getPointRewardComment());
                memberRepository.save(author); // 포인트 업데이트
                comment.updatePointsRewarded(); // 포인트 지급 여부 업데이트
                commentRepository.save(comment); // 댓글 업데이트
            }
        }
    }

    public Comment checkAccessPermission(Long authorId, Long commentId) {
        Comment comment = findById(commentId);

        if (!Objects.equals(comment.getAuthor().getId(), authorId)) {
            throw new AccessDeniedException("You do not have permission to delete.");
        }

        return comment;
    }
}