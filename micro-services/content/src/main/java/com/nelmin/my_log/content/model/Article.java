package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "article")
public class Article {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "pre_view", nullable = false)
    private String preView;

    @Column(name = "tags")
    private String tags;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    private LocalDateTime publishedDate;

    public void setTags(List<String> tags) {

        if (tags != null && !tags.isEmpty()) {
            this.tags = String.join(",", tags).replaceAll(" ", "");
        } else {
            this.tags = null;
        }
    }

    public List<String> getTags() {

        if (StringUtils.hasText(tags)) {
            return List.of(tags.split(","));
        }

        return List.of();
    }

    // TODO Specification
    @Repository
    public interface Repo extends JpaRepository<Article, Long> {
        Optional<Article> findByIdAndUserId(Long id, Long userId);

        Page<ArticleId> getIdsByStatusIn(List<Status> status, Pageable pageable);

        @Query(
                value = "select * from article a where a.id in (select b.article_id from bookmark b where b.user_id = :userId)",
                countQuery = "select count(*) from article a where a.id in (select b.article_id from bookmark b where b.user_id = :userId)",
                nativeQuery = true
        )
        Page<Article> findAllInBookmarks(
                @Param("userId") Long userId,
                Pageable pageable);

        @Query(
                value = "select * from article a where a.id in (select b.article_id from bookmark b where b.user_id = :userId) and " +
                        "(a.title ~* :query  or a.content ~* :query)",
                countQuery = "select count(*) from article a where a.id in (select b.article_id from bookmark b where b.user_id = :userId) and " +
                        "(a.title ~* :query  or a.content ~* :query)",
                nativeQuery = true
        )
        Page<Article> findAllInBookmarks(
                @Param("userId") Long userId,
                @Param("query") String query,
                Pageable pageable);

        @Query(
                value = "select * from article a where a.status in :status and " +
                        "(a.title ~* :query  or a.content ~* :query)",
                countQuery = "select count(*) from article a where a.status in :status and " +
                        "(a.title ~* :query  or a.content ~* :query)",
                nativeQuery = true
        )
        Page<Article> findAllByContent(
                @Param("status") Collection<String> status,
                @Param("query") String query,
                Pageable pageable);


        @Query(
                value = "select * from article a where a.status in :status and " +
                        "(a.tags ~* :query)",
                countQuery = "select count(*) from article a where a.status in :status and " +
                        "(a.tags ~* :query)",
                nativeQuery = true
        )
        Page<Article> findAllByTags(
                @Param("status") Collection<String> status,
                @Param("query") String query,
                Pageable pageable);


        @Query(
                value = "select * from article a where a.status in :status and a.user_id in :userIds",
                countQuery = "select count(*) from article a where a.status in :status and a.user_id in :userIds",
                nativeQuery = true
        )
        Page<Article> findAllByUserIds(
                @Param("userIds") Collection<Long> userIds,
                @Param("status") Collection<String> status,
                Pageable pageable);

        @Query(
                value = "select * from article a where a.status in :status",
                countQuery = "select count(*) from article a where a.status in :status",
                nativeQuery = true
        )
        Page<Article> findAllByStatus(
                @Param("status") Collection<String> status,
                Pageable pageable);

        @Query(
                value = "select distinct tags from article where status in :status",
                countQuery = "select count(distinct tags) from article where status in :status",
                nativeQuery = true
        )
        Page<String> getTags(@Param("status") Collection<String> status, PageRequest of);

        @Query(
                value = "select distinct tags from article where status in :status and (tags ~* :query)",
                countQuery = "select count(distinct tags) from article where status in :status and (tags ~* :query)",
                nativeQuery = true
        )
        Page<String> getTags(@Param("status") Collection<String> status, @Param("query") String query, PageRequest of);

        Long countByStatusAndUserId(Status status, Long userid);

        @Query(
                value = "SELECT * FROM article " +
                        "LEFT JOIN (SELECT article_id, count(id) likes FROM \"like\" GROUP BY article_id) lk " +
                        "ON article.id = lk.article_id " +
                        "WHERE article.status = 'PUBLISHED' order by lk.likes desc nulls last",
                countQuery = "SELECT count(*) FROM article " +
                        "LEFT JOIN (SELECT article_id, count(id) likes FROM \"like\" GROUP BY article_id) lk " +
                        "ON article.id = lk.article_id " +
                        "WHERE article.status = 'PUBLISHED'",
                nativeQuery = true
        )
        Page<Article> findAllMostPopular(Pageable request);
    }

    public interface ArticleId {
        Long getId();
    }

//    // PostgreSQL ~*
//    static Specification<Article> findByParams() {
//        return (articleRoot, cq, cb) -> cb.some("textregexeq", Boolean.class, message, cb.literal(re));
//    }

    public enum Status {
        DRAFT,
        PUBLISHED,
        PRIVATE_PUBLISHED,
        PENDING,
        DELETED
    }
}
