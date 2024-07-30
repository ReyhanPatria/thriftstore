package com.liquestore.repository;

import com.liquestore.model.AccessRight;
import com.liquestore.model.EmployeeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeModel, Integer> {
    EmployeeModel findByUsername(String username);

    EmployeeModel findByEmail(String email);

    List<EmployeeModel> findByAccessRight(AccessRight accessRight);

    List<EmployeeModel> getEmployeeByUsername(String username);
}
