package fipu.diplomski.dmaglica.controller

import fipu.diplomski.dmaglica.model.data.Reservation
import fipu.diplomski.dmaglica.model.request.CreateReservationRequest
import fipu.diplomski.dmaglica.model.request.UpdateReservationRequest
import fipu.diplomski.dmaglica.model.response.BasicResponse
import fipu.diplomski.dmaglica.service.ReservationService
import fipu.diplomski.dmaglica.util.Paths
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Paths.RESERVATION)
class ReservationController(private val reservationService: ReservationService) {

    @PostMapping(Paths.CREATE_RESERVATION)
    fun createReservation(
        @RequestBody request: CreateReservationRequest
    ): BasicResponse = reservationService.create(request)

    @GetMapping(Paths.GET_RESERVATIONS)
    fun getReservations(
        @RequestParam("userId") userId: Int
    ): List<Reservation> = reservationService.getAll(userId)

    @PatchMapping(Paths.UPDATE_RESERVATION)
    fun updateReservation(
        @RequestBody request: UpdateReservationRequest
    ): BasicResponse = reservationService.update(request)

    @DeleteMapping(Paths.DELETE_RESERVATION)
    fun deleteReservation(
        @RequestParam("userId") userId: Int,
        @RequestParam("reservationId") reservationId: Int,
        @RequestParam("venueId") venueId: Int
    ): BasicResponse = reservationService.delete(userId, reservationId, venueId)
}
