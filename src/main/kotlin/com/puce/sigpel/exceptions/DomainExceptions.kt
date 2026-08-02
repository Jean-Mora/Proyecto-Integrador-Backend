package com.puce.sigpel.exceptions

/** El recurso solicitado no existe -> 404 Not Found. */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** El equipo no esta DISPONIBLE cuando se intenta prestar -> 409 Conflict. */
class EquipoNoDisponibleException(message: String) : RuntimeException(message)

/** El usuario autenticado no es dueno del recurso o el estado no lo permite -> 403 Forbidden. */
class ForbiddenOperationException(message: String) : RuntimeException(message)
