package com.userservice.service;

import com.userservice.exception.UserNotFoundException;
import com.userservice.models.Token;
import com.userservice.models.User;
import com.userservice.repository.TokenRepo;
import com.userservice.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenRepo tokenRepo;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                       TokenRepo tokenRepo){
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepo = tokenRepo;
    }

    public User signUp(String name, String email, String password) {

        //Validations
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        //user.setHashPassword(password); //By default once the BCryptPasswordEncoder dependency is set, this gives 401 status_code
        user.setHashPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(user);
    }


    public Token login(String email, String password) throws UserNotFoundException {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if( optionalUser.isEmpty() ){
            throw new UserNotFoundException("User with Email " + email + " Not found");
        }

        User user = optionalUser.get();

        if(!bCryptPasswordEncoder.matches(password, user.getHashPassword())){
            throw new UserNotFoundException("User Email and password are not matching");
        }

        Token token = generateToken(user);
        return tokenRepo.save(token);

    }

    private Token generateToken(User user){
        Token token = new Token();
        token.setValue(RandomStringUtils.randomAlphanumeric(10)); //10 alpha-numeric
        token.setExpiryAt(System.currentTimeMillis() + 60000); //60 ms
        token.setUser(user);
        return token;
    }
    //Self validating Token
     /*
        A token is valid if
        1. Token Exist in DB
        2. Token has not expired
        3. Token has not marked as deleted
      */
    public User validateToken(String token) {
        Optional<Token> tokenResult = tokenRepo
                .findByValueAndDeletedExpiryAtGreaterThan(token, false, System.currentTimeMillis());

        return tokenResult.map(Token::getUser).orElse(null);
    }


    public void logout(String token) {

        Optional<Token> optionalToken = tokenRepo.findByValueAndDeletedEquals(token, false);

        if( optionalToken.isEmpty() ) return;

        Token tokenToBeRemoved = optionalToken.get();
        tokenToBeRemoved.setDeleted(true);

        tokenRepo.save(tokenToBeRemoved);

    }
}
