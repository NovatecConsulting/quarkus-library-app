
import library.service.api.books.BookResource
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.states.Available
import library.service.business.books.domain.states.Borrowed
import javax.inject.Singleton

@Singleton
class BookResourceAssembler(
) {
    fun toResource(bookRecord: BookRecord): BookResource {
        return instantiateResource(bookRecord)
    }

    private fun instantiateResource(bookRecord: BookRecord): BookResource {
        val bookState = bookRecord.state

        return BookResource(
                isbn = bookRecord.book.isbn.toString(),
                title = bookRecord.book.title.toString(),
                authors = bookRecord.book.authors.map { it.toString() },
                numberOfPages = bookRecord.book.numberOfPages,
                borrowed = when (bookState) {
                    is Available -> null
                    is Borrowed -> library.service.api.books.Borrowed(by = "${bookState.by}", on = "${bookState.on}")
                }
        )
    }


}