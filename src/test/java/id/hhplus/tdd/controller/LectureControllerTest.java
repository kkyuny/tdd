package id.hhplus.tdd.controller;

import id.hhplus.tdd.domain.Lecture;
import id.hhplus.tdd.domain.User;
import id.hhplus.tdd.domain.UserLecture;
import id.hhplus.tdd.repository.LectureRepository;
import id.hhplus.tdd.repository.UserLectureRepository;
import id.hhplus.tdd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockHttpServletRequestDsl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LectureControllerTest {

    /*
        요구사항 정리
        1. 특강 신청
         - 동일한 신청자는 한 번의 수강 신청만 성공할 수 있다.
         - 각 강의는 선착순 30명만 신청 가능(초과시 요청 실패)
         - 어떤 유저가 특강을 신청했는지 히스토리 저장

        2. 날짜별로 특강이 존재할 수 있는 범용적인 서비스화
         - 특강 신청하기 전 목록을 조회해볼 수 있다.

        3. 특강 신청 완료 여부 조회
         - 신청자의 신청여부 조회

     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LectureRepository lecturesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLectureRepository userLectureRepository;

    private Lecture lecture;
    private List<User> users;

    @BeforeEach
    void setUp() {
        // Clear repositories
        userLectureRepository.deleteAll();
        lecturesRepository.deleteAll();
        userRepository.deleteAll();

        // Initialize test data
        lecture = lecturesRepository.save(Lecture.builder()
                .title("강의_1")
                .limitCount(30L)
                .build());

        users = IntStream.range(0, 50)
                .mapToObj(i -> userRepository.save(User.builder()
                        .name("User_" + i)
                        .build()))
                .collect(Collectors.toList());
    }

    @Test
    void testCreateLecture() throws Exception {
        // When
        ResultActions result = mockMvc.perform(post("/lectures/create")
                .param("title", "강의_1")
                .param("limitCount", "30")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("강의_1"))
                .andExpect(jsonPath("$.limitCount").value(30));
    }

    @Test
    void testCreateUser() throws Exception {
        // When
        ResultActions result = mockMvc.perform(post("/user/create")
                .param("name", "김항해")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("김항해"));
    }

    @Test
    void testGetAllLectures() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/lectures/getLectures")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("강의_1"))
                .andExpect(jsonPath("$[0].limitCount").value(30));
    }

    @Test
    void testRegisterUserToLectureNotExistUser() throws Exception {

        // Given
        Long lectureId = lecture.getId();
        Long userId = 100L; // 없음
        // When
        ResultActions result = mockMvc.perform(post("/lectures/apply")
                .param("lectureId", lectureId.toString())
                .param("userId", userId.toString()) // 없음
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(result1 -> assertEquals("Not exist user:100", result1.getResolvedException().getMessage()));
    }

    @Test
    void testRegisterUserToLectureNotExistLecture() throws Exception {
        // Given
        Long lectureId = 100L; // 없음
        User user = users.get(0);

        // When
        ResultActions result = mockMvc.perform(post("/lectures/apply")
                .param("lectureId", lectureId.toString()) // 없음
                .param("userId", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(result1 -> assertEquals("Not exist lecture:100", result1.getResolvedException().getMessage()));
    }

    @Test
    void testRegisterUserToLecture() throws Exception {
        // Given
        User user = users.get(0);

        // When
        ResultActions result = mockMvc.perform(post("/lectures/apply")
                .param("lectureId", lecture.getId().toString())
                .param("userId", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.lecture.id").value(lecture.getId()));
    }

    @Test
    void testConcurrencyRegistration() throws Exception {
        // Given
        int numberOfThreads = 40;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    User user = users.get(index);
                    ResultActions result = mockMvc.perform(post("/lectures/apply")
                            .param("lectureId", lecture.getId().toString())
                            .param("userId", user.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON));

                    if (index < 31) {
                        result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.user.id").value(user.getId()))
                                .andExpect(jsonPath("$.lecture.id").value(lecture.getId()))
                                .andExpect(jsonPath("$.registerResult").value(true));
                    } else {
                        result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.registerResult").value(false));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        for (int i = 0; i < 30; i++) {
            User user = users.get(i);
            ResultActions result = mockMvc.perform(get("/lectures/application")
                    .param("userId", user.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        for (int i = 30; i < 40; i++) {
            User user = users.get(i);
            ResultActions result = mockMvc.perform(get("/lectures/application")
                    .param("userId", user.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Test
    void testGetUserLectureHistory() throws Exception {
        // Given
        User user = users.get(0);
        UserLecture userLecture = new UserLecture(lecture, user, true);
        userLectureRepository.save(userLecture);

        // When
        ResultActions result = mockMvc.perform(get("/lectures/application")
                .param("userId", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().string(("true")));
    }

    @Test
    void testGetUserLectureHistoryNotExistLecture() throws Exception {
        // Given
        User user =  users.get(0);

        /* 강의등록 안함
         UserLecture userLecture = new UserLecture(lecture, user);
         userLectureRepository.save(userLecture);
        */

        // When
        ResultActions result = mockMvc.perform(get("/lectures/application")
                .param("userId", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().string(("false")));
    }
}
