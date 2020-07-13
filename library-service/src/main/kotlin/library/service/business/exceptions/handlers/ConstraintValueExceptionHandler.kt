import library.service.api.ErrorDescription
import library.service.correlation.CorrelationIdHolder
import org.apache.http.HttpStatus
import java.time.Clock
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@ApplicationScoped
class ConstraintValueExceptionHandler(
    private val correlationIdHolder: CorrelationIdHolder,
    private val clock: Clock
) : ExceptionMapper<ConstraintViolationException> {


    override fun toResponse(constraintViolationException: ConstraintViolationException): Response {

        val detailList = constraintViolationException.message!!.split(", ").toMutableList()
        val it = detailList.listIterator()

        while (it.hasNext()) {

            val message = it.next().replaceFirst(":", "")
            val subString = message.substringAfterLast(".")

            it.set("The field $subString")

        }

        val errorDescription = ErrorDescription(
            status = HttpStatus.SC_BAD_REQUEST,
            error = "Bad Request",
            timestamp = OffsetDateTime.now(clock).toString(),
            correlationId = correlationIdHolder.get(),
            message = "The request's body is invalid. See details...",
            details = detailList.toList()
        )
        return Response.status(HttpStatus.SC_BAD_REQUEST).entity(errorDescription).build()
    }

}