package com.puce.sigpel.exceptions

/** The requested resource does not exist -> 404 Not Found. */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** The equipment is not AVAILABLE when trying to loan it -> 409 Conflict. */
class EquipmentNotAvailableException(message: String) : RuntimeException(message)

/** The authenticated user does not own the resource or the status does not allow it -> 403 Forbidden. */
class ForbiddenOperationException(message: String) : RuntimeException(message)

/** A resource with the same unique identifying field already exists -> 409 Conflict. */
class DuplicateResourceException(message: String) : RuntimeException(message)
