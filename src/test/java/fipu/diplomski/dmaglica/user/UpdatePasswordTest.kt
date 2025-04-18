package fipu.diplomski.dmaglica.user

import fipu.diplomski.dmaglica.exception.UserNotFoundException
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles
import java.sql.SQLException

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class UpdatePasswordTest : BaseUserServiceTest() {

    companion object {
        const val NEW_PASSWORD = "password2"
    }

    @Test
    fun `should throw if user not found`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(null)

        val exception =
            assertThrows<UserNotFoundException> { userService.updatePassword(mockedUser.email, NEW_PASSWORD) }

        exception.message `should be equal to` "User with email ${mockedUser.email} does not exist"
    }

    @Test
    fun `should throw if can't update password`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(mockedUser)
        `when`(userRepository.save(any())).thenThrow(RuntimeException())

        val exception = assertThrows<SQLException> { userService.updatePassword(mockedUser.email, NEW_PASSWORD) }

        exception.message `should be equal to` "Error while updating password for user with email ${mockedUser.email}"

        verify(userRepository, times(1)).findByEmail(mockedUser.email)
    }

    @Test
    fun `should update password`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(mockedUser)

        val result = userService.updatePassword(mockedUser.email, NEW_PASSWORD)

        result.success `should be` true
        result.message `should be equal to` "Password for user with email ${mockedUser.email} successfully updated"

        verify(userRepository).save(userEntityArgumentCaptor.capture())
        val updatedUser = userEntityArgumentCaptor.value
        updatedUser.password `should be equal to` NEW_PASSWORD

        verify(userRepository, times(1)).findByEmail(mockedUser.email)
        verify(userRepository, times(1)).save(any())
    }
}
