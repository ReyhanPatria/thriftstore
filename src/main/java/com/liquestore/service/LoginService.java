package com.liquestore.service;

import com.liquestore.model.AccessRight;
import com.liquestore.model.Customer;
import com.liquestore.model.Employee;
import com.liquestore.repository.AccessRightRepository;
import com.liquestore.repository.CustomerRepository;
import com.liquestore.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Employee> getUsersByUsername(String username) {
        return employeeRepository.getEmployeeByUsername(username);
    }

    public List<Customer> getCustByUsername(String username) {
        return customerRepository.getCustomerByUsername(username);
    }

    public Boolean authenticateEmployee(String username, String password) {
        Employee userDetails = employeeRepository.findByUsername(username);
        return userDetails != null && passwordEncoder.matches(password, userDetails.getPassword());
    }

    public Boolean authenticateCustomer(String username, String password) {
        Customer userDetails = customerRepository.findByUsername(username);
        return userDetails != null && passwordEncoder.matches(password, userDetails.getPassword());
    }

    public List<Employee> getEmployeesByAccessRightId(int accessrightid) {
        AccessRight accessRight = accessRightRepository.findById(accessrightid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access right ID: " + accessrightid));
        return employeeRepository.findByAccessRight(accessRight);
    }
}

