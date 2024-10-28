package com.sparta.ssetest.repository;

import com.sparta.ssetest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
