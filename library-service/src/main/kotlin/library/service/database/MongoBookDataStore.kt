
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import library.service.database.BookDocument
import library.service.database.Mapper
import javax.inject.Singleton

@Singleton
class MongoBookDataStore (
        private val mongoClient: MongoClient,
        private val bookRecordToDocumentMapper: Mapper<BookRecord, BookDocument>,
        private val bookDocumentToRecordMapper: Mapper<BookDocument, BookRecord>
) : BookDataStore {


    private val list = mutableListOf<BookRecord>()

    override fun existsById(bookId: BookId): Boolean {
        val book: BookRecord? = list.find { it.id == bookId }
        return book != null
    }

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        val document = bookRecordToDocumentMapper.map(bookRecord)
        val updatedDocument = save(document)
        return bookDocumentToRecordMapper.map(updatedDocument)
    }

    private fun save(bookDocument: BookDocument): BookDocument {
        getCollection().replaceOne(
                and(eq("_id", bookDocument.id)),
                bookDocument,
                UpdateOptions().upsert(true))
        return bookDocument
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
        val documents = getCollection().find()
        val bookRecord = mutableListOf<BookRecord>()
        documents.forEach { bookRecord.add(bookDocumentToRecordMapper.map(it)) }
        return bookRecord
    }

    private fun getCollection(): MongoCollection<BookDocument> {
        return mongoClient.getDatabase("library-service").getCollection("books",
                BookDocument::class.java)
    }

}