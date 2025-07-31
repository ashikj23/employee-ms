package com.employeems.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.employeems.dto.ChangeOwnPasswordRequest;
import com.employeems.dto.ChangePasswordRequest;
import com.employeems.dto.UserDTO;
import com.employeems.model.Users;
import com.employeems.repository.UsersRepository;

@Service
public class UsersService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_NAME_EXIST = "Username already exist";
    private static final String PASSWORD_SHOULD_CONTAIN_MORE_CHARACTER = "Password must be at least 6 characters";

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users registerUser(Users user) {
        if (usersRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException(USER_NAME_EXIST);
        } else if (user.getPassword().length() < 6) {
            throw new RuntimeException(PASSWORD_SHOULD_CONTAIN_MORE_CHARACTER);
        }
        user.setCreatedAt(LocalDateTime.now());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        return usersRepository.save(user);
    }

    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    public boolean login(String username, String rawPassword) {
        return usersRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public List<UserDTO> getAllUsers() {
        List<Users> users = usersRepository.findAll();

        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

    public boolean deleteUserById(Integer id) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public String changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        Users user = usersRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);

        usersRepository.save(user);
        return "Password updated successfully";
    }

    public String changeOwnPassword(ChangeOwnPasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException(PASSWORD_SHOULD_CONTAIN_MORE_CHARACTER);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        return "Password successfully updated for " + username;
    }
}
