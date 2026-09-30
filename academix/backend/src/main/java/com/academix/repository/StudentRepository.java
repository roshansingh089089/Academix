package com.academix.repository;
import com.academix.entity.Student;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface StudentRepository extends JpaRepository<Student,Long>,JpaSpecificationExecutor<Student> {
 Optional<Student> findByStudentCode(String code);
 List<Student> findAllByStudentCodeIn(Collection<String> codes);
 @Modifying @Query("delete from Student s where s.id in :ids") int deleteByIds(@Param("ids") Collection<Long> ids);
 @Query(value="select phone as key, count(*) as count from students where phone is not null and phone<>'' group by phone having count(*)>1 order by count(*) desc limit 100",nativeQuery=true) List<Map<String,Object>> duplicatePhones();
 @Query(value="select min(trim(course)) from students where course is not null and trim(course)<>'' group by lower(trim(course)) order by lower(trim(course))",nativeQuery=true) List<String> distinctCourses();
 @Query(value="select min(trim(class_name)) from students where class_name is not null and trim(class_name)<>'' group by lower(trim(class_name)) order by lower(trim(class_name))",nativeQuery=true) List<String> distinctClasses();
 @Query(value="select min(trim(state)) from students where state is not null and trim(state)<>'' group by lower(trim(state)) order by lower(trim(state))",nativeQuery=true) List<String> distinctStates();
 @Query(value="select min(trim(city)) from students where city is not null and trim(city)<>'' and (:state is null or trim(:state)='' or lower(trim(state)) like '%'||lower(trim(:state))||'%') group by lower(trim(city)) order by lower(trim(city))",nativeQuery=true) List<String> distinctCities(@Param("state") String state);
 @Query(value="select min(trim(batch)) from students where batch is not null and trim(batch)<>'' group by lower(trim(batch)) order by lower(trim(batch))",nativeQuery=true) List<String> distinctBatches();
 @Query(value="select min(trim(status)) from students where status is not null and trim(status)<>'' group by lower(trim(status)) order by lower(trim(status))",nativeQuery=true) List<String> distinctStatuses();
 @Query(value="select min(trim(source)) from students where source is not null and trim(source)<>'' group by lower(trim(source)) order by lower(trim(source))",nativeQuery=true) List<String> distinctSources();
}
