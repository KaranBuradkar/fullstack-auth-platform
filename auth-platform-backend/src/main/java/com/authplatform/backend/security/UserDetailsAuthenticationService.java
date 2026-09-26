package com.authplatform.backend.security;

import com.authplatform.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service(value = "authUserDetailsService")
public class UserDetailsAuthenticationService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsAuthenticationService.class);
    private final UserRepository userRepository;

    public UserDetailsAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("Invalid username");
                    return new UsernameNotFoundException("Invalid username");
                });
    }
}
