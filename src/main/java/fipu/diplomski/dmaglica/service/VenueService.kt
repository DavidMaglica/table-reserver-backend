package fipu.diplomski.dmaglica.service

import fipu.diplomski.dmaglica.model.request.CreateVenueRequest
import fipu.diplomski.dmaglica.model.request.UpdateVenueRequest
import fipu.diplomski.dmaglica.model.response.BasicResponse
import fipu.diplomski.dmaglica.model.response.PagedResponse
import fipu.diplomski.dmaglica.repo.*
import fipu.diplomski.dmaglica.repo.entity.ReservationEntity
import fipu.diplomski.dmaglica.repo.entity.VenueEntity
import fipu.diplomski.dmaglica.repo.entity.VenueRatingEntity
import fipu.diplomski.dmaglica.repo.entity.VenueTypeEntity
import fipu.diplomski.dmaglica.util.getSurroundingHalfHours
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

@Service
class VenueService(
    private val venueRepository: VenueRepository,
    private val venueRatingRepository: VenueRatingRepository,
    private val venueTypeRepository: VenueTypeRepository,
    private val reservationRepository: ReservationRepository,
    private val imageService: ImageService,
    private val geolocationService: GeolocationService,
    private val userRepository: UserRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger(VenueService::class.java.name)
    }

    @Transactional(readOnly = true)
    fun get(venueId: Int): VenueEntity {
        val venue: VenueEntity = venueRepository.findById(venueId).orElseThrow {
            EntityNotFoundException("Venue with id: $venueId not found.")
        }
        val venueRating: List<VenueRatingEntity> = venueRatingRepository.findByVenueId(venueId)
        venue.averageRating = venueRating.map { it.rating }.average()
            .takeIf { it.isFinite() } ?: 0.0

        val currentTimestamp: LocalDateTime = LocalDateTime.now()
        val (lowerBound, upperBound) = getSurroundingHalfHours(currentTimestamp)

        val reservations = reservationRepository.findByVenueIdAndDatetimeBetween(
            venueId, lowerBound, upperBound
        )

        if (reservations.isNotEmpty()) {
            calculateCurrentAvailableCapacity(venue, reservations)
        } else {
            venue.availableCapacity = venue.maximumCapacity
        }

        return venue
    }

    @Transactional(readOnly = true)
    fun getAll(pageable: Pageable, searchQuery: String?, typeIds: List<Int>?): PagedResponse<VenueEntity> {
        val (lowerBound, upperBound) = getSurroundingHalfHours(LocalDateTime.now())
        val venues = venueRepository.findFilteredVenues(
            pageable = pageable,
            searchQuery = searchQuery,
            typeIds = typeIds,
        )
        return venuesToPagedResponse(venues, lowerBound, upperBound)
    }

    /**
     * Retrieves venues near specified coordinates with real-time availability.
     *
     * Behavior:
     * 1. Without coordinates: Returns venues in default location (Zagreb)
     * 2. With coordinates: Finds venues in current city + nearby cities (within radius 100km)
     *
     * @param pageable Pagination configuration
     * @param latitude Optional user latitude
     * @param longitude Optional user longitude
     * @return [PagedResponse] with:
     *   - content: List of venues nearby user with calculated:
     *     - averageRating
     *     - current availableCapacity
     *   - page: Current page number
     *   - size: Page size
     *   - totalElements: Total matching venues
     *
     * @implNote Uses geolocation service to determine nearby cities
     *
     */
    @Transactional(readOnly = true)
    fun getNearbyVenues(pageable: Pageable, latitude: Double?, longitude: Double?): PagedResponse<VenueEntity> {
        val currentTimestamp: LocalDateTime = LocalDateTime.now()
        val defaultLocation = "Zagreb"
        val (lowerBound, upperBound) = getSurroundingHalfHours(currentTimestamp)

        if (latitude == null || longitude == null) {
            val venues = venueRepository.findByLocation(defaultLocation, pageable)
            return venuesToPagedResponse(venues, lowerBound, upperBound)
        }

        val currentCity = geolocationService.getGeolocation(latitude, longitude)
        val nearbyCities = geolocationService.getNearbyCities(latitude, longitude)

        if (nearbyCities.isNullOrEmpty()) {
            val venues = venueRepository.findByLocation(currentCity, pageable)
            return venuesToPagedResponse(venues, lowerBound, upperBound)
        }

        nearbyCities.add(currentCity)

        val venues = venueRepository.findByLocationIn(nearbyCities, pageable)

        return venuesToPagedResponse(venues, lowerBound, upperBound)
    }

    /**
     * Retrieves recently added venues sorted by creation date (id descending).
     *
     * Includes:
     * - Real-time available capacity
     * - Current average ratings
     *
     * @param pageable Pagination configuration
     * @return [PagedResponse] with:
     *   - content: List of newest venues with calculated:
     *     - averageRating
     *     - current availableCapacity
     *   - page: Current page number
     *   - size: Page size
     *   - totalElements: Total matching venues
     *
     */
    @Transactional(readOnly = true)
    fun getNewVenues(pageable: Pageable): PagedResponse<VenueEntity> {
        val sortedPageable = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Order.desc("id"))
        )
        val currentTimestamp: LocalDateTime = LocalDateTime.now()
        val (lowerBound, upperBound) = getSurroundingHalfHours(currentTimestamp)

        val venues = venueRepository.findAll(sortedPageable)

        return venuesToPagedResponse(venues, lowerBound, upperBound)
    }

    /**
     * Retrieves venues with highest reservation volume.
     *
     * Calculation:
     * 1. Gets top venues by reservation count
     * 2. Enriches with venue details
     * 3. Calculates current availability
     *
     * @param pageable Pagination configuration
     * @return [PagedResponse] with:
     *   - content: List of matching venues ordered by popularity with calculated:
     *     - averageRating
     *     - current availableCapacity
     *   - page: Current page number
     *   - size: Page size
     *   - totalElements: Total matching venues
     *
     */
    @Transactional(readOnly = true)
    fun getTrendingVenues(pageable: Pageable): PagedResponse<VenueEntity> {
        val currentTimestamp: LocalDateTime = LocalDateTime.now()
        val (lowerBound, upperBound) = getSurroundingHalfHours(currentTimestamp)

        val topVenueStats = reservationRepository.findTopVenuesByReservationCount(pageable)

        val venueIds = topVenueStats.map { it.getVenueId() }.content
        val venues = venueRepository.findAllById(venueIds)

        val orderedVenues = venueIds.mapNotNull { venueId -> venues.find { it.id == venueId } }

        val paged = PageImpl(orderedVenues, pageable, topVenueStats.totalElements)

        return venuesToPagedResponse(paged, lowerBound, upperBound)
    }

    /**
     * Retrieves recommended venues based on quality and availability.
     *
     * Selection criteria:
     * - Average rating > 4.0
     * - Currently available capacity
     * - Sorted by rating then capacity
     *
     * @param pageable Pagination configuration
     * @return [PagedResponse] with:
     *   - content: List of highly-rated available venue with calculated:
     *     - averageRating
     *     - current availableCapacity
     *   - page: Current page number
     *   - size: Page size
     *   - totalElements: Total matching venues
     *
     * @implNote Excludes fully-booked venues regardless of rating
     *
     */
    @Transactional(readOnly = true)
    fun getSuggestedVenues(pageable: Pageable): PagedResponse<VenueEntity> {
        val currentTimestamp: LocalDateTime = LocalDateTime.now()
        val (lowerBound, upperBound) = getSurroundingHalfHours(currentTimestamp)

        val venues = venueRepository.findSuggestedVenues(pageable)

        return venuesToPagedResponse(venues, lowerBound, upperBound)
    }

    @Transactional(readOnly = true)
    fun getType(typeId: Int): String = venueTypeRepository.getReferenceById(typeId).type

    @Transactional(readOnly = true)
    fun getVenueRating(venueId: Int): Double = venueRepository.findById(venueId)
        .orElseThrow { EntityNotFoundException("Venue with id: $venueId not found.") }.averageRating

    @Transactional(readOnly = true)
    fun getAllRatings(venueId: Int): List<VenueRatingEntity> =
        venueRatingRepository.findByVenueId(venueId).sortedByDescending { it.id }

    @Transactional(readOnly = true)
    fun getAllTypes(): List<VenueTypeEntity> = venueTypeRepository.findAll()

    fun getVenueImages(venueId: Int): List<String> = imageService.getVenueImages(venueId)

    fun getMenuImages(venueId: Int): List<String> = imageService.getMenuImages(venueId)

    @Transactional
    fun create(request: CreateVenueRequest): BasicResponse {
        validateCreateRequest(request)?.let { return it }

        val venue = VenueEntity().apply {
            name = request.name
            location = request.location
            description = request.description
            workingHours = request.workingHours
            maximumCapacity = request.maximumCapacity
            availableCapacity = request.maximumCapacity
            venueTypeId = request.typeId
            averageRating = 0.0
        }

        try {
            venueRepository.save(venue)
        } catch (e: Exception) {
            logger.error(e) { "Error while creating venue: ${e.message}" }
            return BasicResponse(false, "Error while creating venue. Please try again later.")
        }

        return BasicResponse(true, "Venue ${request.name} created successfully.")
    }

    fun uploadVenueImage(venueId: Int, image: MultipartFile): BasicResponse =
        imageService.uploadVenueImage(venueId, image)

    fun uploadMenuImage(venueId: Int, image: MultipartFile): BasicResponse =
        imageService.uploadMenuImage(venueId, image)

    @Transactional
    fun update(venueId: Int, request: UpdateVenueRequest?): BasicResponse {
        val venue = venueRepository.findById(venueId).orElseThrow {
            EntityNotFoundException("Venue with id $venueId not found")
        }

        validateUpdateRequest(request)?.let { return it }

        if (!containsVenueChanges(request, venue)) return BasicResponse(
            false,
            "No modifications found. Please change at least one field."
        )

        val updatedAvailability: Int? = request?.maximumCapacity?.let { newMaxCapacity ->
            val currentReservations = venue.maximumCapacity - venue.availableCapacity
            when {
                newMaxCapacity < currentReservations -> return BasicResponse(
                    false,
                    "New maximum capacity cannot exceed current available capacity."
                )

                else -> newMaxCapacity - currentReservations
            }
        }

        venue.apply {
            name = request?.name ?: venue.name
            location = request?.location ?: venue.location
            workingHours = request?.workingHours ?: venue.workingHours
            maximumCapacity = request?.maximumCapacity ?: venue.maximumCapacity
            availableCapacity = updatedAvailability ?: venue.availableCapacity
            venueTypeId = request?.typeId ?: venue.venueTypeId
            description = request?.description ?: venue.description
        }

        try {
            venueRepository.save(venue)
        } catch (e: Exception) {
            logger.error { "Error while updating venue with id $venueId: ${e.message}" }
            return BasicResponse(false, "Error while updating venue. Please try again later.")
        }

        return BasicResponse(true, "Venue updated successfully.")
    }

    @Transactional
    fun rate(venueId: Int, userRating: Double, userId: Int, comment: String?): BasicResponse {
        if (userRating < 0.5 || userRating > 5.0) return BasicResponse(false, "Rating must be between 0.5 and 5.")

        val username = userRepository.findById(userId).getOrElse {
            logger.error { "User with id $userId not found." }
            return BasicResponse(false, "User with id $userId not found.")
        }.username

        val venue = venueRepository.findById(venueId).orElseThrow {
            EntityNotFoundException("Venue with id $venueId not found")
        }
        val venueRating = venueRatingRepository.findByVenueId(venueId)

        val newRatingEntity = VenueRatingEntity().apply {
            this.venueId = venueId
            this.rating = userRating
            this.username = username
            this.comment = comment
        }

        try {
            venueRatingRepository.save(newRatingEntity)
        } catch (e: Exception) {
            logger.error { "Error while updating rating for venue with id $venueId: ${e.message}" }
            return BasicResponse(false, "Error while updating rating. Please try again later.")
        }

        val newAverageRating = calculateNewAverageRating(venueRating, userRating)
        val updatedVenue = venue.apply { averageRating = newAverageRating }

        try {
            venueRepository.save(updatedVenue)
        } catch (e: Exception) {
            logger.error { "Error while updating venue with id $venueId after rating: ${e.message}" }
            return BasicResponse(false, "Error while updating venue after rating. Please try again later.")
        }

        return BasicResponse(true, "Venue with id $venueId successfully rated with rating $userRating.")
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
        try {
            venueRepository.deleteById(venueId)
        } catch (e: Exception) {
            logger.error { "Error while deleting venue with id $venueId: ${e.message}" }
            return BasicResponse(false, "Error while deleting venue. Please try again later.")
        }

        return BasicResponse(true, "Venue successfully deleted.")
    }

    private fun venuesToPagedResponse(
        page: Page<VenueEntity>,
        lowerBound: LocalDateTime,
        upperBound: LocalDateTime
    ): PagedResponse<VenueEntity> {
        if (page.isEmpty) return PagedResponse(
            content = emptyList(),
            page = page.number,
            size = page.size,
            totalElements = 0,
            totalPages = 0
        )
        val venueIds = page.content.map { it.id }
        val enrichedVenues = buildVenueStats(
            venueIds,
            lowerBound,
            upperBound,
            page.content
        )
        return PagedResponse(
            content = enrichedVenues,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages
        )
    }

    private fun validateCreateRequest(request: CreateVenueRequest): BasicResponse? = when {
        request.name.isBlank() -> BasicResponse(false, "Name cannot be empty.")
        request.location.isBlank() -> BasicResponse(false, "Location cannot be empty.")
        request.description.isBlank() -> BasicResponse(false, "Description cannot be empty.")
        request.workingHours.isBlank() -> BasicResponse(false, "Working hours cannot be empty.")
        request.maximumCapacity <= 0 -> BasicResponse(false, "Maximum capacity must be positive.")
        request.typeId <= 0 -> BasicResponse(false, "Invalid venue type id.")
        else -> null
    }

    private fun validateUpdateRequest(request: UpdateVenueRequest?): BasicResponse? = when {
        request == null ->
            BasicResponse(false, "Update request cannot be null. Provide at least one field to update.")

        request.name?.isBlank() == true ->
            BasicResponse(false, "Name is not valid.")

        request.location?.isBlank() == true ->
            BasicResponse(false, "Location is not valid.")

        request.description?.isBlank() == true ->
            BasicResponse(false, "Description is not valid.")

        request.typeId?.let { it <= 0 } == true ->
            BasicResponse(false, "Invalid venue type id.")

        request.workingHours?.isBlank() == true ->
            BasicResponse(false, "Working hours are not valid.")

        request.maximumCapacity?.let { it <= 0 } == true ->
            BasicResponse(false, "Maximum capacity is not valid.")

        else -> null
    }

    private fun containsVenueChanges(request: UpdateVenueRequest?, venue: VenueEntity): Boolean {
        if (request == null) return false

        return listOf(
            request.name?.takeIf { it != venue.name },
            request.location?.takeIf { it != venue.location },
            request.workingHours?.takeIf { it != venue.workingHours },
            request.maximumCapacity?.takeIf { it != venue.maximumCapacity },
            request.typeId?.takeIf { it != venue.venueTypeId },
            request.description?.takeIf { it != venue.description }
        ).any { it != null }
    }

    private fun calculateCurrentAvailableCapacity(venue: VenueEntity, reservations: List<ReservationEntity>) {
        val totalGuests = reservations.sumOf { it.numberOfGuests }

        venue.availableCapacity = venue.maximumCapacity - totalGuests
    }

    /**
     * Builds enriched venue statistics by aggregating ratings and reservations data.
     *
     * For each venue, calculates:
     * 1. Average rating from all user ratings (defaults to 0.0 if no ratings exist)
     * 2. Current available capacity based on active reservations within the specified time window
     *
     * @param venueIds List of venue ids to process
     * @param lowerBound Start of time window for reservation checks (inclusive)
     * @param upperBound End of time window for reservation checks (exclusive)
     * @param venues Base list of venue entities to enrich
     * @return List of enriched [VenueEntity] objects with:
     *   - averageRating: Calculated mean of all ratings (finite value)
     *   - availableCapacity: Updated based on active reservations
     *
     * @implNote This method:
     *   - Performs batch database lookups for efficiency
     *   - Modifies the input venue entities directly
     *   - Handles null/empty cases gracefully (default ratings and capacities)
     *   - Uses a single pass for all calculations after data aggregation
     *
     * @see calculateCurrentAvailableCapacity for capacity calculation details
     */
    private fun buildVenueStats(
        venueIds: List<Int>,
        lowerBound: LocalDateTime,
        upperBound: LocalDateTime,
        venues: List<VenueEntity>
    ): List<VenueEntity> {
        val ratings = venueRatingRepository.findByVenueIdIn(venueIds)
        val reservationsByVenueId =
            reservationRepository.findByDatetimeBetween(lowerBound, upperBound).groupBy { it.venueId }
        val averageRatingByVenueId = ratings.groupBy { it.venueId }
            .mapValues { (_, venueRatings) -> venueRatings.map { it.rating }.average() }

        for (venue in venues) {
            venue.averageRating = averageRatingByVenueId[venue.id] ?: 0.0

            val venueReservations = reservationsByVenueId[venue.id].orEmpty()
            if (venueReservations.isNotEmpty()) {
                calculateCurrentAvailableCapacity(venue, venueReservations)
            } else {
                venue.availableCapacity = venue.maximumCapacity
            }
        }

        return venues
    }
}
