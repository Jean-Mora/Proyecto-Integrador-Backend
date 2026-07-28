package com.puce.labloan.exceptions

/** El recurso solicitado no existe -> 404 Not Found. */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** El equipo no esta DISPONIBLE cuando se intenta prestar -> 409 Conflict. */
class EquipoNoDisponibleException(message: String) : RuntimeException(message)

/** El usuario autenticado no es dueno del recurso o el estado no lo permite -> 403 Forbidden. */
class ForbiddenOperationException(message: String) : RuntimeException(message)

/** Ya existe una incidencia para ese prestamo (relacion 1:1) -> 409 Conflict. */
class IncidenciaYaRegistradaException(message: String) : RuntimeException(message)
