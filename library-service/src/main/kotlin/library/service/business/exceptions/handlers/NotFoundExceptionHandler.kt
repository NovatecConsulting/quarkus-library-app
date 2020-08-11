
import library.service.api.ErrorDescription
import library.service.business.exceptions.NotFoundException
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
class NotFoundExceptionHandler (
        private val correlationIdHolder: CorrelationIdHolder,
        private val clock: Clock) : ExceptionMapper<NotFoundException> {

    override fun toResponse(notFoundException: NotFoundException): Response {

        val errorDescription = notFoundException.message?.let {
            ErrorDescription(
                    status = HttpStatus.SC_NOT_FOUND,
                    error = "Not Found",
                    timestamp = OffsetDateTime.now(clock).toString(),
                    correlationId = correlationIdHolder.get(),
                    message = it
            )
        }
        return Response.status(HttpStatus.SC_NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(errorDescription).build()
    }


}