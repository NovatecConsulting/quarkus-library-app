package library.service.api

import library.service.business.books.BookCollection
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/api/books")
class BooksController(

        private val collection: BookCollection
) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun postBooks(book: Book): BookRecord {

        // Temporary
        val bookRecord = collection.addBook(book)
        println("bookRecordController = $bookRecord")

        return bookRecord

    }
}