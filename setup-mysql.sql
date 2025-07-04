-- ====================================
-- SCRIPT DE CONFIGURACIÓN MYSQL
-- Sistema de Citas Médicas - BASE LIMPIA
-- ====================================

-- Eliminar base de datos si existe (para empezar completamente limpio)
DROP DATABASE IF EXISTS citasdb;

-- Crear la base de datos nueva
CREATE DATABASE citasdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE citasdb;

-- Verificar que la base de datos está creada
SELECT 'Base de datos citasdb recreada - SISTEMA LIMPIO SIN DATOS DE PRUEBA' AS mensaje;

-- Las tablas se crearán automáticamente cuando inicies Spring Boot
-- El sistema estará completamente limpio sin datos de prueba
