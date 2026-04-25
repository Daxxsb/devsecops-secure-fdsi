package com.devsecops.unsecure.controller;

import com.devsecops.unsecure.model.User;
import com.devsecops.unsecure.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String loggedUser = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("username", loggedUser);
        model.addAttribute("role", isAdmin ? "ADMIN" : "USER");
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            List<User> allUsers = userRepository.findAllSafe();
            model.addAttribute("users", allUsers);
        }

        return "dashboard";
    }
}
