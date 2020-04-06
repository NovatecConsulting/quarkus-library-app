import library.service.business.books.domain.composites.Book

data class BookRecord(
        val id: BookId,
        val book: Book
        //val state: BookState = Available
) {

}