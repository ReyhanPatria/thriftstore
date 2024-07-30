package com.LiqueStore.repository;

import com.LiqueStore.model.AccessRightModel;
import com.LiqueStore.model.EmployeeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeModel, Integer> {
    EmployeeModel findByUsername(String username);

    EmployeeModel findByEmail(String email);

    List<EmployeeModel> findByAccessRight(AccessRightModel accessRight);

    List<EmployeeModel> getEmployeeByUsername(String username);
}
