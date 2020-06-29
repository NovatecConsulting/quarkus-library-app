package library.service.correlation

import javax.annotation.Priority
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

private const val correlationIdHeader = "X-Correlation-ID"

@Provider
@Priority(Priorities.HEADER_DECORATOR)
@ApplicationScoped
class CorrelationIdFilter(
        private val correlationIdHolder: CorrelationIdHolder
): ContainerRequestFilter {


    override fun filter(containerRequestContext: ContainerRequestContext) {
        val correlationId = containerRequestContext.getHeaderString(correlationIdHeader)

        if (correlationId != null) {
            correlationIdHolder.set(correlationId)
        }
    }

}

@Provider
@Priority(Priorities.HEADER_DECORATOR)
@ApplicationScoped
class CorrelationIdResponseFilter(
        private val correlationIdHolder: CorrelationIdHolder
): ContainerResponseFilter {
    override fun filter(containerRequestContext: ContainerRequestContext,
                        containerResponseContext: ContainerResponseContext) {

        correlationIdHolder.remove()
        var correlationId = containerRequestContext.getHeaderString(correlationIdHeader)

        if (correlationId == null) {
            correlationId = CorrelationId.generate()
        }

        correlationIdHolder.set(correlationId)
        containerResponseContext.headers.add(correlationIdHeader, correlationId)
    }

}