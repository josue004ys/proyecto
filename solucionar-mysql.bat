@echo off
echo ==========================================
echo SOLUCION PROBLEMA MYSQL CON SPRING BOOT
echo ==========================================
echo.
echo El problema es que la contrasena de MySQL no coincide.
echo Estas son las opciones para solucionarlo:
echo.
echo 1. Probar contrasenas comunes
echo 2. Resetear la contrasena de root MySQL
echo 3. Crear un usuario nuevo para desarrollo  
echo 4. Usar la contrasena actual que tienes
echo.
set /p choice="Elige una opcion (1-4): "

if %choice%==1 goto test_passwords
if %choice%==2 goto reset_password  
if %choice%==3 goto create_user
if %choice%==4 goto manual_password
goto end

:test_passwords
echo.
echo Probando contrasenas comunes...

echo Probando contrasena vacia...
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=' | Set-Content 'src\main\resources\application.properties'"
echo Configurado: contrasena vacia
goto test_connection

echo Probando contrasena "admin"...
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=admin' | Set-Content 'src\main\resources\application.properties'"
echo Configurado: contrasena "admin"
goto test_connection

echo Probando contrasena "123456"...
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=123456' | Set-Content 'src\main\resources\application.properties'"
echo Configurado: contrasena "123456"
goto test_connection

:reset_password
echo.
echo Para resetear la contrasena de MySQL:
echo 1. Para el servicio MySQL
echo 2. Inicia MySQL en modo seguro
echo 3. Cambia la contrasena
echo.
echo ¿Quieres que lo haga automaticamente? (s/n): 
set /p auto_reset=
if /i "%auto_reset%"=="s" (
    net stop MySQL80
    echo SET PASSWORD FOR 'root'@'localhost' = ''; > reset_password.sql
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --init-file=reset_password.sql --console
    del reset_password.sql
    net start MySQL80
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=' | Set-Content 'src\main\resources\application.properties'"
    echo Contrasena reseteada a vacia.
)
goto test_connection

:create_user
echo.
echo Creando usuario 'dev' sin contrasena para desarrollo...
echo Por favor ingresa la contrasena actual de root cuando se solicite:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE USER IF NOT EXISTS 'dev'@'localhost'; GRANT ALL PRIVILEGES ON citas_medicas.* TO 'dev'@'localhost'; FLUSH PRIVILEGES;"

if %errorlevel%==0 (
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.username=.*', 'spring.datasource.username=dev' | Set-Content 'src\main\resources\application.properties'"
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=' | Set-Content 'src\main\resources\application.properties'"
    echo Usuario 'dev' creado exitosamente.
    goto test_connection
) else (
    echo Error al crear el usuario. Verifica la contrasena de root.
    goto end
)

:manual_password
echo.
set /p manual_pass="Ingresa la contrasena correcta de MySQL root: "
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=%manual_pass%' | Set-Content 'src\main\resources\application.properties'"
echo Configurado con tu contrasena.
goto test_connection

:test_connection
echo.
echo Probando la conexion con Spring Boot...
.\mvnw.cmd spring-boot:run -q
if %errorlevel%==0 (
    echo ¡EXITO! El backend arranco correctamente.
) else (
    echo Aun hay problemas. Intenta otra opcion.
)
goto end

:end
echo.
echo Script terminado.
pause
