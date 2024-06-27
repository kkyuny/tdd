package id.hhplus.tdd.controller;

import id.hhplus.tdd.domain.Lecture;
import id.hhplus.tdd.domain.UserLecture;
import id.hhplus.tdd.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lectures")
public class LectureController {

    @Autowired
    private LectureService lectureService;

    @PostMapping("/create")
    public Lecture createLecture(@RequestParam String title, @RequestParam Long limitCount) {
        return lectureService.createLecture(title, limitCount);
    }

    @PostMapping("/apply")
    public UserLecture registerUserToLecture(@RequestParam Long lectureId, @RequestParam Long userId) {
        return lectureService.registerUserToLecture(lectureId, userId);
    }

    @GetMapping("/application")
    public Boolean getUserLectureHistory(@RequestParam Long userId) {
        return lectureService.getUserLectureHistory(userId);
    }

    @GetMapping("/getLectures")
    public List<Lecture> getLectures() {
        return lectureService.getLectures();
    }
}
