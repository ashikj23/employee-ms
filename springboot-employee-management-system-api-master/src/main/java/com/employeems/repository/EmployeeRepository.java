package com.employeems.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employeems.model.Employee;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.fullName = :#{#emp.fullName}, e.department = :#{#emp.department}, e.position = :#{#emp.position}, e.email = :#{#emp.email}, e.phone = :#{#emp.phone} WHERE e.id = :#{#emp.id}")
    void updateEmployee(@Param("emp") Employee emp);

}
