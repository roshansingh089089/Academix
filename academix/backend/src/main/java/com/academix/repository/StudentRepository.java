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
}
