package com.example.cherrypickdemo.domain.user.controller;

import com.example.cherrypickdemo.domain.user.dto.request.LoginRequest;
import com.example.cherrypickdemo.domain.user.dto.request.SignupRequest;
import com.example.cherrypickdemo.domain.user.dto.request.UserDetailResponse;
import com.example.cherrypickdemo.domain.user.entity.User;
import com.example.cherrypickdemo.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    String baseURL  = "/api/v1/";
    String baseURL2 = "/api/v2/";

    // 회원가입
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest.getUsername(), signupRequest.getPassword());
    }

    // 로그인
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<UserDetailResponse> getUser(@PathVariable("user_id") Integer user_id) {

        return ResponseEntity.ok(userService.getUserDetail(user_id));
    }

}
