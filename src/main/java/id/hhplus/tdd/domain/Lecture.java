package id.hhplus.tdd.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Lecture {
    @Id
    @Column(name = "LECTURE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "LECTURE_TITLE")
    private String title;

    @Column
    private Long limitCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime regiDate;

    @Builder
    public Lecture(String title, Long limitCount){
        this.title = title;
        this.limitCount = limitCount;
    }

    @PrePersist
    protected void onCreate() {
        this.regiDate = LocalDateTime.now();
    }
}
