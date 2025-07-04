@echo off
echo ====================================
echo LIMPIANDO BASE DE DATOS - SISTEMA LIMPIO
echo ====================================

echo Conectando a MySQL/MariaDB...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS citasdb; CREATE DATABASE citasdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; USE citasdb; SELECT 'Base de datos citasdb recreada - SISTEMA LIMPIO' AS mensaje;"

echo.
echo ✅ Base de datos limpia recreada exitosamente
echo ✅ El sistema ahora está completamente limpio sin datos de prueba
echo.
echo Ahora puedes iniciar Spring Boot y tendrás un sistema limpio
pause
