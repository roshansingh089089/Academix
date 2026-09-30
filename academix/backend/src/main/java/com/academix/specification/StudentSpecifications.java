package com.academix.specification;
import com.academix.entity.Student;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.*;
public final class StudentSpecifications {
 private StudentSpecifications(){}
 public static Specification<Student> filters(String q,Map<String,String> f){ return (root,query,cb)->{
   List<Predicate> p=new ArrayList<>();
   if(q!=null&&!q.isBlank()){String x="%"+q.toLowerCase().trim()+"%";p.add(cb.or(cb.like(cb.lower(root.get("fullName")),x),cb.like(cb.lower(root.get("studentCode")),x),cb.like(root.get("phone"),x),cb.like(root.get("alternatePhone"),x),cb.like(cb.lower(root.get("email")),x),cb.like(root.get("guardianPhone"),x)));}
   for(String k:List.of("gender","city","district","state","schoolName","className","course","batch","stream","academicYear","status","source")){String v=f.get(k);if(v!=null&&!v.isBlank())p.add(cb.equal(cb.lower(root.get(k)),v.toLowerCase()));}
   if(f.get("ageFrom")!=null&&!f.get("ageFrom").isBlank())p.add(cb.ge(root.get("age"),Integer.valueOf(f.get("ageFrom"))));
   if(f.get("ageTo")!=null&&!f.get("ageTo").isBlank())p.add(cb.le(root.get("age"),Integer.valueOf(f.get("ageTo"))));
   if(f.get("admissionFrom")!=null&&!f.get("admissionFrom").isBlank())p.add(cb.greaterThanOrEqualTo(root.get("admissionDate"),LocalDate.parse(f.get("admissionFrom"))));
   if(f.get("admissionTo")!=null&&!f.get("admissionTo").isBlank())p.add(cb.lessThanOrEqualTo(root.get("admissionDate"),LocalDate.parse(f.get("admissionTo"))));
   return cb.and(p.toArray(Predicate[]::new));
 };}
}
