package com.employeems.controller;

import static com.employeems.controller.UsersController.getResponseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeems.dto.ChangeOwnPasswordRequest;
import com.employeems.dto.ChangePasswordRequest;
import com.employeems.model.Employee;
import com.employeems.model.Users;
import com.employeems.sercurity.JwtUtil;
import com.employeems.service.EmployeeService;
import com.employeems.service.UsersService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsersService usersService;
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;

    public AdminController(UsersService usersService, JwtUtil jwtUtil, EmployeeService employeeService) {
        this.usersService = usersService;
        this.jwtUtil = jwtUtil;
        this.employeeService = employeeService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/changeUserPassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            usersService.changePassword(request);
            return ResponseEntity.ok("Password for user '" + request.getUsername() + "' was updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/changeOwnPassword")
    public ResponseEntity<?> changeOwnPassword(@RequestBody ChangeOwnPasswordRequest request) {
        try {
            String message = usersService.changeOwnPassword(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/adminDashboard")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome, Admin! You have full access.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        return getResponseEntity(usersService);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        boolean deleted = usersService.deleteUserById(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with this ID was not found.");
        }
    }

    /*
    ----------------------- Employees
     */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addEmployee")
    public ResponseEntity<?> createEmployee(@RequestBody @Valid Employee employee) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    
                    String username = authentication.getName();
                    Users creator = usersService.findByUsername(username);
                    employee.setCreatedBy(creator);

                    Employee savedEmployee = employeeService.saveEmployee(employee);
                    return ResponseEntity.ok("Employee Added successfully" + savedEmployee.getId());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You are not authorized to perform this action.");
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You are not logged in.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/allEmployee")
    public ResponseEntity<?> getAllEmployees() {
        return getResponseEntity(employeeService);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateEmployee")
    public ResponseEntity<?> updateEmployee(@RequestBody Employee employee) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    boolean updated = employeeService.updateEmployee(employee);
                    if (updated) {
                        return ResponseEntity.ok("Employee updated successfully.");
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Employee with this ID was not found.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You are not authorized to perform this action.");
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You are not logged in.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    boolean deleted = employeeService.deleteEmployeeById(id);
                    if (deleted) {
                        return ResponseEntity.ok("Employee deleted successfully.");
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Employee with this ID was not found.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You are not authorized to perform this action.");
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
