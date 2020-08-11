
import library.service.api.books.BookResource
import library.service.api.books.Links
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.states.Available
import library.service.business.books.domain.states.Borrowed
import javax.inject.Singleton
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo


@Singleton
class BookResourceAssembler(
) {
    fun toResource(uriInfo: UriInfo, bookRecord: BookRecord, securityContext: SecurityContext): BookResource? {
        return instantiateResource(uriInfo, bookRecord, securityContext)
    }

    private fun instantiateResource(uriInfo: UriInfo, bookRecord: BookRecord, securityContext: SecurityContext): BookResource {
        val bookState = bookRecord.state
        val links = mutableMapOf<String, Links>()
        val baseUri = uriInfo.baseUri.toString()
        val isCurator = securityContext.isUserInRole("curator")

        links["self"] = Links(baseUri + "api/books/" + bookRecord.id)

        when (bookRecord.state) {
            is Available -> links["borrow"] = Links(baseUri + "api/books/" + bookRecord.id + "/borrow")
            is Borrowed -> links["return"] = Links(baseUri + "api/books/" + bookRecord.id + "/return")
        }

        if (isCurator) {
            links["delete"] = Links(baseUri + "api/books/" + bookRecord.id)
        }


        return BookResource(
                isbn = bookRecord.book.isbn.toString(),
                title = bookRecord.book.title.toString(),
                authors = bookRecord.book.authors.map { it.toString() },
                numberOfPages = bookRecord.book.numberOfPages,
                borrowed = when (bookState) {
                    is Available -> null
                    is Borrowed -> library.service.api.books.Borrowed(by = "${bookState.by}", on = "${bookState.on}")
                },
                _links = links
        )
    }


}