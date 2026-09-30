package com.academix.controller;
import com.academix.dto.*;import com.academix.service.StudentService;import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;import java.time.LocalDate;import java.util.*;
@RestController @RequestMapping("/api/students") @RequiredArgsConstructor public class StudentController {
 private final StudentService service;
 @GetMapping public PageResponse<StudentResponse> search(@RequestParam(required=false)String q,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size,@RequestParam(defaultValue="createdAt")String sort,@RequestParam(defaultValue="desc")String direction,@RequestParam Map<String,String> all){return service.search(q,all,page,size,sort,direction);}
 @GetMapping("/filter-options") public Map<String,List<String>> filterOptions(@RequestParam(required=false)String state){return service.filterOptions(state);}
 @GetMapping(value="/export",produces="text/csv") public ResponseEntity<StreamingResponseBody> export(@RequestParam(required=false)String q,@RequestParam(defaultValue="createdAt")String sort,@RequestParam(defaultValue="desc")String direction,@RequestParam Map<String,String> all){String filename="academix-students-"+LocalDate.now()+".csv";StreamingResponseBody body=output->service.exportCsv(q,all,sort,direction,output);return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+filename+"\"").body(body);}
 @GetMapping("/{id}") public StudentResponse get(@PathVariable Long id){return service.get(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public StudentResponse create(@Valid @RequestBody StudentRequest r){return service.create(r);}
 @PutMapping("/{id}") public StudentResponse update(@PathVariable Long id,@Valid @RequestBody StudentRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.delete(id);}
 @PostMapping("/bulk-update") public Map<String,Integer> bulkUpdate(@Valid @RequestBody BulkUpdateRequest r){return Map.of("updated",service.bulkUpdate(r));}
 @PostMapping("/bulk-delete") public Map<String,Integer> bulkDelete(@RequestBody List<Long> ids){return Map.of("deleted",service.bulkDelete(ids));}
}
