package com.back.entity;

import com.back.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseIdAndTime {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;
    private String title;
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    @OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    public Post(Member author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostComment addComment(Member author, String content) {
        PostComment postComment = PostComment.of(this, author, content);
        this.comments.add(postComment);
        return postComment;
    }

    public boolean hasComments() {
        return !comments.isEmpty();
    }
}
