package library.service.api.books.payload

import com.fasterxml.jackson.annotation.JsonCreator
import javax.validation.constraints.NotEmpty

/** Request body used when updating a book's title. */
data class UpdateAuthorsRequest @JsonCreator constructor(
        @get:NotEmpty
        val authors: List<String>?
)