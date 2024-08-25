package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "article_statistic")
public class ArticleStatistic {

    @Id
    private Long Id;

    @OneToOne
    private Article article;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "views")
    private Long views = 0L;

    @Column(name = "reactions")
    private Long reactions = 0L;

    @Column(name = "comments")
    private Long comments = 0L;

    @Column(name = "bookmarks")
    private Long bookmarks = 0L;

    public interface Repo extends JpaRepository<ArticleStatistic, Long> {}
}
