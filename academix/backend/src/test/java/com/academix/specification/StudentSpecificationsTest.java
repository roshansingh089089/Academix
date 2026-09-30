package com.academix.specification;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class StudentSpecificationsTest {@Test void createsComposableSpecification(){assertNotNull(StudentSpecifications.filters("Singh",java.util.Map.of("city","Bangalore","status","ACTIVE")));}}
