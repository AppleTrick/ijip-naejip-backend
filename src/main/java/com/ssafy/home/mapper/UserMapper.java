package com.ssafy.home.mapper;

import com.ssafy.home.dto.User;
import org.apache.ibatis.annotations.Mapper;


import java.util.Optional;

@Mapper
public interface UserMapper {
    void save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    void update(User user);
    void delete(Long id);
}
