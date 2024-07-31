package com.liquestore.controller;

import com.liquestore.dto.Response;
import com.liquestore.model.AccessRight;
import com.liquestore.model.Customer;
import com.liquestore.model.Employee;
import com.liquestore.model.TemporaryOrder;
import com.liquestore.repository.CustomerRepository;
import com.liquestore.repository.EmployeeRepository;
import com.liquestore.repository.TemporaryOrderRepository;
import com.liquestore.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/backend")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    private final CustomerRepository customerRepository;
    private final TemporaryOrderRepository temporaryOrderRepository;
    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String orderid) {
        Customer getCustData = customerRepository.findByUsername(username);
        TemporaryOrder temporaryOrder = temporaryOrderRepository.findByOrderid(orderid);
        if (getCustData != null && getCustData.getAccessRightId().getId() == 4) {
            log.info("data customer ada");
            Customer getCustomer = customerRepository.findByUsername(username);
            boolean cekPhoneOrder = false;
            if (temporaryOrder != null) {
                if (getCustomer.getPhoneNumber().equals(temporaryOrder.getPhoneNumber())) {
                    cekPhoneOrder = true;
                }
            }
            boolean isAuthenticated = loginService.authenticateCustomer(username, password);
            if (isAuthenticated) {
                log.info("username ada");
                Map<String, Object> response = new HashMap<>();
                response.put("customer", getCustomer);
                response.put("cekPhoneOrder", cekPhoneOrder);
                return ResponseEntity.ok(response);
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid username or password");
            }
        }
        else {
            Employee getEmployee = employeeRepository.findByUsername(username);
            boolean isAuthenticated = loginService.authenticateEmployee(username, password);
            if (isAuthenticated) {
                log.info("yes");
                return ResponseEntity.ok(getEmployee);
            }
            else {
                log.info("no");
                return ResponseEntity.badRequest().body("invalid username or password");
            }
        }
    }

    @PostMapping("/register")
    ResponseEntity<?> register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("usernameIG") String usernameIG,
            @RequestParam("phonenumber") String phonenumber,
            @RequestParam("birthdate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date birthdate) {
        Customer existingUsername = customerRepository.findByUsername(username);
        if (existingUsername != null) {
            return ResponseEntity.badRequest()
                    .body(Response.builder()
                            .message("Username sudah digunakan")
                            .build());
        }
        Customer existingPhoneNumber = customerRepository.findByPhonenumber(phonenumber);
        if (existingPhoneNumber != null) {
            return ResponseEntity.badRequest()
                    .body(Response.builder()
                            .message("Phone Number sudah digunakan")
                            .build());
        }
        Customer addCustomer = new Customer();
        addCustomer.setUsername(username);
        addCustomer.setPassword(passwordEncoder.encode(password));
        addCustomer.setName(name);
        addCustomer.setEmail(email);
        addCustomer.setUsernameIg(usernameIG);
        addCustomer.setPhoneNumber(phonenumber);
        addCustomer.setBirthdate(birthdate);
        AccessRight accessRight = AccessRight.builder()
                .id(4)
                .build();
        addCustomer.setAccessRightId(accessRight);
        addCustomer.setStatus("active");
        customerRepository.save(addCustomer);
        log.info(String.valueOf(addCustomer));
        return ResponseEntity.ok(Response.builder()
                .message("berhasil register")
                .build());
    }

    @GetMapping("/getNomorWa")
    public ResponseEntity<?> getNomorWa(@RequestParam(name = "orderid") String orderid) {
        TemporaryOrder temporaryOrder = temporaryOrderRepository.findByOrderid(orderid);
        log.info(orderid);
        return ResponseEntity.ok(temporaryOrder);
    }
}
