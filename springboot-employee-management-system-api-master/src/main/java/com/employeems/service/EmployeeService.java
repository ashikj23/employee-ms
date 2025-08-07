package com.employeems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employeems.model.Employee;
import com.employeems.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
   @Autowired
    private final UsersService usersService;

    public EmployeeService(EmployeeRepository employeeRepository, UsersService usersService) {
        this.employeeRepository = employeeRepository;
        this.usersService = usersService;
    }
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    public Employee saveEmployee(Employee employee) {
         employee.setHireDate(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    public boolean updateEmployee(Employee employee) {
        if (employeeRepository.existsById(employee.getId())) {
            employeeRepository.save(employee); 
            return true;
        }
        return false;
    }
    public boolean deleteEmployeeById(Integer id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
