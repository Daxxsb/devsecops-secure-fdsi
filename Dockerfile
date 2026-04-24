# =============================================================
# VULNERABILIDAD: Imagen base desactualizada con CVEs conocidos
# openjdk:21-jdk-slim tiene vulnerabilidades en paquetes del SO
# que Trivy detectara en el escaneo de contenedor del pipeline secure.
#
# CVEs tipicos detectados:
#   - Vulnerabilidades en libssl/openssl del SO base
#   - Paquetes del sistema sin parchear
#
# FIX en arquitectura secure: usar eclipse-temurin:21-jre-alpine
# que tiene menor superficie de ataque y se mantiene actualizado.
# =============================================================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

# Copiar el JAR generado por Maven
COPY target/*.jar app.jar

# VULNERABILIDAD: Exponer puerto sin documentacion de que servicios corren
EXPOSE 8080

USER app

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
	CMD wget --no-verbose --tries=1 --spider http://localhost:8080/login || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
