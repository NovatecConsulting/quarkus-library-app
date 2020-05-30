package library.service.database

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import java.util.*

data class BookDocument @BsonCreator constructor(
        @BsonId val id: UUID,
        @BsonProperty("isbn")  val isbn: String,
        @BsonProperty("title")  val title: String,
        @BsonProperty("authors") val authors: List<String>?,
        @BsonProperty("numberOfPages") val numberOfPages: Int?,
        @BsonProperty("borrowed") val borrowed: BorrowedState?
)

data class BorrowedState @BsonCreator constructor(
        @BsonProperty("by")  val by: String,
        @BsonProperty("on")  val on: String
)