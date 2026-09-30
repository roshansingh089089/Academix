package com.academix.controller;
import com.academix.dto.*;import com.academix.service.StudentService;import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/students") @RequiredArgsConstructor public class StudentController {
 private final StudentService service;
 @GetMapping public PageResponse<StudentResponse> search(@RequestParam(required=false)String q,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size,@RequestParam(defaultValue="createdAt")String sort,@RequestParam(defaultValue="desc")String direction,@RequestParam Map<String,String> all){return service.search(q,all,page,size,sort,direction);}
 @GetMapping("/{id}") public StudentResponse get(@PathVariable Long id){return service.get(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public StudentResponse create(@Valid @RequestBody StudentRequest r){return service.create(r);}
 @PutMapping("/{id}") public StudentResponse update(@PathVariable Long id,@Valid @RequestBody StudentRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.delete(id);}
 @PostMapping("/bulk-update") public Map<String,Integer> bulkUpdate(@Valid @RequestBody BulkUpdateRequest r){return Map.of("updated",service.bulkUpdate(r));}
 @PostMapping("/bulk-delete") public Map<String,Integer> bulkDelete(@RequestBody List<Long> ids){return Map.of("deleted",service.bulkDelete(ids));}
}
