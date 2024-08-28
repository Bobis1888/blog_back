package com.nelmin.my_log.common.model;

import com.nelmin.my_log.common.abstracts.IUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "\"user\"")
public class User implements IUser {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickName;

    // TODO remove
    @Column(nullable = false)
    private String password;

    private String description;

    private String imagePath;

    @CreatedDate
    private LocalDateTime registrationDate;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginDate;

    private Boolean enabled = true;

    private Boolean blocked = false;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private Premium premium;

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public Boolean isPremiumUser() {
        log.debug("User premium {}", premium);

        if (Objects.isNull(premium)) {
            log.debug("User is not premium");
            return false;
        }

        return premium.getEnabled();
    }

    @Override
    public Boolean isBlocked() {
        return blocked;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!id.equals(user.id)) {
            return false;
        }

        if (!username.equals(user.username)) {
            return false;
        }

        if (!nickName.equals(user.nickName)) {
            return false;
        }

        return registrationDate.equals(user.registrationDate);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + nickName.hashCode();
        result = 31 * result + registrationDate.hashCode();
        return result;
    }

    @Repository
    public interface Repo extends JpaRepository<User, Long> {
        Optional<User> findUserByUsername(String username);

        Optional<User> findUserByNickName(String nickname);

        Optional<UserNickName> getNickNameById(Long id);

        Optional<UserId> getIdByNickName(String nickName);

        List<UserId> findAllByNickNameContaining(String nickName);

        List<User> findAllByNickNameContains(String nickName, Pageable pageable);

        List<UserIdAndNickName> getIdsAndNickNamesByIdIn(List<Long> ids);

        Boolean existsByUsername(String login);
    }

    public interface UserNickName {
        String getNickName();
    }

    public interface UserIdAndNickName {
        Long getId();

        String getNickName();
    }

    public interface UserId {
        Long getId();
    }
}
