package com.ssafy.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String profileImage;
    
    @Builder.Default
    private Role role = Role.ROLE_USER;
    
    @Builder.Default
    private SocialType socialType = SocialType.NONE;
    
    private String socialId;
    
    @Builder.Default
    private boolean isEmailVerified = false;
    
    private Gender gender;
    private AgeGroup ageGroup;
    private Job job;
    private MaritalStatus maritalStatus;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }

    public enum SocialType {
        NONE, KAKAO, NAVER, GOOGLE
    }

    public enum Gender {
        male, female, other
    }

    public enum AgeGroup {
        _20s("20s"), _30s("30s"), _40s("40s"), _50plus("50+");
        
        private final String value;
        AgeGroup(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public enum Job {
        student, employee, business, freelancer, other
    }

    public enum MaritalStatus {
        single, married
    }
}
