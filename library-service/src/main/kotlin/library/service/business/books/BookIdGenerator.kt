package library.service.business.books

import library.service.business.books.domain.types.BookId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class BookIdGenerator(
        private val dataStore: BookDataStore
) {

    fun generate(): BookId {
        var bookId = BookId.generate()
        if (dataStore.existsById(bookId)) {
            System.out.println("Id already exists")
            bookId = generate()
        }
        return bookId
    }

}