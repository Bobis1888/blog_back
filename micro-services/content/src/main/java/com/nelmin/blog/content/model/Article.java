package com.nelmin.blog.content.model;

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
            this.tags = String.join(",", tags).toLowerCase();
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

    @Repository
    public interface Repo extends JpaRepository<Article, Long> {
        Optional<Article> findByIdAndUserId(Long id, Long userId);

        Page<Article> findAllByStatusIn(List<Status> status, Pageable pageable);

        Page<ArticleId> getIdsByStatusIn(List<Status> status, Pageable pageable);

        Page<Article> findAllByStatusInAndUserId(List<Status> status, Long userId, Pageable pageable);

        @Query(
                value = "select * from article where status in :status and (title ilike concat('%', :query, '%') or content ilike concat('%', :query, '%'))",
                countQuery = "select count(*) from article where status in :status and (title ilike concat('%', :query, '%') or content ilike concat('%', :query, '%'))",
                nativeQuery = true
        )
        Page<Article> findAllByContent(@Param("status") List<String> status, @Param("query") String query, Pageable pageable);

        @Query(
                value = "select * from article where  status in :status and tags ilike concat('%', :tags, '%')",
                countQuery = "select count(*) from article where  status in :status and tags ilike concat('%', :tags, '%')",
                nativeQuery = true
        )
        Page<Article> findAllByTags(@Param("status") Collection<String> status, @Param("tags") String tags, Pageable pageable);

        @Query(
                value = "select distinct tags from article where status in :status",
                countQuery = "select count(distinct tags) from article where status in :status",
                nativeQuery = true
        )
        Page<String> getTags(@Param("status") Collection<String> status, PageRequest of);

        @Query(
                value = "select distinct tags from article where status in :status and (tags ilike concat('%', :query, '%'))",
                countQuery = "select count(distinct tags) from article where status in :status and (tags ilike concat('%', :query, '%'))",
                nativeQuery = true
        )
        Page<String> getTags(@Param("status") Collection<String> status, @Param("query") String query, PageRequest of);
    }

    public interface ArticleId {
        Long getId();
    }

    public enum Status {
        DRAFT,
        PUBLISHED,
        PENDING,
        DELETED
    }
}
