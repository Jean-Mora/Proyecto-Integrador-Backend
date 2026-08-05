# ADR 0002: Optimistic locking para evitar prestamos duplicados

## Estado
Aceptado

## Contexto
Dos estudiantes podrian solicitar el mismo equipo casi al mismo tiempo. Sin
control de concurrencia, ambas solicitudes podrian leer el equipo como
DISPONIBLE y las dos transacciones terminarian creando un prestamo para el
mismo equipo, dejando el inventario inconsistente.

Se evaluaron dos opciones:

1. **Pessimistic locking** (`SELECT ... FOR UPDATE` / `@Lock(PESSIMISTIC_WRITE)`): bloquea la fila del equipo mientras dura la transaccion.
2. **Optimistic locking** (`@Version` de JPA/Hibernate): cada `Equipo` tiene una columna `version` que Hibernate incrementa en cada UPDATE; si dos transacciones intentan modificar la misma version, la segunda falla.

## Decision
Se usa **optimistic locking** con `@Version` en la entidad `Equipo`.

Justificacion: las colisiones (dos personas pidiendo el mismo equipo en el
mismo instante) son poco frecuentes frente al volumen total de lecturas
(consultar el catalogo). El locking pesimista bloquearia filas en cada
lectura con intencion de escritura, lo cual no es necesario para este
volumen de trafico y agregaria complejidad de deadlocks entre transacciones.

## Consecuencias
- `PrestamoService.solicitar()` valida el estado del equipo y lo actualiza
  dentro de la misma transaccion (`@Transactional`).
- Si ocurre una colision, Hibernate lanza `ObjectOptimisticLockingFailureException`,
  que `GlobalExceptionHandler` traduce a `409 Conflict`. El cliente (app movil)
  debe interpretar ese codigo como "alguien mas se adelanto, refresca el catalogo".
- No se requiere infraestructura adicional (a diferencia de un lock distribuido),
  lo cual es apropiado para el alcance de este proyecto academico.
