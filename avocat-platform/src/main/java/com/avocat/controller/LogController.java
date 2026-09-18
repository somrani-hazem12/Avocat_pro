package com.avocat.controller;

import com.avocat.entity.Log;
import com.avocat.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class LogController {

    private final LogRepository logRepository;

    @GetMapping
    public List<Log> getAll() {
        return logRepository.findAllByOrderByDateActionDesc();
    }
}