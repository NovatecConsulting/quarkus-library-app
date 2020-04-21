import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoBookDataStore(
) : BookDataStore {

    val list = mutableListOf<BookRecord>()

    override fun existsById(bookId: BookId): Boolean {
        return false
    }

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        list.add(bookRecord)
        println("List contains = $list")
        println("Size of list = ${list.size}")
        return bookRecord
    }

    override fun findAll(): List<BookRecord> {
        return list
    }

}