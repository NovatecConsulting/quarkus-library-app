
import library.service.api.ErrorDescription
import library.service.business.exceptions.MalformedValueException
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
class MalformedValueExceptionHandler(
        private val correlationIdHolder: CorrelationIdHolder,
        private val clock: Clock) : ExceptionMapper<MalformedValueException> {

    override fun toResponse(malformedValueException: MalformedValueException): Response? {

        val detailList = malformedValueException.message!!.split(", ").toMutableList()

        val errorDescription = malformedValueException.message?.let {
            ErrorDescription(
                    status = HttpStatus.SC_BAD_REQUEST,
                    error = "Bad Request",
                    timestamp = OffsetDateTime.now(clock).toString(),
                    correlationId = correlationIdHolder.get(),
                    message = "The request's body is invalid. See details...",
                    details = detailList.toList()
            )
        }
        return Response.status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(errorDescription).build()
    }

}