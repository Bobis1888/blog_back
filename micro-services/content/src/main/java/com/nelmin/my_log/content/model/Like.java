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
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "\"like\"")
public class Like {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * true = like, false = dislike
     */
    @Column(name = "value", nullable = false)
    private Boolean value;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public interface Repo extends JpaRepository<Like, Long> {
        Optional<Like> findByArticleIdAndUserId(Long articleId, Long userId);
        boolean existsByArticleIdAndUserId(Long articleId, Long userId);
        Optional<IsLiked> getValueByArticleIdAndUserId(Long articleId, Long userId);
        Long countByArticleIdAndValue(Long articleId, Boolean value);

        @Query(
                value = "select count(*) from \"like\" l " +
                        "where l.article_id in (select a.id from article a where a.user_id = :userId) " +
                        "and l.value = true",
                nativeQuery = true
        )
        Long countByUserId(@Param("userId") Long userId);

        void deleteByArticleIdAndUserId(Long articleId, Long userId);
    }

    public interface IsLiked {
        Boolean getValue();
    }
}
