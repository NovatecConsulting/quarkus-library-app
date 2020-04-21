
import io.quarkus.arc.DefaultBean
import java.time.Clock
import javax.enterprise.inject.Produces
import javax.inject.Singleton

@Singleton
class AppConfiguration() {

    @Produces
    @DefaultBean
    fun utcClock(): Clock = Clock.systemUTC()

}