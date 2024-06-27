package id.hhplus.tdd.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class UserLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "LECTURE_ID")
    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(nullable = false)
    private boolean registerResult;

    @Column(nullable = false)
    private LocalDateTime regiDate;

    public UserLecture(Lecture lecture, User user, boolean registerResult) {
        this.lecture = lecture;
        this.user = user;
        this.registerResult = registerResult;
    }

    @PrePersist
    protected void onCreate() {
        this.regiDate = LocalDateTime.now();
    }
}
