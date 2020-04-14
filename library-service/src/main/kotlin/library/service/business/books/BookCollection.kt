import library.service.business.books.BookDataStore
import library.service.business.books.BookIdGenerator
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import javax.enterprise.context.ApplicationScoped

/**
 * This represents the book collection of this library application instance.
 *
 * It offers functions for common actions taken with a collection of books:
 *
 * - adding books
 * - finding books
 * - deleting books
 * - borrowing & returning books
 */
@ApplicationScoped
class BookCollection(
        // private val clock: Clock
        private val dataStore: BookDataStore,
        private val idGenerator: BookIdGenerator
        // private val eventDispatcher: EventDispatcher<BookEvent>
) {

    /**
     * Adds the given [Book] to the collection.
     *
     * The [Book] is stored in the collection's [BookDataStore] for future
     * usage.
     *
     * Dispatches a [BookAdded] domain event.
     *
     * @param book the book to add to the collection
     * @return the [BookRecord] for the created book data
     */
    fun addBook(book: Book): Book {

        val bookId = idGenerator.generate()
        val bookRecord = dataStore.createOrUpdate(BookRecord(bookId, book))

        System.out.println("bookId$bookId")
        System.out.println("bookRecord$bookRecord")

        //dispatch(bookAddedEvent(bookRecord))

        println(book)
        // return bookRecord
        return book
    }
}