package library.service.api.books.payload

import com.fasterxml.jackson.annotation.JsonCreator
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/** Request body used when updating a book's number of pages. */
data class UpdateNumberOfPagesRequest @JsonCreator constructor(
        @field:NotNull
        @field:Min(1)
        val numberOfPages: Int?
)