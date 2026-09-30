package com.academix.controller;
import com.academix.repository.StudentRepository;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/students/duplicates") @RequiredArgsConstructor public class DuplicateController {private final StudentRepository repository;@GetMapping public List<Map<String,Object>> phones(){return repository.duplicatePhones();}}
