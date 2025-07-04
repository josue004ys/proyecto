# 🗄️ Configuración MySQL para Sistema de Citas Médicas

## 📋 **Requisitos previos:**

1. **MySQL Server instalado** (versión 8.0 o superior)
2. **MySQL Workbench** o **phpMyAdmin** (opcional, para interfaz gráfica)

## 🚀 **Pasos para configurar MySQL:**

### **1. Instalar MySQL (si no está instalado):**
```bash
# Descargar desde: https://dev.mysql.com/downloads/mysql/
# O instalar via chocolatey:
choco install mysql

# O instalar via winget:
winget install Oracle.MySQL
```

### **2. Crear la base de datos:**

#### **Opción A: Usando MySQL Command Line:**
```sql
mysql -u root -p
CREATE DATABASE citasdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE citasdb;
exit;
```

#### **Opción B: Usando el archivo SQL incluido:**
```bash
mysql -u root -p < setup-mysql.sql
```

### **3. Configurar la contraseña en application.properties:**

**Si tu MySQL tiene contraseña, actualiza esta línea:**
```properties
spring.datasource.password=TU_CONTRASEÑA_MYSQL
```

**Si tu MySQL NO tiene contraseña (instalación por defecto), deja:**
```properties
spring.datasource.password=
```

### **4. Verificar la configuración actual:**

**Tu `application.properties` está configurado para:**
- **Base de datos:** `citasdb`
- **Usuario:** `root`
- **Contraseña:** (vacía, actualiza si es necesario)
- **Puerto:** `3306` (puerto por defecto de MySQL)

## ⚙️ **Configuración actual en application.properties:**

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/citasdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

## 🔧 **Solución de problemas comunes:**

### **Error: "Access denied for user 'root'"**
```bash
# Resetear contraseña de MySQL:
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'nueva_contraseña';
FLUSH PRIVILEGES;
```

### **Error: "Unknown database 'citasdb'"**
```sql
CREATE DATABASE citasdb;
```

### **Error: "Communications link failure"**
- Verificar que MySQL Server esté corriendo
- Verificar el puerto (por defecto 3306)

## 🎯 **Verificar que MySQL está corriendo:**

### **Windows (Services):**
1. Presiona `Win + R`
2. Escribe `services.msc`
3. Busca `MySQL80` o `MySQL`
4. Debe estar en estado "Running"

### **Comando:**
```bash
# Verificar estado del servicio
net start mysql80

# O verificar conexión
mysql -u root -p -e "SELECT VERSION();"
```

## 🚀 **Iniciar la aplicación:**

Una vez configurado MySQL:

```bash
# Backend
cd proyecto2/proyecto
./mvnw spring-boot:run

# Frontend (en otra terminal)
cd citas-medicas
npm start
```

## 📊 **Ventajas de MySQL vs H2:**

| **Característica** | **MySQL** | **H2** |
|-------------------|-----------|---------|
| **Persistencia** | ✅ Permanente | ❌ Temporal |
| **Producción** | ✅ Recomendado | ❌ Solo desarrollo |
| **Performance** | ✅ Alto | ⚠️ Limitado |
| **Escalabilidad** | ✅ Excelente | ❌ Básica |
| **Herramientas** | ✅ Muchas | ⚠️ Pocas |

**Con MySQL, tus datos serán completamente persistentes y el sistema estará listo para producción.**
