package fipu.diplomski.dmaglica.service

import fipu.diplomski.dmaglica.model.request.CreateVenueRequest
import fipu.diplomski.dmaglica.model.request.UpdateVenueRequest
import fipu.diplomski.dmaglica.model.response.BasicResponse
import fipu.diplomski.dmaglica.repo.VenueRatingRepository
import fipu.diplomski.dmaglica.repo.VenueRepository
import fipu.diplomski.dmaglica.repo.VenueTypeRepository
import fipu.diplomski.dmaglica.repo.entity.VenueEntity
import fipu.diplomski.dmaglica.repo.entity.VenueRatingEntity
import fipu.diplomski.dmaglica.repo.entity.VenueTypeEntity
import fipu.diplomski.dmaglica.util.dbActionWithTryCatch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.sql.SQLException

@Service
class VenueService(
    private val venueRepository: VenueRepository,
    private val venueRatingRepository: VenueRatingRepository,
    private val venueTypeRepository: VenueTypeRepository,
    private val imageService: ImageService,
) {

    @Transactional(readOnly = true)
    fun get(venueId: Int): VenueEntity {
        val venue: VenueEntity =
            venueRepository.findById(venueId).orElseThrow { SQLException("Venue with id: $venueId not found.") }
        val venueRating: List<VenueRatingEntity> = venueRatingRepository.findByVenueId(venueId)
        venue.averageRating = venueRating.map { it.rating }.average()
        return venue
    }

    @Transactional(readOnly = true)
    fun getAll(): List<VenueEntity> {
        val venues: List<VenueEntity> = venueRepository.findAll()

        for (venue in venues) {
            val ratings = venueRatingRepository.findByVenueId(venue.id).filter { it.venueId == venue.id }
            if (ratings.isNotEmpty()) {
                val averageRating = ratings.map { it.rating }.average()
                venue.averageRating = averageRating
            } else {
                venue.averageRating = 0.0
            }
        }

        return venues
    }

    @Transactional(readOnly = true)
    fun getType(typeId: Int): String =
        venueTypeRepository.getReferenceById(typeId).type

    @Transactional(readOnly = true)
    fun getVenueRating(venueId: Int): Double = venueRepository.findById(venueId)
        .orElseThrow { SQLException("Venue with id: $venueId not found.") }.averageRating

    @Transactional(readOnly = true)
    fun getAllTypes(): List<VenueTypeEntity> = venueTypeRepository.findAll()

    fun getVenueImages(venueId: Int, venueName: String) = imageService.getVenueImages(venueId, venueName)

    fun getMenuImage(venueId: Int, venueName: String) = imageService.getMenuImage(venueId, venueName)

    @Transactional
    fun create(request: CreateVenueRequest): BasicResponse {
        val venue = VenueEntity().also {
            it.id
            it.name = request.name
            it.location = request.location
            it.description = request.description
            it.workingHours = request.workingHours
            it.venueTypeId = request.typeId
            it.averageRating = 0.0
        }

        dbActionWithTryCatch("Error while saving venue: ${request.name}") {
            venueRepository.save(venue)
        }

        return BasicResponse(true, "Venue ${request.name} created successfully")
    }

    fun uploadVenueImage(venueId: Int, image: MultipartFile): BasicResponse =
        imageService.uploadVenueImage(venueId, image)

    fun uploadMenuImage(venueId: Int, image: MultipartFile): BasicResponse =
        imageService.uploadMenuImage(venueId, image)

    @Transactional
    fun update(venueId: Int, request: UpdateVenueRequest?): BasicResponse {
        val venue = venueRepository.findById(venueId)
            .orElseThrow { SQLException("Venue with id $venueId not found") }

        if (!isRequestValid(request)) return BasicResponse(false, "Request is not valid")

        if (!containsVenueChanges(request, venue)) return BasicResponse(false, "Request does not update anything")

        val updatedVenue = venue.also {
            it.name = request?.name ?: venue.name
            it.location = request?.location ?: venue.location
            it.workingHours = request?.workingHours ?: venue.workingHours
            it.venueTypeId = request?.typeId ?: venue.venueTypeId
            it.description = request?.description ?: venue.description
        }

        dbActionWithTryCatch("Error while updating venue with id $venueId") {
            venueRepository.save(updatedVenue)
        }

        return BasicResponse(true, "Venue updated successfully")
    }

    @Transactional
    fun rate(venueId: Int, userRating: Double): BasicResponse {
        if (userRating < 0.5 || userRating > 5.0) return BasicResponse(false, "Rating must be between 0.5 and 5")

        val venue = venueRepository.findById(venueId)
            .orElseThrow { SQLException("Venue with id $venueId not found") }
        val venueRating =
            venueRatingRepository.findByVenueId(venueId)

        val newRatingEntity = VenueRatingEntity().also {
            it.id
            it.venueId = venueId
            it.rating = userRating
        }
        dbActionWithTryCatch("Error while updating rating for venue with id $venueId") {
            venueRatingRepository.save(newRatingEntity)
        }

        val newAverageRating = calculateNewAverageRating(venueRating, userRating)
        val updatedVenue = venue.also { it.averageRating = newAverageRating }
        dbActionWithTryCatch("Error while updating venue with id $venueId") {
            venueRepository.save(updatedVenue)
        }

        return BasicResponse(true, "Venue with id $venueId successfully rated with rating $userRating")
    }

    private fun calculateNewAverageRating(
        venueRating: List<VenueRatingEntity>,
        userRating: Double
    ): Double {
        val cumulativeRating = venueRating.sumOf { it.rating } + userRating
        val cumulativeRatingCount = venueRating.size + 1

        return cumulativeRating / cumulativeRatingCount
    }

    @Transactional
    fun delete(venueId: Int): BasicResponse {
        dbActionWithTryCatch("Error while deleting venue with id: $venueId") {
            venueRepository.deleteById(venueId)
        }

        return BasicResponse(true, "Venue with id: $venueId successfully deleted")
    }

    private fun isRequestValid(request: UpdateVenueRequest?): Boolean = request?.let {
        it.name != null || it.location != null || it.workingHours != null ||
                it.typeId != null || it.description != null
    } ?: false

    private fun containsVenueChanges(request: UpdateVenueRequest?, venue: VenueEntity): Boolean {
        if (request == null) return false

        return listOf(
            request.name?.takeIf { it != venue.name },
            request.location?.takeIf { it != venue.location },
            request.workingHours?.takeIf { it != venue.workingHours },
            request.typeId?.takeIf { it != venue.venueTypeId },
            request.description?.takeIf { it != venue.description }
        ).any { it != null }
    }
}
