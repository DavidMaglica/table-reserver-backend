package fipu.diplomski.dmaglica.user

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class LoginTest : UserServiceTest() {

    @Test
    fun `should return BasicResponse with success false when user is not found`() {
        `when`(userRepository.getByEmail(anyString())).thenReturn(null)

        val response = userService.login("wrongEmail@email.com", "wrongPassword")

        response.success `should be equal to` false
        response.message `should be equal to` "User with email wrongEmail@email.com does not exist"

        verify(userRepository, times(1)).getByEmail(anyString())
    }

    @Test
    fun `should throw SQLException when getByEmail throws`() {
        `when`(userRepository.getByEmail(anyString())).thenThrow(RuntimeException())

        assertThrows<RuntimeException> { userService.login(mockedUser.email, mockedUser.password) }

        verify(userRepository, times(1)).getByEmail(anyString())
    }

    @Test
    fun `when user is found correct user should be returned`() {
        `when`(userRepository.getByEmail(mockedUser.email)).thenReturn(mockedUser)

        val result = userService.login(mockedUser.email, mockedUser.password)

        result.success `should be equal to` true
        result.message `should be equal to` "User with email ${mockedUser.email} successfully logged in"

        verify(userRepository, times(1)).getByEmail(anyString())
    }
}