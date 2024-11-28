package com.gathering.user.model.dto;

import com.gathering.user.model.dto.response.UserResponseDto;
import com.gathering.user.model.entitiy.User;
import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UserDto {


    private long usersId;
    private String userName;
    private String email;
    private String profile;
    private String roles; // USER, ADMIN

    // Entity -> DTO 변환 메서드
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .usersId(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .roles(user.getRoles())
                .build();
    }
}
