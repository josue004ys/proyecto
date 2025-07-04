# Sistema de Citas Médicas

Sistema completo de gestión de citas médicas desarrollado con Spring Boot (backend) y Angular (frontend).

## 📋 Características Principales

- **Gestión de Especialidades Médicas**: CRUD completo de especialidades médicas
- **Gestión de Doctores**: Registro, consulta y administración de doctores
- **Gestión de Pacientes**: Registro y autenticación de pacientes
- **Agendamiento de Citas**: Sistema completo de reserva de citas médicas
- **Panel de Doctor**: Dashboard para doctores con gestión de horarios
- **Autenticación JWT**: Sistema de seguridad basado en tokens
- **Base de Datos H2**: Base de datos en memoria para desarrollo

## 🛠️ Tecnologías

### Backend
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **Spring Security**
- **JWT Authentication**
- **H2 Database** (desarrollo)
- **Maven**

### Frontend
- **Angular 19.1.0**
- **Bootstrap 5.3.7**
- **RxJS**
- **TypeScript**

## 🚀 Instalación y Ejecución

### Prerequisitos
- Java 20 o superior
- Node.js 18+ y npm
- Git

### Backend (Spring Boot)

1. **Navegar al directorio del backend:**
   ```bash
   cd proyecto2/proyecto
   ```

2. **Compilar y ejecutar:**
   ```bash
   ./mvnw spring-boot:run
   ```
   O en Windows:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```

3. **El backend estará disponible en:**
   - API: http://localhost:8081/api/
   - H2 Console: http://localhost:8081/h2-console
     - JDBC URL: `jdbc:h2:mem:testdb`
     - Usuario: `sa`
     - Contraseña: (vacía)

### Frontend (Angular)

1. **Navegar al directorio del frontend:**
   ```bash
   cd citas-medicas
   ```

2. **Instalar dependencias:**
   ```bash
   npm install
   ```

3. **Ejecutar el servidor de desarrollo:**
   ```bash
   ng serve
   ```

4. **El frontend estará disponible en:**
   - http://localhost:4200

## 📊 Endpoints de la API

### Especialidades
- `GET /api/especialidades` - Obtener todas las especialidades
- `GET /api/especialidades/activas` - Obtener especialidades activas
- `GET /api/especialidades/{id}` - Obtener especialidad por ID
- `POST /api/especialidades` - Crear nueva especialidad
- `PUT /api/especialidades/{id}` - Actualizar especialidad
- `DELETE /api/especialidades/{id}` - Eliminar especialidad

### Doctores
- `GET /api/doctores` - Obtener todos los doctores
- `GET /api/doctores/{id}` - Obtener doctor por ID

### Pacientes
- `GET /api/pacientes` - Obtener todos los pacientes
- `POST /api/pacientes/registrar` - Registrar nuevo paciente
- `GET /api/pacientes/buscar?correo=` - Buscar paciente por correo

### Horarios
- `GET /api/horarios` - Obtener todos los horarios
- `GET /api/horarios/doctor/{doctorId}` - Obtener horarios de un doctor

## 🗄️ Datos Iniciales

El sistema incluye datos de prueba que se cargan automáticamente:

### Especialidades (12)
- Cardiología
- Pediatría
- Medicina General
- Ginecología
- Dermatología
- Neurología
- Traumatología
- Oftalmología
- Otorrinolaringología
- Psiquiatría
- Urología
- Endocrinología

### Doctores (5)
- Dr. Carlos Mendoza (Cardiología)
- Dra. María González (Pediatría)
- Dr. Roberto Silva (Medicina General)
- Dra. Ana López (Ginecología)
- Dr. Luis Ramírez (Dermatología)

### Usuario Administrador
- **Email**: admin@hospital.com
- **Contraseña**: admin123

## 🔧 Configuración

### Backend
La configuración principal se encuentra en `src/main/resources/application.properties`:

```properties
# Puerto del servidor
server.port=8081

# Base de datos H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# JWT
jwt.secret=mi-secreto-super-seguro-para-jwt-que-debe-ser-largo-y-complejo-123456789
jwt.expiration=86400000

# CORS
cors.allowed.origins=http://localhost:4200,http://localhost:3000
```

### Frontend
Los servicios están configurados para conectarse al backend en `localhost:8081`.

## 📁 Estructura del Proyecto

```
proyecto-citas-medicas/
├── citas-medicas/          # Frontend Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── auth/       # Autenticación
│   │   │   ├── citas/      # Gestión de citas
│   │   │   ├── doctor/     # Panel de doctor
│   │   │   └── services/   # Servicios Angular
│   │   └── ...
│   └── ...
└── proyecto2/
    └── proyecto/           # Backend Spring Boot
        ├── src/
        │   ├── main/
        │   │   ├── java/
        │   │   │   └── com/example/proyecto/
        │   │   │       ├── controller/    # Controladores REST
        │   │   │       ├── service/       # Servicios de negocio
        │   │   │       ├── entity/        # Entidades JPA
        │   │   │       ├── repository/    # Repositorios JPA
        │   │   │       ├── config/        # Configuración
        │   │   │       └── security/      # Seguridad
        │   │   └── resources/
        │   └── ...
        └── ...
```

## 🧪 Pruebas

Para verificar que el sistema funciona correctamente:

1. **Iniciar el backend** (puerto 8081)
2. **Iniciar el frontend** (puerto 4200)
3. **Verificar endpoints**:
   - http://localhost:8081/api/especialidades
   - http://localhost:8081/api/doctores
4. **Acceder a la aplicación**: http://localhost:4200

## 📝 Notas de Desarrollo

- El sistema usa **H2 en memoria** para desarrollo, los datos se reinician en cada arranque
- **CORS** está configurado para desarrollo con Angular
- Los **datos de prueba** se cargan automáticamente via `DataInitializer`
- La **autenticación JWT** está implementada pero puede expandirse
- El sistema está preparado para **migrar a MySQL** en producción

## 👥 Contribución

Este es un proyecto educativo completo que demuestra la integración entre Spring Boot y Angular para un sistema de gestión médica.

---
**Developed with ❤️ using Spring Boot + Angular**
