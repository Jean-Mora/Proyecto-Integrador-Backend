package com.pucetec.users.services

import com.pucetec.users.dto.UserRequest
import com.pucetec.users.dto.UserResponse
import com.pucetec.users.entities.User
import com.pucetec.users.exceptions.BlankNameException
import com.pucetec.users.exceptions.DuplicateCognitoIdException
import com.pucetec.users.exceptions.UserNotFoundException
import com.pucetec.users.mappers.toEntity
import com.pucetec.users.mappers.toResponse
import com.pucetec.users.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// es el que almacena la logica del negocio
@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // Registra el perfil de un usuario y lo asocia a su cognitoId.
    // El cognitoId sale del token (claim "sub"), no del body.
    fun createUser(cognitoId: String, request: UserRequest): UserResponse {
        if (request.name.isBlank()) {
            logger.warn("event=user.create_rejected | msg=Blank name")
            throw BlankNameException("Name cannot be blank")
        }

        // La relacion cognitoId -> perfil es 1 a 1: no puede haber dos perfiles
        // para el mismo usuario de Cognito.
        if (userRepository.existsByCognitoId(cognitoId)) {
            logger.warn("event=user.create_rejected | msg=Duplicate cognitoId")
            throw DuplicateCognitoIdException("Ya existe un perfil para el usuario $cognitoId")
        }

        val userEntity = request.toEntity(cognitoId)
        val savedUser = userRepository.save(userEntity)
        logger.info("event=user.created | msg=User profile created | userId=${savedUser.id}")
        return savedUser.toResponse()
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            UserNotFoundException("Usuario $id no encontrado")
        }
        return user.toResponse()
    }

    // El corazon del micro: dado un cognitoId, devuelve los datos propios asociados.
    fun getUserByCognitoId(cognitoId: String): UserResponse {
        val user = userRepository.findByCognitoId(cognitoId).orElseThrow {
            UserNotFoundException("No existe un perfil para el usuario $cognitoId")
        }
        return user.toResponse()
    }

    fun updateUser(cognitoId: String, request: UserRequest): UserResponse {
        val user = userRepository.findByCognitoId(cognitoId).orElseThrow {
            UserNotFoundException("No existe un perfil para el usuario $cognitoId")
        }
        if (request.name.isBlank()) {
            logger.warn("event=user.update_rejected | msg=Blank name | userId=${user.id}")
            throw BlankNameException("Name cannot be blank")
        }
        val updated = User(
            id = user.id,
            cognitoId = user.cognitoId,
            name = request.name,
            email = request.email,
            phone = request.phone
        )
        val saved = userRepository.save(updated)
        logger.info("event=user.updated | msg=User profile updated | userId=${saved.id}")
        return saved.toResponse()
    }

    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException("Usuario $id no encontrado")
        }
        userRepository.deleteById(id)
        logger.info("event=user.deleted | msg=User profile deleted | userId=$id")
    }
}
