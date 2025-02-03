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
- [Spring Security](https://www.baeldung.com/spring-security-with-maven)
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

----
## OAuth 2.0

### The Purpose of OAuth

OAuth 2.0 is an industry-standard protocol for authorization, commonly used to grant access to third-party applications without exposing a user’s credentials. OAuth is especially useful when integrating third-party login options, such as "Login with Google" or "Login with GitHub."

- **Example**: Many websites allow users to log in through third-party services like Google or Facebook, instead of implementing their own login systems.

### How OAuth Works

OAuth defines four key participants in the authorization process:

1. **User**: The individual who wants to access a resource.
2. **Resource Server**: The server that holds the protected resources (e.g., Google’s email server for Gmail).
3. **Application**: The service that the user is trying to access (e.g., Scaler’s website).
4. **Authorization Server**: The server that handles the login process and

generates tokens (e.g., Google’s OAuth server).

#### OAuth Flow:

1. The user tries to access a resource on the **Application**.
2. The application redirects the user to the **Authorization Server** for login.
3. After login, the Authorization Server issues a token, which the user sends back to the application.
4. The application uses this token to request resources from the **Resource Server**.
5. The Resource Server validates the token, ensuring it is authentic (using the secret key or querying the Authorization Server), and grants access to the resources.

![OAuth Flow](https://d2beiqkhq929f0.cloudfront.net/public_assets/assets/000/088/683/original/2.png?1725537820)

### Implementing OAuth in UserService

- By default, it doesn't allow the request, because of the `@Bean` in securityConfig, as its trying to fetch user details from In memory db
```java
@Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
```
- Since, UserService have already `Bcrypt encoder`, we can provide as hashpassword & `.withDefaultPasswordEncoder()` can be removed by adding `.builder()`
- Spring, by default, builds the service's `Bcrypt encoder`, therefore the code looks like, 
```java
@Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder()
                .username("user")
                .password("$2a$12$0I51Mrt/zxUYO/N88Imp2.RzzksaBtM3X4H/VSNv5dPNh5w6SLJsa")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
```
- Where, the `password` its hashed using [Bcrypt-Generator.com - Online Bcrypt Hash Generator & Checker](https://bcrypt-generator.com/)

------
- Before, the application sends out the details of user to get validation from AuthServer like `Google AuthServer`, 
- The service which is requesting to validate the User/any details, needs to be authenticated/authorized by `Google AuthServer`, this can be done using
![img.png](img.png)
```java
@Bean
public RegisteredClientRepository registeredClientRepository() {
    RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("oidc-client")
            .clientSecret("$2a$12$mTmbGQI/HiOpP/DERAXe5uejFnJepvNs46RjS24YzbJBse3j3ImIO") //Bcrypted secret
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("https://oauth.pstmn.io/v1/callback")
            .postLogoutRedirectUri("https://oauth.pstmn.io/v1/callback")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .build();

    return new InMemoryRegisteredClientRepository(oidcClient);
}
```
- This `Bean` in our service security config, helps us to indicate that, our service is authenticated to connect to `Auth Server`
- The application which is requesting the user-identity from Auth-Server, will be authenticated via, `client_secret` & `client_id`

-----
### Integrating the OAuth with user-service & database layer

Known exception/errors 
1. ``Row size too large. The maximum row size for the used table type, not counting BLOBs, is 65535. This includes storage overhead, check the manual. You have to change some columns to TEXT or BLOBs``
- To fix this, add `@Lob` at the table, where row size is >255 varchar
2. [``No AuthenticationProvider found for org.springframework.security.authentication.UsernamePasswordAuthenticationToken``](https://github.com/spring-projects/spring-security/issues/13652#issuecomment-1683525778)
- To fix that, you have to create custom userDetails implementation, by override the `UserDetailsService` interface, 
- In the security.services - `CustomUserServiceDetails` implements `UserDetailsService`
```java
@Service
public class CustomUserServiceDetails implements UserDetailsService {

    private UserRepository userRepository;

    public CustomUserServiceDetails(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if( userOptional.isEmpty() )
            throw new UsernameNotFoundException("User with email: " + username + " doesn't exist");

        return new CustomUserDetails(userOptional.get());
    }
}
```
3. [``failed to lazily initialize a collection of role: com.userservice.models.User.roles: could not initialize proxy - no Session``](https://www.baeldung.com/hibernate-initialize-proxy-exception)
- This error occurs due to fetch type, whenever there is dependency of `@ManyToMany`, add explict of FetchType as `Eager`
