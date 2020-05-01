package library.service.api.books.payload

import com.fasterxml.jackson.annotation.JsonCreator
import library.service.business.books.domain.types.Title
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/** Request body used when updating a book's title. */
data class UpdateTitleRequest @JsonCreator constructor(

        @field:NotBlank
        @field:Size(min = 1, max = 256)
        @field:Pattern(regexp = Title.VALID_TITLE_PATTERN)
        val title: String?
)