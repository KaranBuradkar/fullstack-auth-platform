package com.authplatform.backend.mapper;

import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toEntity(RegisterRequest request);

    AuthResponse toResponse(User user);
}
