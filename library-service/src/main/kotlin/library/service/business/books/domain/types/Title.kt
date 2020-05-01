package library.service.business.books.domain.types

import com.fasterxml.jackson.annotation.JsonAutoDetect

/** The title of a book. */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Title(

        private val value: String

) {

    companion object {
        const val VALID_TITLE_PATTERN = """(?U)[\w $ASCII_SPECIAL_CHARACTERS]+"""
    }

    override fun toString(): String = value

}