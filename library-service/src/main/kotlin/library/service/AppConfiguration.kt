import io.quarkus.arc.DefaultBean
import java.time.Clock
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces

@Dependent
class AppConfiguration() {

    @Produces
    @DefaultBean
    fun utcClock(): Clock = Clock.systemUTC()

}