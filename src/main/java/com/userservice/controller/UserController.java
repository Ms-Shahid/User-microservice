package com.userservice.controller;

import com.userservice.dtos.LoginRequestDto;
import com.userservice.dtos.LogoutRequestDto;
import com.userservice.dtos.SignUpRequestDto;
import com.userservice.dtos.UserResponseDto;
import com.userservice.exception.UserNotFoundException;
import com.userservice.models.Token;
import com.userservice.models.User;
import com.userservice.service.UserService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
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
    public ResponseEntity<UserResponseDto> validate(@PathVariable("token") @NonNull String token){
        System.out.println("Call is getting triggered");
        User user = userService.validateToken(token);
        if( user == null ) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(LogoutRequestDto requestDto){
        userService.logout(requestDto.getToken());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
