@echo off
echo ====================================
echo CONFIGURACION DE MYSQL PARA SPRING BOOT
echo ====================================
echo.
echo Este script te ayudara a configurar MySQL correctamente.
echo.
echo Opciones:
echo 1. Probar con contraseña vacia (root sin password)
echo 2. Probar con contraseña "root"
echo 3. Probar con contraseña "admin"
echo 4. Probar con contraseña "12345"
echo 5. Crear usuario nuevo sin contraseña para desarrollo
echo 6. Configurar manualmente
echo.
set /p choice="Elige una opcion (1-6): "

if %choice%==1 (
    echo Configurando con contraseña vacia...
    call :configure_password ""
) else if %choice%==2 (
    echo Configurando con contraseña "root"...
    call :configure_password "root"
) else if %choice%==3 (
    echo Configurando con contraseña "admin"...
    call :configure_password "admin"
) else if %choice%==4 (
    echo Configurando con contraseña "123456"...
    call :configure_password "123456"
) else if %choice%==5 (
    echo Creando usuario nuevo para desarrollo...
    call :create_dev_user
) else if %choice%==6 (
    echo Configuracion manual...
    call :manual_config
) else (
    echo Opcion invalida
    pause
    exit /b 1
)

echo.
echo Probando conexion...
call :test_spring_boot
pause
exit /b 0

:configure_password
echo spring.datasource.password=%~1 > temp_password.txt
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=%~1' | Set-Content 'src\main\resources\application.properties'"
echo Configuracion actualizada.
goto :eof

:create_dev_user
echo Creando usuario 'dev' sin contraseña...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE USER IF NOT EXISTS 'dev'@'localhost'; GRANT ALL PRIVILEGES ON citas_medicas.* TO 'dev'@'localhost'; FLUSH PRIVILEGES;"
if %errorlevel%==0 (
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.username=.*', 'spring.datasource.username=dev' | Set-Content 'src\main\resources\application.properties'"
    powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=' | Set-Content 'src\main\resources\application.properties'"
    echo Usuario 'dev' creado y configurado.
) else (
    echo Error al crear usuario 'dev'.
)
goto :eof

:manual_config
set /p username="Ingresa el usuario de MySQL: "
set /p password="Ingresa la contraseña de MySQL: "
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.username=.*', 'spring.datasource.username=%username%' | Set-Content 'src\main\resources\application.properties'"
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'spring.datasource.password=.*', 'spring.datasource.password=%password%' | Set-Content 'src\main\resources\application.properties'"
echo Configuracion manual completada.
goto :eof

:test_spring_boot
echo.
echo Probando el arranque de Spring Boot...
.\mvnw.cmd spring-boot:run -q
goto :eof
