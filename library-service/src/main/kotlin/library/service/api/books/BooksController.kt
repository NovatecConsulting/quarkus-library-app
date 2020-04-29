package library.service.api.books

import BookResourceAssembler
import library.service.api.books.payload.CreateBookRequest
import library.service.business.books.BookCollection
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Isbn13
import library.service.business.books.domain.types.Title
import org.jboss.resteasy.annotations.jaxrs.PathParam
import java.util.*
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BooksController(

        val collection: BookCollection,
        val assembler: BookResourceAssembler
) {

    @GET
    fun getBooks(): List<BookRecord> {
        val allBooks = collection.getAllBooks()
        println("allBooks = $allBooks")
        return allBooks
    }

    @POST
    fun postBook(@Valid body: CreateBookRequest): BookRecord {
        val book = Book(
                isbn = Isbn13.parse(body.isbn!!),
                title = Title(body.title!!),
                authors = emptyList(),
                numberOfPages = null
        )
        val bookRecord = collection.addBook(book)
        return assembler.toResource(bookRecord)
    }

    @PUT
    @Path("/{id}/title")
    fun putBookTitle(@PathParam id: String): String {
        return id
    }

    @PUT
    @Path("/{id}/authors")
    fun putBookAuthors(@PathParam id: String): String {
        return id
    }

    @DELETE
    @Path("/{id}/authors")
    fun deleteBookAuthors(@PathParam id: String): String {
        return id
    }

    @PUT
    @Path("/{id}/numberOfPages")
    fun putBookNumberOfPages(@PathParam id: String): String {
        return id
    }

    @DELETE
    @Path("/{id}/numberOfPages")
    fun deleteBookNumberOfPages(@PathParam id: String): String {
        return id
    }

    @GET
    @Path("/{id}")
    fun getBook(@PathParam id: UUID): BookRecord {
        println("UUID = $id")

        val bookRecord = collection.getBook(BookId(id))
        println("bookRecord = $bookRecord")
        return bookRecord
    }

    @DELETE
    @Path("/{id}")
    fun deleteBook() {

    }

    @POST
    @Path("/{id}/borrow")
    fun postBorrowBook() {

    }

    @POST
    @Path("/{id}/return")
    fun postReturnBook() {

    }

}