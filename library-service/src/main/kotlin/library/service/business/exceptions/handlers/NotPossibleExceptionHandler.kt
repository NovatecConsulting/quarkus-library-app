
import library.service.api.ErrorDescription
import library.service.business.exceptions.NotPossibleException
import org.apache.http.HttpStatus
import java.time.Clock
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@ApplicationScoped
class NotPossibleExceptionHandler(
        private val clock: Clock) : ExceptionMapper<NotPossibleException> {

    override fun toResponse(p0: NotPossibleException?): Response {
        println("Message + ${p0?.message}")

        val errorDescription = p0?.message?.let {
            ErrorDescription(
                    status = HttpStatus.SC_CONFLICT,
                    error = "Conflict",
                    timestamp = OffsetDateTime.now(clock).toString(),
                    correlationId = "",
                    message = it
            )
        }
        return Response.status(HttpStatus.SC_CONFLICT).entity(errorDescription).build()
    }


}