package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "reaction")
public class Reaction {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "value", nullable = false)
    private String value;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public interface Repo extends JpaRepository<Reaction, Long> {
        Optional<Reaction> findByArticleIdAndUserId(Long articleId, Long userId);
        boolean existsByArticleIdAndUserId(Long articleId, Long userId);
        Optional<IsReacted> getValueByArticleIdAndUserId(Long articleId, Long userId);

        @Query(
                value = "select count(l.value), l.value from reaction l " +
                        "where l.article_id = :articleId " +
                        "group by l.value;",
                nativeQuery = true
        )
        List<CountReaction> countByArticleId(Long articleId);

        @Query(
                value = "select count(l.value), l.value from reaction l " +
                        "where l.article_id in (select a.id from article a where a.user_id = :userId) " +
                        "group by l.value;",
                nativeQuery = true
        )
        List<CountReaction> countByUserId(@Param("userId") Long userId);

        void deleteByArticleIdAndUserId(Long articleId, Long userId);
        void deleteAllByArticleIdIn(List<Long> ids);
    }

    public interface IsReacted {
        String getValue();
    }

    public interface CountReaction {
        Long getCount();
        String getValue();
    }
}
