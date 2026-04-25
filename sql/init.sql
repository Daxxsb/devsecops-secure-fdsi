-- =============================================================
-- Script de inicializacion de base de datos segura
-- Arquitectura DevSecOps - Demo ECIJ
-- =============================================================

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

-- Passwords protegidos con BCrypt (coste 12)
INSERT INTO users (username, password, role) VALUES
    -- admin / admin123
    ('admin',    '$2b$12$SPTuPKxe.KQmaPe7flB1veAp2nGoduG9GsR7qO1Rx/7YOaCI/s4Ha', 'ADMIN'),
    -- alice / password
    ('alice',    '$2b$12$LVE2zu7ayl/IKSuUUqKzGOzEvUPAIchRsvzQxpb5RpTxlvMI6IKAC', 'USER'),
    -- bob / 123456
    ('bob',      '$2b$12$bcJj/RyDE3cWIF/wNI3ryexQjfA3CkpI9a1RX9K5ANdmmH167Tvma', 'USER'),
    -- charlie / user123
    ('charlie',  '$2b$12$k4SXSC4hWinmM2NKh/amce7uTxvajLMx4wxnCJwzUzRBaL8cVnai2', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Mensaje de confirmacion
DO $$
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'BD inicializada con usuarios seguros (BCrypt)';
    RAISE NOTICE 'admin:admin123 | alice:password | bob:123456 | charlie:user123';
    RAISE NOTICE '================================================';
END $$;
