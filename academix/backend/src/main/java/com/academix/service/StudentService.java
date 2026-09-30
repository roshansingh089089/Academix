package com.academix.service;
import com.academix.dto.*;import com.academix.entity.Student;import com.academix.exception.NotFoundException;import com.academix.mapper.StudentMapper;import com.academix.repository.StudentRepository;import com.academix.specification.StudentSpecifications;
import lombok.RequiredArgsConstructor;import org.springframework.data.domain.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.util.*;
@Service @RequiredArgsConstructor public class StudentService {
 private final StudentRepository repository; private final StudentMapper mapper;
 @Transactional(readOnly=true) public PageResponse<StudentResponse> search(String q,Map<String,String> filters,int page,int size,String sort,String direction){int safe=Math.min(Math.max(size,1),100);Sort.Direction d="desc".equalsIgnoreCase(direction)?Sort.Direction.DESC:Sort.Direction.ASC;Page<StudentResponse> result=repository.findAll(StudentSpecifications.filters(q,filters),PageRequest.of(Math.max(page,0),safe,Sort.by(d,allowedSort(sort)))).map(mapper::toResponse);return PageResponse.from(result);}
 @Transactional(readOnly=true) public StudentResponse get(Long id){return mapper.toResponse(find(id));}
 @Transactional public StudentResponse create(StudentRequest r){return mapper.toResponse(repository.save(mapper.fromRequest(r)));}
 @Transactional public StudentResponse update(Long id,StudentRequest r){Student s=find(id);mapper.update(s,r);return mapper.toResponse(repository.save(s));}
 @Transactional public void delete(Long id){repository.delete(find(id));}
 @Transactional public int bulkUpdate(BulkUpdateRequest r){List<Student>s=repository.findAllById(r.ids());s.forEach(x->{if(r.status()!=null)x.setStatus(r.status());if(r.batch()!=null)x.setBatch(r.batch());if(r.course()!=null)x.setCourse(r.course());if(r.source()!=null)x.setSource(r.source());});repository.saveAll(s);return s.size();}
 @Transactional public int bulkDelete(List<Long> ids){return repository.deleteByIds(ids);}
 private Student find(Long id){return repository.findById(id).orElseThrow(()->new NotFoundException("Student not found: "+id));}
 private String allowedSort(String sort){return Set.of("id","studentCode","fullName","city","course","batch","status","createdAt").contains(sort)?sort:"createdAt";}
}
