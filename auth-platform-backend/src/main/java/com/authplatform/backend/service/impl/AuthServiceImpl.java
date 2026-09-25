package com.authplatform.backend.service.impl;

import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.UserAlreadyExistException;
import com.authplatform.backend.mapper.UserMapper;
import com.authplatform.backend.repository.UserRepository;
import com.authplatform.backend.service.AuthService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user != null) {
            log.debug("User Already exist");
            throw new UserAlreadyExistException("User already Exist");
        }
        user = userMapper.toEntity(request);
//        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
}
