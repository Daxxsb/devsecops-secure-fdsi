package com.devsecops.unsecure.repository;

import com.devsecops.unsecure.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Repositorio de usuarios.
 *
 * VULNERABILIDADES PRESENTES (intencionales para demo DevSecOps):
 *
 * #1 - SQL INJECTION (OWASP Top 10 - A03:2021 Injection)
 *      La query se construye concatenando strings directamente con el input del usuario.
 *      Payload de explotacion: usuario = ' OR '1'='1' --
 *      Esto hace que la query sea: SELECT * FROM users WHERE username = '' OR '1'='1' --' AND password = '...'
 *      Lo que retorna todos los usuarios y permite acceso sin credenciales validas.
 *      FIX en arquitectura secure: usar PreparedStatement con parametros.
 *
 * #2 - HASH MD5 DEBIL (OWASP Top 10 - A02:2021 Cryptographic Failures)
 *      MD5 esta roto criptograficamente desde 2004.
 *      Las contrasenas almacenadas en MD5 son crackeables con herramientas como hashcat o crackstation.net
 *      FIX en arquitectura secure: usar BCryptPasswordEncoder con factor de costo >= 12.
 *
 * DETECTABLE CON SAST: SpotBugs con plugin find-sec-bugs detecta ambas vulnerabilidades
 * en el analisis estatico del pipeline DevSecOps.
 */
@Repository
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role")
    );

    /**
     * VULNERABILIDAD CRITICA: SQL INJECTION
     *
     * Esta query concatena directamente los inputs del usuario en el SQL.
     * Un atacante puede inyectar codigo SQL para:
     * - Bypassear autenticacion
     * - Extraer todos los datos de la tabla
     * - En algunos casos, ejecutar comandos en el servidor
     *
     * Payload de demo:
     *   username: ' OR '1'='1' --
     *   password: cualquier cosa
     *
     * Query resultante:
     *   SELECT * FROM users WHERE username = '' OR '1'='1' --' AND password = 'md5(...)'
     *   El -- comenta el resto, el OR '1'='1' siempre es true -> acceso concedido
     */
    public List<User> findByUsernameAndPassword(String username, String password) {

        // VULNERABILIDAD: hash MD5 debil de la contrasena
        String hashedPassword = md5Hash(password);

        // VULNERABILIDAD CRITICA: concatenacion directa = SQL Injection
        String sql = "SELECT * FROM users WHERE username = '" + username
                   + "' AND password = '" + hashedPassword + "'";

        // VULNERABILIDAD: log de la query completa expone el payload del atacante
        logger.debug("Ejecutando query de autenticacion: {}", sql);

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    /**
     * VULNERABILIDAD: Uso de MD5 para hashing de contrasenas.
     * MD5 produce hashes de 128 bits reversibles con tablas precomputadas.
     * Ejemplo: MD5("admin123") = 0192023a7bbd73250516f069df18b500
     * Este hash se puede crackear en segundos en crackstation.net
     */
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); // VULNERABILIDAD: algoritmo debil
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash MD5", e);
        }
    }
}
