package library.service.api

import library.service.business.books.BookCollection
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api/books")
class BooksController(

        @Inject
        val collection: BookCollection
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