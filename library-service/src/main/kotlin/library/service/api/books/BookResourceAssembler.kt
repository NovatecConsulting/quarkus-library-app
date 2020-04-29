import library.service.business.books.domain.BookRecord
import javax.inject.Singleton

@Singleton
class BookResourceAssembler(
) {
    fun toResource(bookRecord: BookRecord): BookRecord {
        return bookRecord
    }
}