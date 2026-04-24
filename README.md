# DevSecOps Demo - Arquitectura Unsecure

Proyecto academico para demostrar diferencias entre una arquitectura tradicional y una arquitectura DevSecOps con controles de seguridad integrados.

Paper base: Fundamentos de DevSecOps en el ciclo de vida del software - Uso Temprano del DevSecOps.

## Aviso importante

Este repositorio es intencionalmente vulnerable para fines educativos. No debe usarse como base para ambientes productivos.

## Stack

- Java 21 (runtime)
- Spring Boot 3.0.0 (intencionalmente antiguo)
- PostgreSQL 15
- Docker + Docker Compose
- GitHub Actions
- Thymeleaf + Spring JDBC

## Estructura del proyecto

```text
devsecops-unsecure/
|-- .github/workflows/
|   `-- pipeline-unsecure.yml      # Pipeline sin controles de seguridad
|-- docs/exploits/
|   `-- brute_force_demo.py        # Script de fuerza bruta para demo
|-- sql/
|   `-- init.sql                   # Esquema + datos semilla vulnerables
|-- src/main/java/com/devsecops/unsecure/
|   |-- UnsecureLoginApplication.java
|   |-- controller/LoginController.java
|   |-- model/User.java
|   `-- repository/UserRepository.java
|-- src/main/resources/
|   |-- application.properties
|   `-- templates/
|       |-- login.html
|       `-- dashboard.html
|-- Dockerfile
|-- docker-compose.yml
`-- pom.xml
```

## Como ejecutar (estado actual validado)

### Prerrequisitos

- Docker Desktop activo
- Java 21 instalado
- Maven 3.9+
- Python 3 (para el script de brute force)

### Levantar la app

```bash
mvn clean package -DskipTests
docker compose up --build -d
```

### Verificar estado

```bash
docker compose ps
docker compose logs --no-color app --tail=80
```

Abrir: http://localhost:8080

### Detener todo

```bash
docker compose down
```

## Credenciales de prueba

| Usuario | Contrasena | Rol |
|---|---|---|
| admin | admin123 | ADMIN |
| alice | password | USER |
| bob | 123456 | USER |

Nota: el usuario charlie existe en la semilla SQL, pero su hash en init.sql no coincide con user123.

## Vulnerabilidades demostrables

| # | Vulnerabilidad | OWASP | Evidencia esperada |
|---|---|---|---|
| 1 | SQL Injection en login | A03:2021 | Login exitoso con payload ' OR '1'='1' -- |
| 2 | MD5 para contrasenas | A02:2021 | Hashes visibles y crackeables |
| 3 | Credenciales hardcodeadas | A02:2021 | application.properties con usuario/password en texto plano |
| 4 | Sin rate limiting | A07:2021 | Script brute force encuentra admin123 sin bloqueo |
| 5 | Sin CSRF token en form | A01:2021 | login.html sin input _csrf |
| 6 | Imagen base susceptible a CVEs | A06:2021 | Dockerfile con imagen base generalista |
| 7 | Dependencias vulnerables (Spring Boot 3.0.0) | A06:2021 | pom.xml con version 3.0.0 |
| 8 | Logs exponen SQL completo | A09:2021 | docker logs muestra query de autenticacion con input |

## Guia corta para evidenciar vulnerabilidades

1. SQL Injection

```text
Usuario: ' OR '1'='1' --
Contrasena: cualquier valor
```

Resultado esperado: redireccion a /dashboard.

2. Brute force

```bash
python docs/exploits/brute_force_demo.py
```

Resultado esperado: multiples intentos sin bloqueo y exito con admin123.

3. Logs con SQL sensible

```bash
docker compose logs -f app
```

Resultado esperado: linea con Ejecutando query de autenticacion: SELECT ...

4. Credenciales hardcodeadas

Revisar src/main/resources/application.properties.

5. CSRF ausente

Revisar src/main/resources/templates/login.html y confirmar que no existe _csrf.

## Plan de mejora de seguridad (paso a paso)

### Fase 1 - Bloqueo rapido de riesgos criticos

1. SQL Injection: reemplazar concatenacion SQL por consultas parametrizadas con JdbcTemplate.
2. Password hashing: migrar de MD5 a BCryptPasswordEncoder (coste >= 12).
3. Secretos: mover credenciales a variables de entorno y GitHub Secrets.
4. Logging: eliminar logs de SQL completo y de datos de entrada sensibles.

### Fase 2 - Endurecimiento de autenticacion y sesiones

1. Integrar Spring Security.
2. Activar CSRF en formularios POST.
3. Agregar rate limiting en /login (Bucket4j o filtro dedicado).
4. Bloqueo temporal por intentos fallidos y auditoria de eventos.

### Fase 3 - Seguridad de dependencias y contenedores

1. Actualizar Spring Boot a una version mantenida.
2. Definir politica de actualizacion mensual de dependencias.
3. Escanear imagen con Trivy y bloquear CVEs HIGH/CRITICAL.
4. Usar imagen base reducida, usuario no root y HEALTHCHECK.

### Fase 4 - Pipeline DevSecOps completo

1. SAST en CI (SpotBugs + FindSecBugs).
2. SCA con OWASP Dependency-Check.
3. Container scanning con Trivy.
4. DAST con OWASP ZAP antes de aprobar despliegue.
5. Reglas de calidad: fallar pipeline si hay hallazgos criticos.

## Comandos git para inicializar, commitear y pushear

### 1) Inicializar repositorio local

```bash
git init
git branch -M main
```

### 2) Crear .gitignore recomendado para este proyecto

```gitignore
# Java / Maven
target/
*.class

# IDE
.vscode/
.idea/

# OS
.DS_Store
Thumbs.db

# Python cache
__pycache__/
*.pyc
```

### 3) Agregar y commitear

```bash
git add .
git commit -m "feat: demo unsecure para practica DevSecOps"
```

Si Git pide identidad:

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-correo@dominio.com"
```

### 4) Crear remoto (sugerencia)

Sugerencia de nombre: devsecops-unsecure-fdsi

En GitHub, crear un repositorio vacio y copiar la URL.

HTTPS:

```bash
git remote add origin https://github.com/TU_USUARIO/devsecops-unsecure-fdsi.git
git push -u origin main
```

SSH:

```bash
git remote add origin git@github.com:TU_USUARIO/devsecops-unsecure-fdsi.git
git push -u origin main
```

### 5) Verificar remoto configurado

```bash
git remote -v
```

## Referencias

- OWASP Top 10: https://owasp.org/www-project-top-ten/
- SpotBugs + FindSecBugs: https://find-sec-bugs.github.io/
- OWASP Dependency-Check: https://jeremylong.github.io/DependencyCheck/
- Trivy: https://aquasecurity.github.io/trivy/
- OWASP ZAP: https://www.zaproxy.org/
