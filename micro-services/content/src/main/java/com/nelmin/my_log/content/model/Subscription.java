package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Builder
@Table(name = "subscription")
public class Subscription {
    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @CreatedDate
    private LocalDateTime createdDate;

    public interface Repo extends JpaRepository<Subscription, Long> {

        Page<Subscription> findAllByUserId(Long userId, Pageable pageable);

        List<AuthorProjection> findAllByUserId(Long userId);

        Page<Subscription> findAllByAuthorId(Long authorId, Pageable pageable);

        Long countByAuthorId(Long authorId);

        Long countByUserId(Long userId);

        Optional<Subscription> findByUserIdAndAuthorId(Long userId, Long authorId);

        boolean existsByUserIdAndAuthorId(Long userId, Long authorId);

        void deleteByUserIdAndAuthorId(Long id, Long authorId);
    }

    public interface AuthorProjection {
        Long getAuthorId();
    }
}
