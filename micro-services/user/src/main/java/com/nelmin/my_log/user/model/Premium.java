package com.nelmin.my_log.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "premium")
public class Premium {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

//    @Column(name = "user_id", nullable = false)
//    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    private Boolean enabled = true;

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Premium premium = (Premium) o;
        return Objects.equals(id, premium.id) && Objects.equals(getUserId(), premium.getUserId()) && Objects.equals(createdDate, premium.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getUserId(), createdDate);
    }

    @Repository
    public interface Repo extends JpaRepository<Premium, Long> {
    }
}
