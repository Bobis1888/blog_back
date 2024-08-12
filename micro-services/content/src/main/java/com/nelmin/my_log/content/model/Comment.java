package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "comment")
public class Comment {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "rating")
    private Long rating;

    @CreatedDate
    private LocalDateTime createdDate;

    @Repository
    public interface Repo extends JpaRepository<Comment, Long> {

        Page<Comment> findAllByArticleId(Long articleId, Pageable pageable);
        Long countByUserId(Long userid);
        void deleteByIdAndUserId(Long id, Long userId);
    }
}
