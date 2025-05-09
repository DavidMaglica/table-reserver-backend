package fipu.diplomski.dmaglica.venue

import fipu.diplomski.dmaglica.model.request.UpdateVenueRequest
import fipu.diplomski.dmaglica.repo.entity.VenueEntity
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.sql.SQLException
import java.util.*

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class UpdateVenueTest : BaseVenueServiceTest() {

    @Test
    fun `should throw if venue not found`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.empty())

        val exception = assertThrows<SQLException> {
            venueService.update(mockedVenue.id, null)
        }

        exception.message?.let { it `should be equal to` "Venue with id ${mockedVenue.id} not found" }

        verify(venueRepository, times(1)).findById(mockedVenue.id)
    }

    @Test
    fun `should return early if request is null`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))

        val result = venueService.update(mockedVenue.id, null)

        result.success `should be` false
        result.message `should be` "Request is not valid"

        verify(venueRepository, times(1)).findById(mockedVenue.id)
    }

    @Test
    fun `should return early if request does not change anything`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))

        val result = venueService.update(
            mockedVenue.id,
            UpdateVenueRequest(
                name = mockedVenue.name,
                location = mockedVenue.location,
                description = mockedVenue.description,
                typeId = mockedVenue.venueTypeId,
                workingHours = mockedVenue.workingHours
            )
        )

        result.success `should be` false
        result.message `should be` "Request does not update anything"

        verify(venueRepository, times(1)).findById(mockedVenue.id)
    }

    @Test
    fun `should throw if save fails`() {
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRepository.save(any())).thenThrow(RuntimeException("Save failed"))

        val exception = assertThrows<SQLException> {
            venueService.update(mockedVenue.id, UpdateVenueRequest(name = "New name"))
        }

        exception.message?.let { it `should be equal to` "Error while updating venue with id ${mockedVenue.id}" }

        verify(venueRepository, times(1)).findById(mockedVenue.id)
        verify(venueRepository, times(1)).save(any())
    }

    @Test
    fun `should update only select fields`() {
        val newVenue = VenueEntity().also {
            it.id = mockedVenue.id
            it.name = "New name"
            it.location = mockedVenue.location
            it.description = "New description"
            it.workingHours = mockedVenue.workingHours
            it.averageRating = mockedVenue.averageRating
            it.venueTypeId = mockedVenue.venueTypeId
        }
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRepository.save(any())).thenReturn(newVenue)

        val result = venueService.update(
            mockedVenue.id,
            UpdateVenueRequest(name = newVenue.name, description = newVenue.description)
        )

        verify(venueRepository).save(venueArgumentCaptor.capture())
        val savedVenue = venueArgumentCaptor.value

        result.success `should be equal to` true
        result.message `should be equal to` "Venue updated successfully"

        savedVenue.name `should be equal to` newVenue.name
        savedVenue.location `should be equal to` mockedVenue.location
        savedVenue.description `should be equal to` newVenue.description
        savedVenue.workingHours `should be equal to` mockedVenue.workingHours
        savedVenue.venueTypeId `should be equal to` mockedVenue.venueTypeId
    }

    @Test
    fun `should update venue`() {
        val newVenue = VenueEntity().also {
            it.id = mockedVenue.id
            it.name = "New name"
            it.location = "New location"
            it.description = "New description"
            it.workingHours = "New working hours"
            it.averageRating = mockedVenue.averageRating
            it.venueTypeId = 2
        }
        `when`(venueRepository.findById(anyInt())).thenReturn(Optional.of(mockedVenue))
        `when`(venueRepository.save(any())).thenReturn(newVenue)

        val result = venueService.update(
            mockedVenue.id,
            UpdateVenueRequest(
                name = newVenue.name,
                location = newVenue.location,
                description = newVenue.description,
                typeId = newVenue.venueTypeId,
                workingHours = newVenue.workingHours
            )
        )

        verify(venueRepository).save(venueArgumentCaptor.capture())
        val savedVenue = venueArgumentCaptor.value

        result.success `should be equal to` true
        result.message `should be equal to` "Venue updated successfully"

        savedVenue.name `should be equal to` newVenue.name
        savedVenue.location `should be equal to` newVenue.location
        savedVenue.description `should be equal to` newVenue.description
        savedVenue.workingHours `should be equal to` newVenue.workingHours
        savedVenue.venueTypeId `should be equal to` newVenue.venueTypeId
    }
}