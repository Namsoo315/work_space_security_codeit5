package com.codeit.session.mapper;

import com.codeit.session.dto.user.UserCreateRequest;
import com.codeit.session.dto.user.UserDto;
import com.codeit.session.dto.user.UserUpdateRequest;
import com.codeit.session.dto.user.UserResponse;
import com.codeit.session.entity.Role;
import com.codeit.session.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserCreateRequest createRequest);

    UserDto toDto(User user);

    @Mapping(source = "role", target = "role")
    UserResponse toResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    default Role toRole(String role) {
        if (role == null) {
            return Role.USER;
        }
        return Role.valueOf(role.toUpperCase());
    }
}
