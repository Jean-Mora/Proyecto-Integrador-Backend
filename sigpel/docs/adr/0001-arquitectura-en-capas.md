# ADR 0001: Arquitectura en capas (controller / service / repository)

## Estado
Aceptado

## Contexto
El backend de Sigpel necesita exponer una API REST sobre una base de datos
relacional, con reglas de negocio (disponibilidad de equipos, quien puede
aprobar/cancelar un prestamo) y validaciones de seguridad (rol y propiedad).
Se evaluo poner la logica directamente en los controllers (mas rapido de
escribir al inicio) contra separar en capas explicitas.

## Decision
Se separa el codigo en capas con responsabilidad unica:

- **controllers**: reciben la peticion HTTP, delegan al service y devuelven el DTO de respuesta. No contienen logica de negocio.
- **services**: contienen toda la logica de negocio, las transacciones (`@Transactional`) y las validaciones de autorizacion por propiedad.
- **repositories**: acceso a datos via Spring Data JPA, sin logica de negocio.
- **entities**: mapeo objeto-relacional puro.
- **dto / mappers**: separan lo que la API expone de las entidades JPA, evitando fugas del modelo de persistencia hacia el cliente.

## Consecuencias
- Los controllers quedan casi triviales, lo que facilita agregar validaciones de rol con `@PreAuthorize` sin ensuciar la logica de negocio.
- Las pruebas unitarias se escriben contra los services (mockeando repositories), sin necesidad de levantar el contexto de Spring completo.
- Costo: mas archivos y mas boilerplate (DTOs + mappers) que si se expusieran las entidades directamente.
