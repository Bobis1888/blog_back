package com.nelmin.my_log.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "report")
public class Report {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;
    private Long userId;

    private Long articleId;
    private String type;
    private String description;

    @CreatedDate
    private LocalDateTime createdDate;

    public interface Repo extends JpaRepository<Report, Long> {

        @Query(
                value = "select article_id as id, count(article_id) as count from report group by article_id",
                nativeQuery = true
        )
        List<ReportsCount> countReport();
        Boolean existsByArticleIdAndUserId(Long articleId, Long userId);
        void deleteAllByArticleIdIn(List<Long> ids);
    }

    public interface ReportsCount {
        Long getCount();
        Long getId();
    }
}
