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
        male, female, other;

        @com.fasterxml.jackson.annotation.JsonCreator
        public static Gender from(String s) {
            if (s == null || s.isEmpty()) return null;
            return Gender.valueOf(s.toLowerCase());
        }
    }

    public enum AgeGroup {
        _20s("20s"), _30s("30s"), _40s("40s"), _50plus("50+");
        
        private final String value;
        AgeGroup(String value) { this.value = value; }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() { return value; }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static AgeGroup from(String value) {
            for (AgeGroup ageGroup : AgeGroup.values()) {
                if (ageGroup.value.equals(value)) {
                    return ageGroup;
                }
            }
            return null;
        }
    }

    public enum Job {
        student, employee, business, freelancer, other;

        @com.fasterxml.jackson.annotation.JsonCreator
        public static Job from(String s) {
            if (s == null || s.isEmpty()) return null;
            try {
                return Job.valueOf(s.toLowerCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum MaritalStatus {
        single, married;

        @com.fasterxml.jackson.annotation.JsonCreator
        public static MaritalStatus from(String s) {
            if (s == null || s.isEmpty()) return null;
            try {
                return MaritalStatus.valueOf(s.toLowerCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
