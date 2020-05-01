package library.service.business.books.domain.types

import com.fasterxml.jackson.annotation.JsonAutoDetect
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.states.Borrowed

/** Person who [Borrowed] a [BookRecord]. */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Borrower(
        private val value: String
) {

    companion object {
        const val VALID_BORROWER_PATTERN = """(?U)[\w][\w -]*"""
    }

    override fun toString(): String = value

}