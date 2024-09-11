package com.nelmin.my_log.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Entity(name = "notification")
public class Notification {

    public enum Type {
        COMMENT, REPLY_COMMENT, LIKE, SUBSCRIBE, REPORT
    }

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private Type type;

    // jsonb
    private String payload;

    private Boolean isRead = false;

    @CreatedDate
    private LocalDateTime createdDate;

    public interface Repo extends JpaRepository<Notification, Long> {

        Page<Notification> findAllByUserId(Long userId, Pageable page);

        Long countByUserIdAndIsRead(Long id, boolean read);

        @Modifying
        @Query(
                value = "update notification set is_read = :read where user_id = :userId",
                nativeQuery = true
        )
        void updateAllByUserId(@Param("userId") Long userId, @Param("read") boolean b);
    }
}
