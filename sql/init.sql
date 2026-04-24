-- =============================================================
-- Script de inicializacion de base de datos
-- Arquitectura Unsecure - Demo DevSecOps
-- =============================================================

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    -- VULNERABILIDAD: contrasenas almacenadas como MD5 debil
    -- MD5("admin123")   = 0192023a7bbd73250516f069df18b500
    -- MD5("user123")    = 1e8c29a5d72cf2ca33c9c826cd36d0ea
    -- MD5("password")   = 5f4dcc3b5aa765d61d8327deb882cf99
    -- MD5("123456")     = e10adc3949ba59abbe56e057f20f883e
    -- Todos estos hashes son conocidos en tablas rainbow publicas
    password VARCHAR(32)  NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

-- Insertar usuarios de prueba con contrasenas en MD5
-- VULNERABILIDAD: contrasenas debiles y predecibles
INSERT INTO users (username, password, role) VALUES
    -- admin / admin123
    ('admin',    '0192023a7bbd73250516f069df18b500', 'ADMIN'),
    -- alice / password
    ('alice',    '5f4dcc3b5aa765d61d8327deb882cf99', 'USER'),
    -- bob / 123456
    ('bob',      'e10adc3949ba59abbe56e057f20f883e', 'USER'),
    -- charlie / user123
    ('charlie',  '1e8c29a5d72cf2ca33c9c826cd36d0ea', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Mensaje de confirmacion
DO $$
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'BD inicializada con usuarios vulnerables (MD5)';
    RAISE NOTICE 'admin:admin123 | alice:password | bob:123456';
    RAISE NOTICE '================================================';
END $$;
