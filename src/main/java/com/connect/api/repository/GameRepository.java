package com.connect.api.repository;

import com.connect.api.dto.entity.Game;
import com.connect.api.dto.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface GameRepository extends CrudRepository<Game, Long> {

    @Query("SELECT COUNT(g) FROM Game g WHERE g.playerOne = :user OR g.playerTwo = :user")
    Integer getPastGameCount(@Param("user") User user);

}
