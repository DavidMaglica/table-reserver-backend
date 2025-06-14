package fipu.diplomski.dmaglica.mobile.venue

import jakarta.persistence.EntityNotFoundException
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class RateVenueTest : BaseVenueServiceTest() {

    @Test
    fun `should return early if rating is not valid`() {
        val response = venueService.rate(mockedVenue.id, 6.0)

        response.success `should be` false
        response.message `should be equal to` "Rating must be between 0.5 and 5."

        verifyNoInteractions(venueRatingRepository, venueRepository)
    }

    @Test
    fun `should throw if venue not found`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFoundException> {
            venueService.rate(mockedVenue.id, 3.0)
        }

        exception.message?.let { it `should be equal to` "Venue with id ${mockedVenue.id} not found" }

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verifyNoInteractions(venueRatingRepository)
    }

    @Test
    fun `should return failure response if saving rating fails`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRatingRepository.findByVenueId(anyInt())).thenReturn(listOf(mockedRating))
        `when`(venueRatingRepository.save(any())).thenThrow(RuntimeException())

        val response = venueService.rate(mockedVenue.id, 3.0)

        response.success `should be equal to` false
        response.message `should be equal to` "Error while updating rating. Please try again later."

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verify(venueRatingRepository, times(1)).findByVenueId(mockedVenue.id)
        verify(venueRatingRepository, times(1)).save(venueRatingArgumentCaptor.capture())
        verifyNoMoreInteractions(venueRatingRepository, venueRepository)
    }

    @Test
    fun `should return failure response if saving venue fails`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRatingRepository.findByVenueId(anyInt())).thenReturn(listOf(mockedRating))
        `when`(venueRepository.save(any())).thenThrow(RuntimeException())

        val response = venueService.rate(mockedVenue.id, 3.0)

        response.success `should be equal to` false
        response.message `should be equal to` "Error while updating venue after rating. Please try again later."

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verify(venueRatingRepository, times(1)).findByVenueId(mockedVenue.id)
        verify(venueRatingRepository, times(1)).save(venueRatingArgumentCaptor.capture())
        verify(venueRepository, times(1)).save(venueArgumentCaptor.capture())
        verifyNoMoreInteractions(venueRatingRepository, venueRepository)
    }

    @Test
    fun `should insert new rating and update if does not exist earlier`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRatingRepository.findByVenueId(anyInt())).thenReturn(emptyList())

        val response = venueService.rate(mockedVenue.id, 3.0)

        response.success `should be` true
        response.message `should be equal to` "Venue with id ${mockedVenue.id} successfully rated with rating 3.0."

        verify(venueRatingRepository).save(venueRatingArgumentCaptor.capture())
        val updatedRating = venueRatingArgumentCaptor.value
        updatedRating.venueId `should be equal to` mockedVenue.id
        updatedRating.rating `should be equal to` 3.0

        verify(venueRepository).save(venueArgumentCaptor.capture())
        val updatedVenue = venueArgumentCaptor.value
        updatedVenue.id `should be equal to` mockedVenue.id
        updatedVenue.averageRating `should be equal to` 3.0

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verify(venueRatingRepository, times(1)).findByVenueId(mockedVenue.id)
        verify(venueRatingRepository, times(1)).save(venueRatingArgumentCaptor.capture())
        verify(venueRepository, times(1)).save(venueArgumentCaptor.capture())
    }

    @Test
    fun `should insert new rating and calculate new average correctly`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRatingRepository.findByVenueId(anyInt())).thenReturn(listOf(mockedRating))

        val response = venueService.rate(mockedVenue.id, 5.0)

        response.success `should be` true
        response.message `should be equal to` "Venue with id ${mockedVenue.id} successfully rated with rating 5.0."

        verify(venueRatingRepository).save(venueRatingArgumentCaptor.capture())
        val updatedRating = venueRatingArgumentCaptor.value
        updatedRating.venueId `should be equal to` mockedVenue.id
        updatedRating.rating `should be equal to` 5.0

        verify(venueRepository).save(venueArgumentCaptor.capture())
        val updatedVenue = venueArgumentCaptor.value
        updatedVenue.id `should be equal to` mockedVenue.id
        updatedVenue.averageRating `should be equal to` 4.5

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verify(venueRatingRepository, times(1)).findByVenueId(mockedVenue.id)
        verify(venueRatingRepository, times(1)).save(venueRatingArgumentCaptor.capture())
        verify(venueRepository, times(1)).save(venueArgumentCaptor.capture())
    }

}