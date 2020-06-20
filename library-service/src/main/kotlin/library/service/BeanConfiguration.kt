
import io.quarkus.arc.DefaultBean
import io.quarkus.runtime.Startup
import java.time.Clock
import javax.enterprise.inject.Produces
import javax.inject.Singleton

@Singleton
@Startup
class BeanConfiguration() {

    @Produces
    @DefaultBean
    fun utcClock(): Clock = Clock.systemUTC()

}