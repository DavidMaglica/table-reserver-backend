package fipu.diplomski.dmaglica.geolocation

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class GetNearbyCitiesTest : AbstractGeolocationTest() {
}