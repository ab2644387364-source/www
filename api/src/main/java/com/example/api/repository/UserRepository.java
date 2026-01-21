package com.example.api.repository;

import com.example.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    User findUserByEmailAndPassword(String email, String password);

    User findByEmail(String email);

}
