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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "private_link")
public class PrivateLink {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "link", nullable = false)
    private String link = UUID.randomUUID().toString();

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate = LocalDateTime.now().plusDays(365);

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public interface Repo extends JpaRepository<PrivateLink, Long> {
        Optional<PrivateLinkArticleId> getArticleIdByLink(String privateLink);

        void deleteAllByArticleIdIsIn(List<Long> ids);
    }

    public interface PrivateLinkArticleId {
        Long getArticleId();
    }
}
