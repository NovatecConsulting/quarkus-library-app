package library.service.business.books.domain.types

import com.fasterxml.jackson.annotation.JsonAutoDetect

/** The author of a book. */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Author(
        private val value: String
) {

    override fun toString(): String = value

}