package id.hhplus.tdd.service;

import id.hhplus.tdd.domain.User;
import id.hhplus.tdd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User createUser(String name) {
        User user = User.builder()
                .name(name)
                .build();
        return userRepository.save(user);
    }
}
