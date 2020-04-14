import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoBookDataStore(

) : BookDataStore {
    override fun existsById(bookId: BookId): Boolean {
        return false
    }

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        return bookRecord
    }
}