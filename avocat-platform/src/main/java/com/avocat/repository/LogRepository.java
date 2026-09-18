package com.avocat.repository;

import com.avocat.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findAllByOrderByDateActionDesc();
}