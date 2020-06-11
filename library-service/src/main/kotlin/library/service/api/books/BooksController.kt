package library.service.api.books

import BookResourceAssembler
import library.service.api.books.payload.*
import library.service.business.books.BookCollection
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.*
import library.service.business.exceptions.BookNotFoundException
import org.jboss.resteasy.annotations.jaxrs.PathParam
import java.util.*
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo


@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BooksController(
        private val collection: BookCollection,
        private val assembler: BookResourceAssembler
) {

    @GET
    fun getBooks(): Response? {
        val allBooks = collection.getAllBooks()
        val bookResources = mutableListOf<BookResource>()
        for (record in allBooks){
            bookResources.add(assembler.toResource(record))
        }
        return Response.status(Response.Status.OK).entity(bookResources).build()
    }

    @POST
    fun postBook(@Valid body: CreateBookRequest, @Context uriInfo: UriInfo): Response? {
        val book = Book(
                isbn = Isbn13.parse(body.isbn!!),
                title = Title(body.title!!),
                authors = emptyList(),
                numberOfPages = null
        )
        val bookRecord = collection.addBook(book)
        return Response.status(Response.Status.CREATED).entity(assembler.toResource(bookRecord)).build()
    }

    @PUT
    @Path("/{id}/title")
    fun putBookTitle(@PathParam id: UUID, body: UpdateTitleRequest): Response? {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeTitle(Title(body.title!!))
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @PUT
    @Path("/{id}/authors")
    fun putBookAuthors(@PathParam id: UUID, body: UpdateAuthorsRequest): Response? {
        val bookRecord = collection.updateBook(BookId(id)) { it ->
            if (body.authors.isNullOrEmpty()) {
                throw BookNotFoundException(BookId(id))
            }
            it.changeAuthors(body.authors.map { Author(it) })

        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @DELETE
    @Path("/{id}/authors")
    fun deleteBookAuthors(@PathParam id: UUID): Response? {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeAuthors(emptyList())
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @PUT
    @Path("/{id}/numberOfPages")
    fun putBookNumberOfPages(@PathParam id: UUID, @Valid body: UpdateNumberOfPagesRequest): Response? {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(body.numberOfPages)
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @DELETE
    @Path("/{id}/numberOfPages")
    fun deleteBookNumberOfPages(@PathParam id: UUID): Response? {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(null)
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @GET
    @Path("/{id}")
    fun getBook(@PathParam id: UUID): Response? {
        val bookRecord = collection.getBook(BookId(id))
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @DELETE
    @Path("/{id}")
    fun deleteBook(@PathParam id: UUID): Response? {
        collection.removeBook(BookId(id))
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    @POST
    @Path("/{id}/borrow")
    fun postBorrowBook(@PathParam id: UUID, @Valid body: BorrowBookRequest): Response? {
        val bookRecord = collection.borrowBook(BookId(id), Borrower(body.borrower!!))
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

    @POST
    @Path("/{id}/return")
    fun postReturnBook(@PathParam id: UUID): Response? {
        val bookRecord = collection.returnBook(BookId(id))
        return Response.status(Response.Status.OK).entity(assembler.toResource(bookRecord)).build()
    }

}