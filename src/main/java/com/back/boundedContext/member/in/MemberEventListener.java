package com.back.boundedContext.member.in;

import com.back.boundedContext.member.app.MemberFacade;
import com.back.global.exception.DomainException;
import com.back.shared.post.evnet.PostCommentCreatedEvent;
import com.back.shared.post.evnet.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class MemberEventListener {
    private final MemberFacade memberFacade;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PostCreatedEvent event) {
        memberFacade.findById(event.post().authorId())
                .orElseThrow(() -> new DomainException(
                        "404-1",
                        "%d번 회원을 찾을 수 없습니다.".formatted(event.post().authorId())
                ))
                .increaseActivityScore(3);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PostCommentCreatedEvent event) {
        memberFacade.findById(event.postComment().authorId())
                .orElseThrow(() -> new DomainException(
                        "404-1",
                        "%d번 회원을 찾을 수 없습니다.".formatted(event.postComment().authorId())
                ))
                .increaseActivityScore(1);
    }
}
