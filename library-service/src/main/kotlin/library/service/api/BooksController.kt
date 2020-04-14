package library.service.api

import BookCollection
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.Isbn13
import library.service.business.books.domain.types.Title
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/api/books")
class BooksController(
        @Inject
        private val collection: BookCollection
) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun postBooks(book: Book): Book {

        return collection.addBook(book)

    }
}