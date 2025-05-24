package fipu.diplomski.dmaglica.controller

import fipu.diplomski.dmaglica.model.request.CreateVenueRequest
import fipu.diplomski.dmaglica.model.request.UpdateVenueRequest
import fipu.diplomski.dmaglica.repo.entity.VenueEntity
import fipu.diplomski.dmaglica.repo.entity.VenueTypeEntity
import fipu.diplomski.dmaglica.service.VenueService
import fipu.diplomski.dmaglica.util.Paths
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(Paths.VENUE)
class VenueController(
    private val venueService: VenueService
) {

    @GetMapping(Paths.GET_VENUE)
    fun getVenue(
        @RequestParam("venueId") venueId: Int,
    ): VenueEntity = venueService.get(venueId)

    @GetMapping(Paths.GET_ALL_VENUES)
    fun getAllVenues(): List<VenueEntity> = venueService.getAll()

    @GetMapping(Paths.GET_VENUE_TYPE)
    fun getVenueType(
        @RequestParam("typeId") typeId: Int,
    ): String = venueService.getType(typeId)

    @GetMapping(Paths.GET_VENUE_RATING)
    fun getVenueRating(
        @RequestParam("venueId") venueId: Int,
    ) = venueService.getVenueRating(venueId)

    @GetMapping(Paths.GET_ALL_VENUE_TYPES)
    fun getAllVenueTypes(): List<VenueTypeEntity> = venueService.getAllTypes()

    @GetMapping
    fun getVenueImages(
        @RequestParam("venueId") venueId: Int,
        @RequestParam("venueName") venueName: String,
    ) = venueService.getVenueImages(venueId, venueName)

    @GetMapping(Paths.GET_VENUE_MENU)
    fun getMenuImage(
        @RequestParam("venueId") venueId: Int,
        @RequestParam("venueName") venueName: String,
    ) = venueService.getMenuImage(venueId, venueName)

    @PostMapping(Paths.CREATE_VENUE)
    fun createVenue(
        @RequestBody request: CreateVenueRequest,
    ) = venueService.create(request)

    @PostMapping(Paths.UPLOAD_VENUE_IMAGE)
    fun uploadVenueImage(
        @RequestParam("venueId") venueId: Int,
        @RequestParam("image") image: MultipartFile
    ) = venueService.uploadVenueImage(venueId, image)

    @PostMapping(Paths.UPLOAD_MENU_IMAGE)
    fun uploadMenuImage(
        @RequestParam("venueId") venueId: Int,
        @RequestParam("image") image: MultipartFile
    ) = venueService.uploadMenuImage(venueId, image)

    @PatchMapping("${Paths.UPDATE_VENUE}/{venueId}")
    fun updateVenue(
        @PathVariable("venueId") venueId: Int,
        @RequestBody(required = false) request: UpdateVenueRequest?
    ) = venueService.update(venueId, request)

    @PostMapping(Paths.RATE_VENUE)
    fun rateVenue(
        @RequestParam("venueId") venueId: Int,
        @RequestParam("rating") rating: Double,
    ) = venueService.rate(venueId, rating)

    @DeleteMapping(Paths.DELETE_VENUE)
    fun deleteVenue(
        @RequestParam("venueId") venueId: Int,
    ) = venueService.delete(venueId)
}
