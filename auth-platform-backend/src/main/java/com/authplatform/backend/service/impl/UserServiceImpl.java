package com.authplatform.backend.service.impl;

import com.authplatform.backend.dto.response.UserProfileResponse;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserAlreadyExistsException;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.mapper.UserMapper;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileResponse getUserProfile() {

        log.atInfo()
                .addKeyValue("event", "USER_PROFILE_FETCH")
                .log("User profile fetch start");

        // 1. Get Current User
        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null) {
            log.atWarn()
                    .addKeyValue("event", "USER_PROFILE_FETCH")
                    .addKeyValue("reason", "UNAUTHENTIC_USER_ENTER")
                    .log("User profile fetch rejected");
            throw new UserAlreadyExistsException();
        }
        // 2. Extract email
        String email = authentication.getName();

        // 3. Fetch User from Database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.atWarn()
                            .addKeyValue("event", "USER_PROFILE_FETCH")
                            .addKeyValue("reason", "USER_NOT_FOUND")
                            .log("User profile fetch rejected");

                    return new UserNotFoundException();
                });

        log.atInfo()
                .addKeyValue("event", "USER_PROFILE_FETCH")
                .log("User profile fetched completed");

        // 4. Map entity to response dto and return
        return userMapper.toProfileResponse(user);
    }

}
