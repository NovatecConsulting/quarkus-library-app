package library.service.api.books

data class BookResource(
        val isbn: String,
        val title: String,
        val authors: List<String>?,
        val numberOfPages: Int?,
        val borrowed: Borrowed?
)

data class Borrowed(
        val by: String,
        val on: String
)