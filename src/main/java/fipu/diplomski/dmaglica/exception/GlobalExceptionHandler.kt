package fipu.diplomski.dmaglica.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.SQLException

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<String> {
        logger.error("Exception occurred: ${ex.message}")
        return ResponseEntity.internalServerError().body("Something went wrong: ${ex.message}")
    }

    @ExceptionHandler(SQLException::class)
    fun handleSqlException(ex: SQLException): ResponseEntity<String> {
        logger.error("SQL exception occurred: ${ex.message}. Cause: ${ex.cause}")
        return ResponseEntity.internalServerError().body("SQL exception occurred: ${ex.message}")
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<String> {
        logger.error("User not found: ${ex.message}. Cause: ${ex.cause}")
        return ResponseEntity.internalServerError().body("User not found: ${ex.message}")
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<String> {
        logger.error("User already exists: ${ex.message}. Cause: ${ex.cause}")
        return ResponseEntity.internalServerError().body("User already exists: ${ex.message}")
    }
}