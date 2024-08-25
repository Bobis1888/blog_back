package com.nelmin.my_log.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "user_statistic")
public class UserStatistic {

    @Id
    private Long Id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "articles")
    private Long articles;

    @Column(name = "views")
    private Long views;

    @Column(name = "reactions")
    private Long reactions;

    @Column(name = "comments")
    private Long comments;

    @Column(name = "subscribers")
    private Long subscribers;

    public interface Repo extends JpaRepository<UserStatistic, Long> {

        Optional<UserStatistic> findByUserId(Long userId);

        List<UserStatistic> findAllByUserIdIn(List<Long> userIds);
    }
}
