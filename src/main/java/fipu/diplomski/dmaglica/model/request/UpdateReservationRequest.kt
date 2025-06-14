package fipu.diplomski.dmaglica.model.request

import java.time.LocalDateTime

data class UpdateReservationRequest(
    val userId: Int,
    val reservationId: Int,
    val venueId: Int,
    val reservationDate: LocalDateTime? = null,
    val numberOfPeople: Int? = null,
)
