package library.service.business.exceptions

import library.service.business.books.domain.types.BookId

class BookAlreadyReturnedException(id: BookId)
    : NotPossibleException("The book with ID: $id was already returned!")