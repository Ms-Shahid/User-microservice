package com.userservice.security.services;

import com.userservice.models.User;
import com.userservice.repository.UserRepository;
import com.userservice.security.models.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
