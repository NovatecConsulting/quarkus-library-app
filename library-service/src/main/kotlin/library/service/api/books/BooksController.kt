package library.service.api.books

import BookResourceAssembler
import library.service.api.books.payload.*
import library.service.business.books.BookCollection
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.*
import library.service.business.exceptions.MalformedValueException
import org.jboss.resteasy.annotations.jaxrs.PathParam
import java.util.*
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.*


@Path("/api/books")
@Produces("application/hal+json")
@Consumes("application/json")
class BooksController(
        private val collection: BookCollection,
        private val assembler: BookResourceAssembler
) {

    @GET
    fun getBooks(@Context uriInfo: UriInfo, @Context securityContext: SecurityContext): Response {
        val allBooks = collection.getAllBooks()
        val bookResources = mutableListOf<BookResource>()
        for (record in allBooks){
            assembler.toResource(uriInfo, record, securityContext)?.let { bookResources.add(it) }
        }
        return Response.status(Response.Status.OK).entity(bookResources).build()
    }

    @POST
    fun postBook(@Context uriInfo: UriInfo, @Valid body: CreateBookRequest, @Context securityContext: SecurityContext): Response {
        val book = Book(
                isbn = Isbn13.parse(body.isbn!!),
                title = Title(body.title!!),
                authors = emptyList(),
                numberOfPages = null
        )
        val bookRecord = collection.addBook(book)
        return Response.status(Response.Status.CREATED).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @PUT
    @Path("/{id}/title")
    fun putBookTitle(@Context uriInfo: UriInfo, @PathParam id: UUID, body: UpdateTitleRequest,
                     @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.updateBook(BookId(id)) { it ->
            if (body.title.isNullOrBlank()) {
                throw MalformedValueException("The field 'title' must not be blank.")
            }
            it.changeTitle(Title(body.title))
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @PUT
    @Path("/{id}/authors")
    fun putBookAuthors(@Context uriInfo: UriInfo, @PathParam id: UUID, body: UpdateAuthorsRequest, @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.updateBook(BookId(id)) { it ->
            if (body.authors.isNullOrEmpty()) {
                throw MalformedValueException("The field 'authors' must not be empty.")
            }
            it.changeAuthors(body.authors.map { Author(it) })

        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @DELETE
    @Path("/{id}/authors")
    fun deleteBookAuthors(@Context uriInfo: UriInfo, @PathParam id: UUID, @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeAuthors(emptyList())
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @PUT
    @Path("/{id}/numberOfPages")
    fun putBookNumberOfPages(@Context uriInfo: UriInfo, @PathParam id: UUID, @Valid body: UpdateNumberOfPagesRequest, @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(body.numberOfPages)
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @DELETE
    @Path("/{id}/numberOfPages")
    fun deleteBookNumberOfPages(@Context uriInfo: UriInfo, @PathParam id: UUID, @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(null)
        }
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }

    @GET
    @Path("/{id}")
    fun getBook(@Context uriInfo: UriInfo, @PathParam id: String, @Context securityContext: SecurityContext): Response? {
        try {
            val uuid = UUID.fromString(id)
            val bookRecord = collection.getBook(BookId(uuid))
            return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
        } catch (e: IllegalArgumentException) {
            throw MalformedValueException("The request's 'id' parameter is malformed.")
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteBook(@PathParam id: String): Response {
        try {
            val uuid = UUID.fromString(id)
            collection.removeBook(BookId(uuid))
            return Response.status(Response.Status.NO_CONTENT).build()
        } catch (e: IllegalArgumentException) {
            throw MalformedValueException("The request's 'id' parameter is malformed.")
        }
    }

    @POST
    @Path("/{id}/borrow")
    fun postBorrowBook(@Context uriInfo: UriInfo, @PathParam id: String, @Valid body: BorrowBookRequest, @Context securityContext: SecurityContext): Response? {
        try {
            val uuid = UUID.fromString(id)
            val bookRecord = collection.borrowBook(BookId(uuid), Borrower(body.borrower!!))
            return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
        } catch (e: IllegalArgumentException) {
            throw MalformedValueException("The request's 'id' parameter is malformed.")
        }
    }

    @POST
    @Path("/{id}/return")
    fun postReturnBook(@Context uriInfo: UriInfo, @PathParam id: UUID, @Context securityContext: SecurityContext): Response {
        val bookRecord = collection.returnBook(BookId(id))
        return Response.status(Response.Status.OK).entity(assembler.toResource(uriInfo, bookRecord, securityContext)).build()
    }
}

