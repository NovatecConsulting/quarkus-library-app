package library.service.business.books

import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.events.BookAdded
import library.service.business.books.domain.events.BookEvent
import library.service.business.books.domain.types.BookId
import library.service.business.events.EventDispatcher
import library.service.business.exceptions.BookNotFoundException
import java.time.Clock
import java.time.OffsetDateTime
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

        private val clock: Clock,
        private val dataStore: BookDataStore,
        private val idGenerator: BookIdGenerator,
        private val eventDispatcher: EventDispatcher<BookEvent>
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
    fun addBook(book: Book): BookRecord {

        val bookId = idGenerator.generate()
        val bookRecord = dataStore.createOrUpdate(BookRecord(bookId, book))

        println("clock = $clock")
        println("bookId = $bookId")
        println("bookRecord = $bookRecord")

        dispatch(bookAddedEvent(bookRecord))

        println(book)

        return bookRecord
    }

    fun getBook(id: BookId): BookRecord {
        return dataStore.findById(id) ?: throw BookNotFoundException(id)
    }

    /**
     * Gets a list of all [BookRecord] currently part of this collection.
     *
     * The books are looked up in the collection's [BookDataStore]. If there
     * are no books in the data store, an empty list is returned.
     *
     * @return a list of all [BookRecord]
     */
    fun getAllBooks(): List<BookRecord> {
        return dataStore.findAll()
    }

    private fun bookAddedEvent(bookRecord: BookRecord) = BookAdded(timestamp = now(), bookRecord = bookRecord)

    private fun dispatch(event: BookEvent) = eventDispatcher.dispatch(event)
    private fun now() = OffsetDateTime.now(clock)

}