package com.devsecops.unsecure.controller;

import com.devsecops.unsecure.model.User;
import com.devsecops.unsecure.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controlador de autenticacion.
 *
 * VULNERABILIDADES PRESENTES (intencionales para demo DevSecOps):
 *
 * #3 - SIN RATE LIMITING (OWASP Top 10 - A07:2021 Identification & Authentication Failures)
 *      No hay limite de intentos de login fallidos.
 *      Un atacante puede hacer fuerza bruta ilimitada contra el endpoint /login.
 *      Demostrable con un script Python o Burp Suite Intruder.
 *      FIX en arquitectura secure: Spring Security con bloqueo de cuenta tras N intentos.
 *
 * #4 - SIN PROTECCION CSRF (OWASP Top 10 - A01:2021 Broken Access Control)
 *      El formulario no valida tokens CSRF.
 *      Un atacante puede engañar a un usuario autenticado para que envie requests maliciosos.
 *      FIX en arquitectura secure: Spring Security habilita CSRF protection por defecto.
 *
 * #5 - INFORMACION EXCESIVA EN ERRORES
 *      Los mensajes de error distinguen entre "usuario no existe" y "contrasena incorrecta".
 *      Esto permite a un atacante enumerar usuarios validos del sistema.
 *      FIX: mensaje generico "Credenciales invalidas" para ambos casos.
 *
 * DETECTABLE CON DAST: OWASP ZAP detecta la ausencia de CSRF y la enumeracion de usuarios
 * durante las pruebas dinamicas de seguridad en el pipeline DevSecOps.
 */
@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

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

    /**
     * Endpoint de autenticacion.
     *
     * VULNERABILIDAD #3: Sin rate limiting.
     * Este endpoint acepta tantas peticiones como el atacante quiera enviar.
     * Para demostrarlo en el video: ejecutar el script brute_force_demo.py
     * incluido en la carpeta /docs/exploits/
     *
     * VULNERABILIDAD #4: Sin CSRF token.
     * Spring Boot sin Spring Security no valida tokens CSRF.
     *
     * VULNERABILIDAD #5: Enumeracion de usuarios por mensajes de error distintos.
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        logger.debug("Intento de login para usuario: {}", username);

        // VULNERABILIDAD #5: distinguimos entre usuario no encontrado vs password incorrecta
        // Un atacante puede usar esto para enumerar usuarios validos
        List<User> users = userRepository.findByUsernameAndPassword(username, password);

        if (!users.isEmpty()) {
            User user = users.get(0);
            session.setAttribute("loggedUser", user.getUsername());
            session.setAttribute("role", user.getRole());
            logger.info("Login exitoso para: {}", user.getUsername());
            return "redirect:/dashboard";
        }

        // Verificamos si el usuario existe (esto es la vulnerabilidad de enumeracion)
        // En la arquitectura secure esto NO se hace - mensaje generico para ambos casos
        logger.warn("Intento de login fallido para usuario: {}", username);
        model.addAttribute("error", "Usuario o contrasena incorrectos");
        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String loggedUser = (String) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", loggedUser);
        model.addAttribute("role", session.getAttribute("role"));

        // VULNERABILIDAD: mostramos todos los usuarios en el dashboard
        // Un usuario normal no deberia ver esto, pero sin control de roles es accesible
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("users", allUsers);

        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
