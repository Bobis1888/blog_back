package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    // MOVE OUT
    @Column(name = "count_views", nullable = false)
    private Long countViews = 0L;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Bookmark> bookmark;

    @OneToOne(mappedBy = "article")
    private ArticleStatistic statistic;

    public void setTags(Set<String> tags) {

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
    public interface Repo extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
        Optional<Article> findByIdAndUserId(Long id, Long userId);

        Page<ArticleId> getIdsByStatusIn(List<Status> status, Pageable pageable);

        @Query(
                value = "select * from article a where a.status in :status",
                countQuery = "select count(*) from article a where a.status in :status",
                nativeQuery = true
        )
        Page<Article> findAllByStatus(
                @Param("status") Collection<String> status,
                Pageable pageable);

        @Query(
                value = "select tag, count(*) as count " +
                        "from article a , unnest(string_to_array(tags, ',')) as tag " +
                        "where a.status = 'PUBLISHED' " +
//                        "and a.updated_date >= current_timestamp - interval '24 hours' " +
                        "and (tag ~* :query) " +
                        "group by tag " +
                        "order by count desc " +
                        "limit :limit",
                nativeQuery = true
        )
        List<TagCount> extractTags(@Param("limit") Integer limit, @Param("query") String query);

        Long countByStatusAndUserId(Status status, Long userid);

        @Query(
                value = "SELECT * FROM article " +
                        "LEFT JOIN (SELECT article_id, count(id) reactions FROM reaction GROUP BY article_id) lk " +
                        "ON article.id = lk.article_id " +
                        "WHERE article.status = 'PUBLISHED' order by lk.reactions desc nulls last, article.count_views desc nulls last",
                countQuery = "SELECT count(*) FROM article " +
                        "LEFT JOIN (SELECT article_id, count(id) reactions FROM reaction GROUP BY article_id) lk " +
                        "ON article.id = lk.article_id " +
                        "WHERE article.status = 'PUBLISHED'",
                nativeQuery = true
        )
        Page<Article> findAllMostPopular(Pageable request);


        @Modifying
        @Query(
                value = "update article set count_views = count_views + 1 where id = :id",
                nativeQuery = true
        )
        void increaseCountViews(Long id);

        @Modifying
        @Query(
            value = "delete from article where id in :ids",
            nativeQuery = true
        )
        void deleteAllByIdIn(@Param("ids") Collection<Long> ids);
    }

    public interface ArticleId {
        Long getId();
    }

    public interface TagCount {
        String getTag();

        Long getCount();
    }

    public enum Status {
        DRAFT,
        PUBLISHED,
        PRIVATE_PUBLISHED,
        PENDING,
        BLOCKED,
        DELETED
    }
}
