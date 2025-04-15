package fipu.diplomski.dmaglica.reservation

import fipu.diplomski.dmaglica.exception.UserNotFoundException
import fipu.diplomski.dmaglica.exception.VenueNotFoundException
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.sql.SQLException
import java.util.*

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class DeleteReservationTest : BaseReservationServiceTest() {

    @Test
    fun `should throw if user does not exist`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(null)

        val exception = assertThrows<UserNotFoundException> {
            reservationService.delete(mockedUser.email, mockedReservation.id)
        }

        exception.message `should be equal to` "User with email ${mockedUser.email} not found"

        verify(userRepository, times(1)).findByEmail(anyString())
        verifyNoInteractions(venueRepository)
        verifyNoInteractions(reservationRepository)
    }

    @Test
    fun `should throw if venue does not exist`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(mockedUser)
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.empty())

        val exception = assertThrows<VenueNotFoundException> {
            reservationService.delete(mockedUser.email, mockedReservation.id)
        }

        exception.message `should be equal to` "Venue with id ${mockedReservation.id} not found"

        verify(userRepository, times(1)).findByEmail(anyString())
        verify(venueRepository, times(1)).findById(anyInt())
        verifyNoInteractions(reservationRepository)
    }

    @Test
    fun `should throw if unable to delete`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(mockedUser)
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(reservationRepository.deleteById(anyInt())).thenThrow(RuntimeException())

        val exception = assertThrows<SQLException> {
            reservationService.delete(mockedUser.email, mockedReservation.id)
        }

        exception.message `should be equal to` "Error while deleting reservation for user with email ${mockedUser.email}"

        verify(userRepository, times(1)).findByEmail(anyString())
        verify(venueRepository, times(1)).findById(anyInt())
        verify(reservationRepository, times(1)).deleteById(mockedReservation.id)
    }

    @Test
    fun `should delete reservation`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(mockedUser)
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))

        val result = reservationService.delete(mockedUser.email, mockedReservation.id)

        result.success `should be equal to` true
        result.message `should be equal to` "Reservation deleted successfully"

        verify(userRepository, times(1)).findByEmail(anyString())
        verify(venueRepository, times(1)).findById(anyInt())
        verify(reservationRepository, times(1)).deleteById(mockedReservation.id)
    }
}
