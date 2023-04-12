package com.library.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.entity.Category;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.model.PasswordModel;
import com.library.model.RoleToUserModel;
import com.library.repository.UserRepository;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@EnableScheduling
@Slf4j
@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    //    GetAllUsers
//    @GetMapping("/users")
//    public ResponseEntity<List<User>> getUsers(){
//        return ResponseEntity.ok().body(userService.getUsers());
//    }
    @GetMapping("users")
    public String user(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("size", users.size());
        model.addAttribute("title", "Users");
        return "admin/users/user";
    }

    @RequestMapping(value = "/user/new")
    public String addForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("title", "Add new user");
        return "admin/users/user-add";
    }

    @PostMapping("user/add")
    public String addUser(@Validated @ModelAttribute("user")
                          User user, BindingResult result, Model model) {
        User existedUser = userService.findUserByEmail(user.getEmail());
        if (existedUser != null) {
            log.info("Cannot create this account.\n\tEmail has existed!");
            ResponseEntity.badRequest().body("Cannot create this account.\n\tEmail has existed!");

        } else {
            URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/add").toUriString());
            ResponseEntity.created(uri).body(userService.saveUser(user));
        }

        return "redirect:/users";
    }

    //    GetUserByUsername
    @GetMapping("/user/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        if (userService.getUser(username) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Username " + username + " is not existed !");
        } else {
            return ResponseEntity.ok().body(userService.getUser(username));
        }
    }

    //    GetUserByEmail
    @GetMapping("/user/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        if (userService.findUserByEmail(email) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " is not existed !");
        } else {
            return ResponseEntity.ok().body(userService.findUserByEmail(email));
        }
    }

    //    GetUserById
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserByID(@PathVariable Long id) {
        if (userRepository.findById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with id " + id + " is not existed !");
        } else {
            return ResponseEntity.ok().body(userRepository.findById(id));
        }
    }


    //    SaveUser
    @PostMapping("/user/save")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        //Check if existed user
        User existedUser = userService.findUserByEmail(user.getEmail());
        if (existedUser != null) {
            log.info("Cannot create this account.\n\tEmail has existed!");
            return ResponseEntity.badRequest().body("Cannot create this account.\n\tEmail has existed!");
        } else {
            URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
            return ResponseEntity.created(uri).body(userService.saveUser(user));
        }
    }

    //    SaveRole
    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    //    RoleAddToUser
    @PostMapping("/role/add-to-user")
    public String addRoleToUser(@RequestBody RoleToUserModel toUserModel) throws Exception {
        return userService.addRoleToUser(toUserModel.getEmail(), toUserModel.getRoleName());
    }

    //
    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);

                return ResponseEntity.ok().body(user);
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("access_message", exception.getMessage());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            //throw new RuntimeException("Access token is missing !");
            return ResponseEntity.ok().body(new RuntimeException("Access token is missing !"));
        }
        return ResponseEntity.ok().body("");
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);
                String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("access_message", exception.getMessage());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing !");
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        userRepository.delete(user);
        return "redirect:/users";
    }

    //    UpdateUser
    @PutMapping("/user/update")
    public ResponseEntity<?> updateUserByAdmin(@RequestParam("userId") Long userId,
                                               @RequestBody User user) {
        User userExisted = userRepository.findById(userId).get();
        if (userExisted != null) {
            userExisted = userService.updateUserById(userId, user);
            return ResponseEntity.ok(userExisted);
        } else {
            return ResponseEntity.badRequest().body("Cannot find User with Id " + userId);
        }
    }

    @PutMapping("/user/update-profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestBody User userRequest) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);

                if (user != null) {
                    user.setName(userRequest.getName());
                    user.setUsername(userRequest.getUsername());
                    user.setAvatar(userRequest.getAvatar());
                    user.setAddress(userRequest.getAddress());
                    user.setStatus(userRequest.getStatus());
                    user.setVirtualWallet(userRequest.getVirtualWallet());
                    userRepository.save(user);
                    return ResponseEntity.ok(user);
                } else {
                    return ResponseEntity.badRequest().body("Cannot find User with name " + username);
                }
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("access_message", exception.getMessage());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            return ResponseEntity.ok().body(new RuntimeException("Access token is missing !"));
        }
        return ResponseEntity.ok().body("");
    }


    //    UpdateVirtualWallet
    @PutMapping("/user/update-money")
    public ResponseEntity<?> updateMoneyUserByAdmin(@RequestParam("userId") Long userId,
                                                    @RequestBody User user) {
        User userExisted = userRepository.findById(userId).get();
        if (userExisted != null) {
            user.setVirtualWallet(userExisted.getVirtualWallet() + user.getVirtualWallet());
            System.out.println(user);
            userExisted = userService.updateUserById(userId, user);
            return ResponseEntity.ok(userExisted);
        } else {
            return ResponseEntity.badRequest().body("Cannot find User with Id " + userId);
        }
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
       /* String url =
                applicationUrl + "/savePassword?token="
                        + token;*/
        String url =
                applicationUrl + "/savePassword/"
                        + token;
        //sendVerificationEmail
        log.info("Click the link to Reset your password: {}",
                url);
        return url;
    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" + // http://locallhost:4200
                request.getServerName() +
                ":" +
                request.getServerPort() +
                "/api/user" +
                request.getContextPath();
    }

    private String applicationUrlClient(String request) {
        return request;
    }


}
