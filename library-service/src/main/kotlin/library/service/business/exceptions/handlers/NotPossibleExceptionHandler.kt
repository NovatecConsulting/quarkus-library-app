
import library.service.api.ErrorDescription
import library.service.business.exceptions.NotPossibleException
import library.service.correlation.CorrelationIdHolder
import org.apache.http.HttpStatus
import java.time.Clock
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@ApplicationScoped
class NotPossibleExceptionHandler(
        private val correlationIdHolder: CorrelationIdHolder,
        private val clock: Clock) : ExceptionMapper<NotPossibleException> {

    override fun toResponse(notPossibleException: NotPossibleException): Response {

        val errorDescription = notPossibleException.message?.let {
            ErrorDescription(
                    status = HttpStatus.SC_CONFLICT,
                    error = "Conflict",
                    timestamp = OffsetDateTime.now(clock).toString(),
                    correlationId = correlationIdHolder.get(),
                    message = it
            )
        }
        return Response.status(HttpStatus.SC_CONFLICT).type(MediaType.APPLICATION_JSON).entity(errorDescription).build()
    }


}