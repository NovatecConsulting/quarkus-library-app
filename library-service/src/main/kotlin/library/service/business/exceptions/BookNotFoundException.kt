package library.service.business.exceptions

import library.service.business.books.domain.types.BookId

class BookNotFoundException(id: BookId)
    : NotFoundException("The book with ID: $id does not exist!")