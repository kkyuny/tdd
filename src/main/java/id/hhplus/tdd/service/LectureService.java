package id.hhplus.tdd.service;

import id.hhplus.tdd.domain.Lecture;
import id.hhplus.tdd.domain.User;
import id.hhplus.tdd.domain.UserLecture;
import id.hhplus.tdd.repository.LectureRepository;
import id.hhplus.tdd.repository.UserLectureRepository;
import id.hhplus.tdd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LectureService {
    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLectureRepository userLectureRepository;

    public Lecture createLecture(String title, Long limitCount) {
        Lecture lecture = Lecture.builder()
                .title(title)
                .limitCount(limitCount)
                .build();

        return lectureRepository.save(lecture);
    }

    @Transactional
    public UserLecture registerUserToLecture(Long lectureId, Long userId) {
        // LectureRepository에서 findById를 비관적 락 설정

        // exception이 발생하였을 때 testController에서 catch?감지?하여 처리하는 방법이 궁금합니다.
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("Not exist lecture:" + lectureId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not exist user:" + userId));

        long registeredCount = userLectureRepository.countByLecture(lecture);

        // 기본적으로 false 입력
        UserLecture userLecture = new UserLecture(lecture, user, false);
        // 조건 충족시에만 true 처리
        if (registeredCount <= lecture.getLimitCount()) {
            userLecture = new UserLecture(lecture, user, true);
        }
        return userLectureRepository.save(userLecture);
    }

    public Boolean getUserLectureHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));

        return userLectureRepository.existsByUser(user);
    }

    public List<Lecture> getLectures() {
        return lectureRepository.findAll();
    }
}
