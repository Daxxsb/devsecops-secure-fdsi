# DevSecOps Demo - Arquitectura Secure

Proyecto academico para comparar una arquitectura tradicional (unsecure) contra una arquitectura DevSecOps (secure).

## Estado actual de este repositorio

Esta version corresponde a la parte secure:

- SQL Injection mitigada con consultas parametrizadas.
- Password hashing con BCrypt (sin MD5).
- Login protegido por Spring Security.
- CSRF habilitado y token incluido en formularios.
- Secretos por variables de entorno (sin credenciales hardcodeadas en codigo).
- Dashboard con control de acceso por rol y sin exposicion de hashes.
- Pipeline secure con controles SAST, SCA, escaneo de imagen y DAST.

## Stack

- Java 21 runtime (compilacion release 17)
- Spring Boot 3.3.10
- Spring Security
- PostgreSQL 15
- Docker + Docker Compose
- GitHub Actions

## Ejecucion local (secure)

### Prerrequisitos

- Docker Desktop activo
- Java 21
- Maven 3.9+

### Levantar aplicacion

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

### Credenciales de prueba

- admin / admin123
- alice / password
- bob / 123456
- charlie / user123

### Detener

```bash
docker compose down
```

## Variables de entorno (opcionales)

Si no defines variables, se usan defaults locales de demo.

- POSTGRES_DB
- POSTGRES_USER
- POSTGRES_PASSWORD
- DB_URL
- DB_USER
- DB_PASSWORD

## Checklist de comparativa (Unsecure vs Secure)

### 1) SQL Injection en login

- Unsecure: payload `' OR '1'='1' --` permite acceso.
- Secure: payload falla; autenticacion valida credenciales reales.

### 2) Hash de contrasenas

- Unsecure: hashes MD5 expuestos y crackeables.
- Secure: hashes BCrypt en base de datos, no expuestos en UI.

### 3) CSRF

- Unsecure: formulario sin token.
- Secure: formulario incluye token CSRF y Spring Security valida request.

### 4) Exposicion de datos

- Unsecure: dashboard mostraba hashes.
- Secure: dashboard no muestra hashes y lista de usuarios solo para ADMIN.

### 5) Secretos en codigo

- Unsecure: credenciales en application.properties.
- Secure: secretos via variables de entorno.

### 6) Pipeline CI/CD

- Unsecure: build -> docker -> deploy sin gates de seguridad.
- Secure: gates automáticos (SAST, SCA, Trivy, DAST) bloquean despliegue cuando corresponde.

## Workflows disponibles

- `.github/workflows/pipeline-unsecure.yml`
- `.github/workflows/pipeline-secure.yml`
- `.github/workflows/pipeline-secure-demo-sca.yml`

## Nota para presentacion

Para la comparativa:

1. Ejecuta el repo unsecure y demuestra bypass/explotacion.
2. Ejecuta este repo secure y muestra que los mismos intentos ya no funcionan.
3. Muestra en Actions que el pipeline secure aplica gates antes de deploy.

## Referencias

- OWASP Top 10: https://owasp.org/www-project-top-ten/
- SpotBugs: https://spotbugs.github.io/
- OWASP Dependency-Check: https://jeremylong.github.io/DependencyCheck/
- Trivy: https://aquasecurity.github.io/trivy/
- OWASP ZAP: https://www.zaproxy.org/
