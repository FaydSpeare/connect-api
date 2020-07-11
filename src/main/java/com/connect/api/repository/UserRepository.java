package com.connect.api.repository;

import com.connect.api.dto.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserRepository extends CrudRepository<User, Long> {

    @Query("SELECT userId FROM User WHERE username = :username")
    Long getUserWithUsername(@Param("username") String username);

    @Query("SELECT userId FROM User WHERE email = :email")
    Long getUserWithEmail(@Param("email") String email);

    @Query("SELECT username FROM User WHERE userId = :userId")
    String getUsername(@Param("userId") Long userId);

    @Query("SELECT email FROM User WHERE userId = :userId")
    String getEmail(@Param("userId") Long userId);

}
