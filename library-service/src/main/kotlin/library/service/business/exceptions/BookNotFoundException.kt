package library.service.business.exceptions

import library.service.business.books.domain.types.BookId
import org.bson.BsonValue
import java.util.*

class BookNotFoundException(id: BsonValue?)
    : NotFoundException("The book with ID: $id does not exist!")