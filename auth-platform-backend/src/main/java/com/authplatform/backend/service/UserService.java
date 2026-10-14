package com.authplatform.backend.service;

import com.authplatform.backend.dto.response.UserProfileResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    UserProfileResponse getUserProfile();
}
