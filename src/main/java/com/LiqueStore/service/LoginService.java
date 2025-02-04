package com.LiqueStore.service;

import com.LiqueStore.model.AccessRightModel;
import com.LiqueStore.model.CustomerModel;
import com.LiqueStore.model.EmployeeModel;
import com.LiqueStore.repository.AccessRightRepository;
import com.LiqueStore.repository.CustomerRepository;
import com.LiqueStore.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccessRightRepository accessRightRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<EmployeeModel> getUsersByUsername(String username) {
        return employeeRepository.getEmployeeByUsername(username);
    }

    public List<CustomerModel> getCustByUsername(String username) {
        return customerRepository.getCustomerByUsername(username);
    }

    public Boolean authenticateEmployee(String username, String password) {
        EmployeeModel userDetails = employeeRepository.findByUsername(username);
        return userDetails != null && passwordEncoder.matches(password, userDetails.getPassword());
    }

    public Boolean authenticateCustomer(String username, String password) {
        CustomerModel userDetails = customerRepository.findByUsername(username);
        return userDetails != null && passwordEncoder.matches(password, userDetails.getPassword());
    }

    public List<EmployeeModel> getEmployeesByAccessRightId(int accessrightid) {
        AccessRightModel accessRight = accessRightRepository.findById(accessrightid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access right ID: " + accessrightid));
        return employeeRepository.findByAccessRight(accessRight);
    }
}

