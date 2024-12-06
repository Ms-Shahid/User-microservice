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


### Integrating UserService <-> ProductService

> Problem Statement : 
trigger the userService for generating token, use this token in productService header & trigger the userService to validate this token for getting product details

- As per the PS, in the logic to `/validate` is in userService(AuthServer), the changes in `productService`, 
 ```java
@GetMapping("/validate/{id}")
public Product validateTokenAndGetProduct( @RequestHeader("Token") String token,
    @PathVariable("id") Long id) throws ProductNotFoundException {
    if(!tokenService.validateToken(token)){
        throw new UnknownAccessTypeException("User is not authorized");
    }
    Product product = productService.getProductById(id);
    ResponseEntity<Product> productResponseEntity = new ResponseEntity<>(product, HttpStatus.OK);
    return productResponseEntity.getBody();
}
```

- In tokenService, we are calling ``UserService`` as Rest call
```java
public boolean validateToken(String token){
        UserResponseDto userResponseDto = restTemplate.getForObject(
                "http://localhost:8080/user/validate/" + token, UserResponseDto.class
        );
        System.out.println("Ping.... " + userResponseDto);

        return userResponseDto != null
                && !userResponseDto.getEmail().isEmpty()
                && !userResponseDto.getName().isEmpty();

    }
``` 

### API Contract
- `Swagger` API contract is been shared by services, that describes the api's that are accessed & shared between these microservices.