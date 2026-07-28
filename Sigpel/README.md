# LabLoan — Backend

Backend del proyecto integrador **Sigpel
** (préstamo de equipos de laboratorio),
construido en **Kotlin + Spring Boot 4**, con autenticación **AWS Cognito (JWT)**
y base de datos **PostgreSQL**.

## Requisitos

- JDK 21
- PostgreSQL 15+ corriendo localmente (o vía Docker)
- Un User Pool de Cognito con dos grupos: `ENCARGADO` y `ESTUDIANTE`

## Configuración

Variables de entorno (o edita `src/main/resources/application.yml` directamente):

| Variable | Descripción |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Conexión a PostgreSQL |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales de la base |
| `AWS_REGION` | Región del User Pool de Cognito |
| `COGNITO_USER_POOL_ID` | ID del User Pool (ej. `us-east-1_XXXXXXXXX`) |

## Ejecutar

```bash
./gradlew bootRun
```

## Pruebas

```bash
./gradlew test
```

> **Nota:** este código se escribió y organizó en un entorno sin acceso a Maven
> Central, por lo que no se pudo compilar/ejecutar `./gradlew build` aquí.
> Revísalo con tu IDE (IntelliJ) y corre los tests antes de la entrega para
> confirmar que compila tal cual en tu máquina.

## Estructura (arquitectura en capas)

```
entities/      → mapeo JPA (CategoriaEquipo, Equipo, Prestamo, Incidencia)
repositories/  → Spring Data JPA
dto/           → request/response, separados de las entidades
mappers/       → entity -> response DTO
services/      → lógica de negocio, transacciones, autorización por propiedad
controllers/   → endpoints REST + @PreAuthorize por rol
exceptions/    → excepciones propias + manejador global (401/403/404/409)
config/        → seguridad (Cognito JWT) + helper de usuario autenticado
```

## Mapeo con la rúbrica (Arquitectura Empresarial /8)

| Criterio | Dónde está |
|---|---|
| 3.1 Modelo de datos y dominio | `entities/` — 4 tablas, relaciones 1:N y 1:1 con `@ManyToOne`/`@OneToOne` |
| 3.2 Organización en capas | Separación completa controller/service/repository/dto/mapper |
| 3.3 Lógica de negocio y manejo de errores | `PrestamoService` (condición de carrera, transiciones de estado) + `GlobalExceptionHandler` |
| 3.4 Calidad de código | Kotlin idiomático, nombres expresivos, sin duplicación |
| 3.5 Pruebas unitarias | `src/test/.../services/*Test.kt` con MockK, casos válidos e inválidos |
| 3.6 Autenticación/Autorización | `SecurityConfig` (Cognito JWT + roles) + validación de propiedad en `PrestamoService` |

Documentación de decisiones: ver `docs/adr/`.
