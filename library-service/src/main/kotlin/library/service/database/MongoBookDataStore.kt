
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import library.service.database.BookDocument
import library.service.database.Mapper
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.*
import javax.inject.Singleton


@Singleton
class MongoBookDataStore(
        private val mongoClient: MongoClient,
        private val bookRecordToDocumentMapper: Mapper<BookRecord, BookDocument>,
        private val bookDocumentToRecordMapper: Mapper<BookDocument, BookRecord>
) : BookDataStore {


    override fun existsById(bookId: BookId): Boolean {
        val book = getCollection().find(eq("_id", UUID.fromString(bookId.toString()))).first()
        return book != null
    }

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        val document = bookRecordToDocumentMapper.map(bookRecord)
        val updateResult = getCollection().replaceOne(
                eq("_id", document.id),
                document,
                ReplaceOptions().upsert(true))

        return if (updateResult.upsertedId == null) {
            // Update element, when document matches id
            val updatedDocument = getCollection().find(eq("_id", document.id)).first()
            bookDocumentToRecordMapper.map(updatedDocument)

        } else {
            // Create new element, when no document matches id
            val updatedDocument = getCollection().find(eq("_id", updateResult.upsertedId)).first()
            bookDocumentToRecordMapper.map(updatedDocument)
        }

    }

    override fun delete(bookRecord: BookRecord) {
        val document = bookRecordToDocumentMapper.map(bookRecord)
        getCollection().deleteOne(eq("_id", document.id))
    }

    override fun findById(id: BookId): BookRecord? {
        val book = getCollection().find(eq("_id", UUID.fromString(id.toString()))).first()
        if (book != null) {
            return bookDocumentToRecordMapper.map(book)
        }
        return null
    }

    override fun findAll(): List<BookRecord> {
        val documents = getCollection().find().toList()
        val bookRecord = mutableListOf<BookRecord>()
        documents.forEach { bookRecord.add(bookDocumentToRecordMapper.map(it)) }
        return bookRecord
    }

    private fun getCollection(): MongoCollection<BookDocument> {

        val database = "library-service"
        val collectionName = "books"
        val codeRegistries = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(UuidCodecProvider(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

        return mongoClient.getDatabase(database).withCodecRegistry(codeRegistries).getCollection(collectionName, BookDocument::class.java)
    }

}