
import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import javax.inject.Singleton

@Singleton
class MongoBookDataStore(
) : BookDataStore {

    private val list = mutableListOf<BookRecord>()

    override fun existsById(bookId: BookId): Boolean {
        val book: BookRecord? = list.find { it.id == bookId }
        return book != null
    }

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        list.add(bookRecord)
        return bookRecord
    }

    override fun delete(bookRecord: BookRecord) {
        println("DELETED BOOK = $bookRecord")
    }

    override fun findById(bookId: BookId): BookRecord? {
        val book: BookRecord? = list.find { it.id == bookId }
        println("DB book = $book")
        return book
    }

    override fun findAll(): List<BookRecord> {
        return list
    }

}