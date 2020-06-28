
import library.service.api.ErrorDescription
import library.service.correlation.CorrelationIdHolder
import org.apache.http.HttpStatus
import java.time.Clock
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@ApplicationScoped
class IllegalArgumentExceptionHandler (
        private val correlationIdHolder: CorrelationIdHolder,
        private val clock: Clock) : ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(p0: IllegalArgumentException?): Response {

        val errorDescription = p0?.message?.let {
            ErrorDescription(
                    status = HttpStatus.SC_BAD_REQUEST,
                    error = "Bad Request",
                    timestamp = OffsetDateTime.now(clock).toString(),
                    correlationId = correlationIdHolder.get(),
                    message = "The request's body could not be read. It is either empty or malformed."
            )
        }
        return Response.status(HttpStatus.SC_BAD_REQUEST).entity(errorDescription).build()
    }

}