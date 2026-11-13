package com.attendance.repository;

import com.attendance.model.User;
import com.attendance.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    List<User> findByRole(UserRole role);

    User update(User user);

    void delete(long id);

    boolean existsByUsername(String username);

    Optional<User> authenticate(String username, String password);
}
