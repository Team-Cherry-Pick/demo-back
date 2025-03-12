package com.example.cherrypickdemo.domain.user.service;

import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 회원가입
    public String signup(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            return "이미 존재하는 유저 ID 입니다.";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userRepository.save(user);
        return "회원가입 성공";
    }

    // 로그인
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return "존재하지 않는 유저입니다.";
        }

        if (user.getPassword().equals(password)) {
            return "로그인 성공";
        }

        return "잘못된 비밀번호입니다.";
    }
}
