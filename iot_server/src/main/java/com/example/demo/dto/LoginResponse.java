package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String phone;
        private String nikeName;
        private String address;
        private String icon;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getPhone(),
                    user.getNikeName(),
                    user.getAddress(),
                    user.getIcon()
            );
        }
    }
}

