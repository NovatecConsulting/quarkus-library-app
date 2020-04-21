package library.service.api

import library.service.api.books.payload.CreateBookRequest
import library.service.business.books.BookCollection
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.Isbn13
import library.service.business.books.domain.types.Title
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BooksController(

        val collection: BookCollection
) {

    @GET
    fun getBooks(): List<BookRecord> {
        val allBooks = collection.getAllBooks()
        println("allBooks = $allBooks")
        return allBooks
    }

    @POST
    fun postBooks(@Valid body: CreateBookRequest): BookRecord {

        val book = Book(
                isbn = Isbn13.parse(body.isbn!!),
                title = Title(body.title!!),
                authors = emptyList(),
                numberOfPages = null
        )

        println("book = $book")
        val bookRecord = collection.addBook(book)
        println("bookRecordController = $bookRecord")

        return bookRecord
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    fun putBookTitle() {

    }
}