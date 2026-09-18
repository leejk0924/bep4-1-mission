package com.back.boundedContext.member.eventListener;

import com.back.boundedContext.member.service.MemberService;
import com.back.global.exception.DomainException;
import com.back.shared.post.PostCommentCreatedEvent;
import com.back.shared.post.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class MemberEventListener {
    private final MemberService memberService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(PostCreatedEvent event) {
        memberService.findById(event.post().authorId()).ifPresentOrElse(
                member -> member.increaseActivityScore(3),
                () -> {
                    throw new DomainException("401-2", "존재하지 않는 회원입니다. id : %d".formatted(event.post().authorId()));
                }
        );
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(PostCommentCreatedEvent event) {
        memberService.findById(event.postComment().authorId()).ifPresentOrElse(
                member -> member.increaseActivityScore(1),
                () -> {
                    throw new DomainException("401-3", "존재하지 않는 회원입니다. id : %d".formatted(event.postComment().authorId()));
                }
        );
    }

}
