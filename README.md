# EcoRastro: Gestión de Huella de Carbono Personal

EcoRastro es una aplicación de escritorio desarrollada para la monitorización y reducción del impacto ambiental individual. El sistema permite a los usuarios registrar actividades diarias, analizar su huella mediante herramientas estadísticas avanzadas y recibir recomendaciones personalizadas para fomentar hábitos sostenibles.

## Funcionalidades Principales

* **Gestión de Usuarios:** Sistema de autenticación seguro con registro de perfiles, validación de credenciales y cifrado de contraseñas mediante BCrypt.
* **Registro de Huellas (CRUD):** Gestión integral de registros de consumo (inserción, consulta, edición y borrado) con validación lógica de datos.
* **Control de Hábitos:** Implementación de rutinas recurrentes con periodicidad diaria, semanal o mensual para la automatización del seguimiento ambiental.
* **Análisis Estadístico:** Dashboard visual con gráficos circulares de distribución por categorías, gráficos de barras comparativos y gráficos de líneas para la evolución temporal de los últimos 12 meses.
* **Sistema de Gamificación:** Ranking global de usuarios con asignación de niveles según el desempeño y la reducción de emisiones.
* **Exportación de Datos:** Capacidad de generación de informes del historial de actividades en formato CSV.

## Especificaciones Técnicas

* **Lenguaje:** Java 21.
* **Interfaz Gráfica:** JavaFX con arquitectura FXML y diseño basado en hojas de estilo CSS (Tema Ancient Woods).
* **Persistencia:** Hibernate 6.4 (JPA) para la gestión del mapeo objeto-relacional.
* **Base de Datos:** MySQL 8.0.
* **Gestión de Dependencias:** Maven.

## Arquitectura del Software

El proyecto sigue una arquitectura modular por capas para garantizar la escalabilidad y el mantenimiento:

1. **Modelo:** Entidades JPA con mapeos complejos, incluyendo el uso de claves compuestas y relaciones de integridad referencial.
2. **DAO (Data Access Object):** Capa de persistencia con consultas HQL optimizadas mediante técnicas de carga para evitar problemas de rendimiento.
3. **Servicios:** Lógica de negocio desacoplada que actúa como intermediaria entre los datos y la interfaz.
4. **Controladores:** Gestión de eventos de usuario y actualización dinámica de las vistas FXML.

## Calidad y Pruebas

El desarrollo se ha realizado bajo la metodología **TDD (Test Driven Development)**, asegurando la fiabilidad del código mediante una suite de pruebas unitarias e integración con JUnit 5. Se validan procesos críticos como el cálculo de huellas, la lógica de los servicios y la persistencia de datos.

## Instalación y Configuración

1. Configurar los parámetros de conexión en el archivo `src/main/resources/hibernate.cfg.xml`.
2. Desplegar el entorno de base de datos MySQL (se proporciona `docker-compose.yml` para una ejecución rápida).
3. Compilar y ejecutar la aplicación mediante el comando de Maven: `mvn clean javafx:run`.

---
**Autor:** Antonio Delgado Portero  
**Asignatura:** Acceso a Datos  
**Centro:** IES Francisco de los Ríos
