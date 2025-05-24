package fipu.diplomski.dmaglica.service

import fipu.diplomski.dmaglica.exception.UserNotFoundException
import fipu.diplomski.dmaglica.model.data.NotificationOptions
import fipu.diplomski.dmaglica.model.data.Role
import fipu.diplomski.dmaglica.model.data.User
import fipu.diplomski.dmaglica.model.data.UserLocation
import fipu.diplomski.dmaglica.model.response.BasicResponse
import fipu.diplomski.dmaglica.repo.NotificationOptionsRepository
import fipu.diplomski.dmaglica.repo.UserRepository
import fipu.diplomski.dmaglica.repo.entity.NotificationOptionsEntity
import fipu.diplomski.dmaglica.repo.entity.UserEntity
import fipu.diplomski.dmaglica.util.dbActionWithTryCatch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val notificationOptionsRepository: NotificationOptionsRepository,
) {

    @Transactional
    fun signup(email: String, username: String, password: String): BasicResponse {
        userRepository.findByEmail(email)?.let {
            return BasicResponse(false, "User with email $email already exists")
        }

        val user = UserEntity().also {
            it.email = email
            it.username = username
            it.password = password
            it.roleId = Role.USER.ordinal
        }

        dbActionWithTryCatch("Error while saving user with email $email") {
            userRepository.save(user)

            val userNotificationOptions = NotificationOptionsEntity().also {
                it.userId = user.id
                it.locationServicesEnabled = false
                it.pushNotificationsEnabled = false
                it.emailNotificationsEnabled = false
            }
            notificationOptionsRepository.save(userNotificationOptions)
        }

        return BasicResponse(true, "User with email $email successfully created")
    }

    @Transactional(readOnly = true)
    fun login(email: String, password: String): BasicResponse {
        val user = userRepository.findByEmail(email)
            ?: return BasicResponse(false, "User with email $email does not exist")

        if (user.password.lowercase() != password.lowercase()) {
            return BasicResponse(false, "Incorrect password")
        }

        return BasicResponse(true, "User with email $email successfully logged in")
    }

    @Transactional(readOnly = true)
    fun getNotificationOptions(email: String): NotificationOptions {
        val user = findUserIfExists(email)

        val notificationOptions = notificationOptionsRepository.findByUserId(user.id)

        return NotificationOptions(
            pushNotificationsTurnedOn = notificationOptions.pushNotificationsEnabled,
            emailNotificationsTurnedOn = notificationOptions.emailNotificationsEnabled,
            locationServicesTurnedOn = notificationOptions.locationServicesEnabled,
        )
    }

    @Transactional(readOnly = true)
    fun getLocation(email: String): UserLocation? {
        val user = findUserIfExists(email)

        if (user.lastKnownLatitude == null || user.lastKnownLongitude == null) {
            return null
        }

        return UserLocation(latitude = user.lastKnownLatitude!!, longitude = user.lastKnownLongitude!!)
    }

    @Transactional
    fun updateEmail(email: String, newEmail: String): BasicResponse {
        val user = findUserIfExists(email)

        user.email = newEmail
        dbActionWithTryCatch("Error while updating email for user with email $email") {
            userRepository.save(user)
        }

        return BasicResponse(true, "Email for user with email $email updated to $newEmail")
    }

    @Transactional
    fun updateUsername(email: String, newUsername: String): BasicResponse {
        val user = findUserIfExists(email)

        user.username = newUsername
        dbActionWithTryCatch("Error while updating username for user with email $email") {
            userRepository.save(user)
        }

        return BasicResponse(true, "Username for user with email $email successfully updated")
    }

    @Transactional
    fun updatePassword(email: String, newPassword: String): BasicResponse {
        val user = findUserIfExists(email)

        user.password = newPassword
        dbActionWithTryCatch("Error while updating password for user with email $email") {
            userRepository.save(user)
        }

        return BasicResponse(true, "Password for user with email $email successfully updated")
    }

    @Transactional
    fun updateNotificationOptions(
        email: String,
        pushNotificationsTurnedOn: Boolean,
        emailNotificationsTurnedOn: Boolean,
        locationServicesTurnedOn: Boolean
    ): BasicResponse {
        val user = findUserIfExists(email)

        val notificationOptions = notificationOptionsRepository.findByUserId(user.id)

        notificationOptions.pushNotificationsEnabled = pushNotificationsTurnedOn
        notificationOptions.emailNotificationsEnabled = emailNotificationsTurnedOn
        notificationOptions.locationServicesEnabled = locationServicesTurnedOn
        dbActionWithTryCatch("Error while updating notification options for user with email $email") {
            notificationOptionsRepository.save(notificationOptions)
        }

        return BasicResponse(
            true,
            "Notification options for user with email $email successfully updated"
        )
    }

    @Transactional
    fun updateLocation(email: String, latitude: Double, longitude: Double): BasicResponse {
        val user = findUserIfExists(email)

        user.lastKnownLatitude = latitude
        user.lastKnownLongitude = longitude
        dbActionWithTryCatch("Error while updating location for user with email $email") {
            userRepository.save(user)
        }

        return BasicResponse(true, "Location for user with email $email successfully updated")
    }

    @Transactional
    fun delete(email: String): BasicResponse {
        val user = findUserIfExists(email)

        dbActionWithTryCatch("Error while deleting user with email $email") {
            userRepository.deleteById(user.id)
        }

        return BasicResponse(true, "User with email $email successfully deleted")
    }

    @Transactional(readOnly = true)
    fun getUser(email: String): User {
        val user = findUserIfExists(email)

        val notificationOptions = notificationOptionsRepository.findByUserId(user.id).let {
            NotificationOptions(
                pushNotificationsTurnedOn = it.pushNotificationsEnabled,
                emailNotificationsTurnedOn = it.emailNotificationsEnabled,
                locationServicesTurnedOn = it.locationServicesEnabled,
            )
        }

        return User(
            id = user.id,
            username = user.username,
            password = user.password,
            email = user.email,
            notificationOptions = notificationOptions,
            role = Role.entries[user.roleId],
            lastKnownLatitude = user.lastKnownLatitude,
            lastKnownLongitude = user.lastKnownLongitude,
        )
    }

    private fun findUserIfExists(email: String): UserEntity =
        userRepository.findByEmail(email) ?: throw UserNotFoundException("User with email $email does not exist")
}
