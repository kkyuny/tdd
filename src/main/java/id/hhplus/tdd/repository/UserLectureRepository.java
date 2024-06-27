package id.hhplus.tdd.repository;

import id.hhplus.tdd.domain.Lecture;
import id.hhplus.tdd.domain.User;
import id.hhplus.tdd.domain.UserLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {
    Boolean existsByUser(User user);

    long countByLecture(Lecture lecture);
}
