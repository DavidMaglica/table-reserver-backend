package fipu.diplomski.dmaglica.util

class Paths {
    companion object {
        const val USER = "/user"
        const val SIGNUP = "/signup"
        const val LOGIN = "/login"
        const val GET_USER = "/get"
        const val GET_NOTIFICATION_OPTIONS = "/get-notification-options"
        const val GET_LOCATION = "/get-location"
        const val UPDATE_EMAIL = "/update-email"
        const val UPDATE_USERNAME = "/update-username"
        const val UPDATE_PASSWORD = "/update-password"
        const val UPDATE_LOCATION = "/update-location"
        const val UPDATE_NOTIFICATION_OPTIONS = "/update-notification-options"
        const val DELETE_USER = "/delete"

        const val VENUE = "/venue"
        const val CREATE_VENUE = "/create"
        const val UPLOAD_VENUE_IMAGE = "/upload-image"
        const val GET_VENUE = "/get"
        const val GET_ALL_VENUES = "/get-all"
        const val GET_VENUE_TYPE = "/get-type"
        const val GET_VENUE_RATING = "/get-rating"
        const val GET_ALL_VENUE_TYPES = "/get-all-types"
        const val GET_VENUE_MENU = "/get-menu"
        const val UPLOAD_MENU_IMAGE = "/upload-menu-image"
        const val UPDATE_VENUE = "/update"
        const val RATE_VENUE = "/rate"
        const val DELETE_VENUE = "/delete"

        const val RESERVATION = "/reservation"
        const val CREATE_RESERVATION = "/create"
        const val GET_RESERVATIONS = "/get-all"
        const val UPDATE_RESERVATION = "/update"
        const val DELETE_RESERVATION = "/delete"

        const val GEOLOCATION = "/geolocation"
        const val FETCH_GEOLOCATION = "/fetch-geolocation"
        const val GET_GEOLOCATION = "/get-geolocation"
        const val GET_NEARBY_CITIES = "/get-nearby-cities"

        const val SUPPORT = "/support"
        const val SEND_EMAIL = "/send-email"
    }
}
