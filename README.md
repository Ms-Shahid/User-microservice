## User Microservice 

### consists of 3 main models 
- BaseModel : id, isDeleted
- User : name, email, password
- Token : value/token, expiryAt
- Role : value

------------

#### In order to control the request & response of invoke requests, dto's, is being created
- signUpRequestDto & UserResponseDto
- LoginRequestDto
-----------

#### service
- currently, since this microservice consists of only userService, therefore, ``UserService`` is been added as ``class`` instead of `interface`
- Service consists of `/login`, `/signup` & `/validate{token}` functions

---------

#### More control over data between different services 
- As per standards, we shouldn't share all the details of one service to other, ( Eager loading )
- Sharing only the essential details when calling a service. 
```java
@PostMapping("/signup")
public UserResponseDto signUp(@RequestBody SignUpRequestDto signUpRequestDto){

        User user = userService.signUp(
                    signUpRequestDto.getName(),
                    signUpRequestDto.getEmail(),
                    signUpRequestDto.getPassword()
        );

        return UserResponseDto.from(user);
    }
```
- Here, from the requestDto, we have the control, to share which info to service. 
- Similarly, we can control what response data to be returned using ``UserResponseDto.from(user);``, converts user object to userDto

#### Authentication via Bcrypt encoder

> Note : if we want to add autowire/inject the dependency into our service, we should create a `Bean` as a `configuration`
```java
@Configuration
public class EncoderConfig {

    @Bean
    public BCryptPasswordEncoder getBCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```
- This has been used as dependency in ``UserService constrcutor`` 
- By default, `Spring-security` as dependency blocks every endpoint, and gives `401 Unauthorized` 
- For that we need to provide, another config, ``SecurityConfig``, permitting(` .anyRequest().permitAll()`) every requests by default
```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> {
                            try {
                                requests
                                        .anyRequest().permitAll()
                                        .and().cors().disable()
                                        .csrf().disable();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
        return http.build();
    }
}
```


  
