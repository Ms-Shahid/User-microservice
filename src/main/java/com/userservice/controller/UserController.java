package com.userservice.controller;

import com.userservice.dtos.LoginRequestDto;
import com.userservice.dtos.SignUpRequestDto;
import com.userservice.dtos.UserResponseDto;
import com.userservice.exception.UserNotFoundException;
import com.userservice.models.Token;
import com.userservice.models.User;
import com.userservice.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponseDto signUp(@RequestBody SignUpRequestDto signUpRequestDto){

        User user = userService.signUp(
                    signUpRequestDto.getName(),
                    signUpRequestDto.getEmail(),
                    signUpRequestDto.getPassword()
        );

        return UserResponseDto.from(user);
    }

    @PostMapping("/login")
    public Token login(@RequestBody LoginRequestDto loginRequestDto) throws UserNotFoundException {
        return userService.login(
                loginRequestDto.getEmail(), loginRequestDto.getPassword()
        );
    }

    @GetMapping("/validate/{token}")
    public UserResponseDto validate(@PathVariable String token){
        User user = userService.validateToken(token);
        return UserResponseDto.from(user);
    }
}
