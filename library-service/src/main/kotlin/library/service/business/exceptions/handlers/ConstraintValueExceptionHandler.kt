
import library.service.api.ErrorDescription
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
        private val clock: Clock) : ExceptionMapper<ConstraintViolationException> {


    override fun toResponse(p0: ConstraintViolationException?): Response {

        println("Constraint Value Exception")

        val detailList = p0?.message!!.split(", ").toMutableList()
        val it = detailList.listIterator()

        while (it.hasNext()) {

            val message = it.next().replaceFirst(":","")
            val subString = message.substringAfterLast(".")

            it.set("The field $subString")

        }

        val errorDescription = ErrorDescription(
                status = HttpStatus.SC_BAD_REQUEST,
                error = "Bad Request",
                timestamp = OffsetDateTime.now(clock).toString(),
                correlationId = "",
                message = "The request's body is invalid. See details...",
                details = detailList.toList()
        )
        return Response.status(HttpStatus.SC_BAD_REQUEST).entity(errorDescription).build()
    }

}