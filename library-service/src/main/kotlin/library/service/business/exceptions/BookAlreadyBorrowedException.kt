package library.service.business.exceptions

import library.service.business.books.domain.types.BookId

class BookAlreadyBorrowedException(id: BookId)
    : NotPossibleException("The book with ID: $id is already borrowed!")