package com.connect.api.repository;

import com.connect.api.dto.entity.Code;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface CodeRepository extends CrudRepository<Code, Long> {

    @Query("SELECT codeId FROM Code WHERE code = :code")
    Long getCode(@Param("code") String code);

}

