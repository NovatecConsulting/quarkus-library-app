package library.service.business.books

import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId

/**
 * Interface defining all methods which need to be implemented by a data store
 * in order to handle the persistence of books.
 */
interface BookDataStore {

    /**
     * Creates or updates the given [BookRecord] in the data store.
     *
     * The previous existence of a record has to be verified by the caller.
     * This method will override any existing data based on the record's
     * [BookId]!
     *
     * @param bookRecord the [BookRecord] to create or update
     * @return the created / updated [BookRecord]
     */
    fun createOrUpdate(bookRecord: BookRecord): BookRecord

    /**
     * Checks if there exists a [BookRecord] for the given [BookId].
     *
     * @return `true` if a record exists, otherwise `false`
     */
    fun existsById(bookId: BookId): Boolean



}