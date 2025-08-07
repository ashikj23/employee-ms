package com.employeems.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeems.dto.ChangeOwnPasswordRequest;
import com.employeems.dto.UserDTO;
import com.employeems.model.Employee;
import com.employeems.service.EmployeeService;
import com.employeems.service.UsersService;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;
    private final EmployeeService employeeService;

    public UsersController(UsersService usersService, EmployeeService employeeService) {
        this.usersService = usersService;
        this.employeeService = employeeService;
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/forgotPassword")
    public ResponseEntity<?> changeOwnPassword(@RequestBody ChangeOwnPasswordRequest request) {
        try {
            String message = usersService.changeOwnPassword(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/userDashboard")
    public ResponseEntity<String> userDashboard() {
        return ResponseEntity.ok("Welcome, User! You can view this content.");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/allEmployee")
    public ResponseEntity<?> getAllEmployees() {
        return getResponseEntity(employeeService);
    }

    static ResponseEntity<?> getResponseEntity(EmployeeService employeeService) {
        List<Employee> allEmployees = employeeService.getAllEmployees();
        Map<String, Object> response = new HashMap<>();
        response.put("count", allEmployees.size());
        response.put("employees", allEmployees); 
        return ResponseEntity.ok(response);
    }

    static ResponseEntity<?> getResponseEntity(UsersService usersService) {
        List<UserDTO> userDTOs = usersService.getAllUsers();
        Map<String, Object> map = new HashMap<>();
        map.put("count", userDTOs.size());
        map.put("Users", userDTOs);
        return ResponseEntity.ok(map);
    }
}