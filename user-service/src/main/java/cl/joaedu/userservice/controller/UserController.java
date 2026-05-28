package cl.joaedu.userservice.controller;

import cl.joaedu.userservice.dto.UserRequest;
import cl.joaedu.userservice.dto.UserResponse;
import cl.joaedu.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        UserResponse mockUser = new UserResponse(
                1L,
                "Joaquin Sandoval",
                "joaquin@gymflow.cl",
                "PREMIUM",
                "ROLE_ADMIN"
        );
        return List.of(mockUser);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}