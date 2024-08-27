package com.nelmin.my_log.storage.model;


import com.nelmin.my_log.storage.dto.FileType;
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
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Table(name = "storage")
public class Storage {

    @Id
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    private Long id;

    private String uuid;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private FileType type;

    private String contentType;

    private byte[] file = new byte[0];

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Repository
    public interface Repo extends JpaRepository<Storage, Long> {
        Optional<Storage> findByUserIdAndType(Long userId, FileType type);
        Optional<Storage> findByUuid(String uuid);
    }
}
