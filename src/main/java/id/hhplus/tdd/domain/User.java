package id.hhplus.tdd.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name="USERS")
public class User {

    @Id
    @Column(name = "USER_ID")
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_NAME")
    private String name;

    @Column(nullable = false)
    private LocalDateTime regiDate;

    @Builder
    public User(String name) {
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        this.regiDate = LocalDateTime.now();
    }
}
