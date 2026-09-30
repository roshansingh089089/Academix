package com.academix.controller;
import com.academix.service.DashboardService;import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor public class DashboardController {private final DashboardService service;@GetMapping("/stats") public Map<String,Object> stats(){return service.stats();}}
