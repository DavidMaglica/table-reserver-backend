package fipu.diplomski.dmaglica.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.SQLException

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val kLogger = KotlinLogging.logger(GlobalExceptionHandler::class.java.name)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<String> {
        kLogger.error { "Exception occurred: ${ex.message}" }
        return ResponseEntity.internalServerError().body("Something went wrong: ${ex.message}")
    }

    @ExceptionHandler(SQLException::class)
    fun handleSqlException(ex: SQLException): ResponseEntity<String> {
        kLogger.error { "Sql exception occurred: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("Sql exception occurred: ${ex.message}")
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity<String> {
        kLogger.error { "Entity not found occurred: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entity not found exception occurred: ${ex.message}")
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<String> {
        kLogger.error { "User not found: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("User not found: ${ex.message}")
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<String> {
        kLogger.error { "User already exists: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("User already exists: ${ex.message}")
    }

    @ExceptionHandler(ImageDataException::class)
    fun handleImageDataException(ex: ImageDataException): ResponseEntity<String> {
        kLogger.error { "Image data exception: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("Exception while handling image data: ${ex.message}")
    }

    @ExceptionHandler(VenueNotFoundException::class)
    fun handleVenueNotFoundException(ex: VenueNotFoundException): ResponseEntity<String> {
        kLogger.error { "Venue not found: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("Venue not found: ${ex.message}")
    }

    @ExceptionHandler(ReservationNotFoundException::class)
    fun handleReservationNotFoundException(ex: ReservationNotFoundException): ResponseEntity<String> {
        kLogger.error { "Reservation not found: ${ex.message}. Cause: ${ex.cause}" }
        return ResponseEntity.internalServerError().body("Reservation not found: ${ex.message}")
    }
}
