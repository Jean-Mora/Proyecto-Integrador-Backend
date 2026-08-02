package com.puce.sigpel.exceptions

/** El recurso solicitado no existe -> 404 Not Found. */
class ResourceNotFoundException(message: String) : RuntimeException(message)
