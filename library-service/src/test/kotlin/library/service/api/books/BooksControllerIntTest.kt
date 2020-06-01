package library.service.api.books

import io.mockk.every
import io.mockk.mockk
import io.quarkus.test.Mock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import library.service.business.books.BookDataStore
import library.service.business.books.BookIdGenerator
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import org.apache.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import utils.JsonMatcher
import utils.MutableClock
import utils.classification.IntegrationTest
import java.time.OffsetDateTime
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.ws.rs.core.MediaType

val bookDataStore = mockk<BookDataStore>()
val bookIdGenerator = mockk<BookIdGenerator>()

@IntegrationTest
@QuarkusTest
internal class BooksControllerIntTest {

    @Inject
    lateinit var clock: MutableClock

    @Produces
    @Mock
    fun bookDataStore(): BookDataStore = bookDataStore

    @Produces
    @Mock
    fun bookIdGenerator(): BookIdGenerator = bookIdGenerator

    @BeforeEach
    fun setTime() {
        clock.setFixedTime("2017-08-20T12:34:56.789Z")
    }

    @BeforeEach
    fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }

    }

    @Test
    fun `when there are no books, the response only contains a self link`() {

        val expectedResponse = """
            [
              {
                "isbn": "9780137081073",
                "title": "Clean Coder: A Code of Conduct for Professional Programmers",
                "authors": [
                  "Robert C. Martin"
                ],
                "numberOfPages": 256,
                "borrowed": {
                  "by": "Uncle Bob",
                  "on": "2017-08-20T12:34:56.789Z"
                }
              }
            ]
        """

        val borrowedBook = borrowedBook(
                id = BookId.from("53397dc0-932d-4198-801a-3e00b2742ba7"),
                book = Books.CLEAN_CODER,
                borrowedBy = "Uncle Bob",
                borrowedOn = "2017-08-20T12:34:56.789Z"
        )

        every { bookDataStore.findAll() } returns listOf(borrowedBook)

        given()
                .`when`().get("/api/books")
                .then().contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `when there are books, the response contains them with all relevant links`() {
        val availableBook = availableBook(
                id = BookId.from("883a2931-325b-4482-8972-8cb6f7d33816"),
                book = Books.CLEAN_CODE
        )

        val borrowedBook = borrowedBook(
                id = BookId.from("53397dc0-932d-4198-801a-3e00b2742ba7"),
                book = Books.CLEAN_CODER,
                borrowedBy = "Uncle Bob",
                borrowedOn = "2017-08-20T12:34:56.789Z"
        )

        every { bookDataStore.findAll() } returns listOf(availableBook, borrowedBook)

        val expectedResponse = """
            [
              {
                "isbn": "9780132350884",
                "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
                "authors": [
                  "Robert C. Martin",
                  "Dean Wampler"
                ],
                "numberOfPages": 462,
                "borrowed": null
              },
              {
                "isbn": "9780137081073",
                "title": "Clean Coder: A Code of Conduct for Professional Programmers",
                "authors": [
                  "Robert C. Martin"
                ],
                "numberOfPages": 256,
                "borrowed": {
                  "by": "Uncle Bob",
                  "on": "2017-08-20T12:34:56.789Z"
                }
              }
            ]
        """

        given()
                .`when`().get("/api/books")
                .then().contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test fun `creates a book and responds with its resource representation`() {
        val bookId = BookId.generate()
        every { bookIdGenerator.generate() } returns bookId

        val requestBody = """
                    {
                      "isbn": "9780132350884",
                      "title": "Clean Code: A Handbook of Agile Software Craftsmanship"
                    }
                """

        val expectedResponse = """
                    {
                       "isbn": "9780132350884",
                       "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
                       "authors": [],
                       "numberOfPages": null,
                       "borrowed": null
                    }
                """

        given()
                .contentType(MediaType.APPLICATION_JSON).body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test fun `400 BAD REQUEST for invalid ISBN`() {
        val requestBody = """
                    {
                      "isbn": "abcdefghij",
                      "title": "Clean Code: A Handbook of Agile Software Craftsmanship"
                    }
                """

        val expectedResponse = """
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "timestamp": "2017-08-20T12:34:56.789Z",
                      "message": "This is not a valid ISBN-13 number: abcdefghij"
                    }
                """

        given()
                .contentType(MediaType.APPLICATION_JSON).body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }



    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
            .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))

}