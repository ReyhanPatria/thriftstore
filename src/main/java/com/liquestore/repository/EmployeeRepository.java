package com.liquestore.repository;

import com.liquestore.model.AccessRight;
import com.liquestore.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Employee findByUsername(String username);

    Employee findByEmail(String email);

    List<Employee> findByAccessRight(AccessRight accessRight);

    List<Employee> getEmployeeByUsername(String username);
}
