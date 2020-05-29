package library.service.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import javax.inject.Singleton

/**
 * Is used as the response body in case an error response is send to the caller.
 *
 * Information like the [timestamp] and [correlationId] can be used to identify
 * corresponding log entries in case a consumer is reporting issues.
 */
@JsonInclude(NON_EMPTY)
@Singleton
data class ErrorDescription(
        val status: Int,
        val error: String,
        val timestamp: String,
        val correlationId: String?,
        val message: String,
        val details: List<String> = emptyList()
)