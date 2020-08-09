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
import java.util.*
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
    private final val id = BookId.generate()
    private final val book = Books.CLEAN_CODE
    private final val availableBookRecord = availableBook(id, book)
    private val borrowedBookRecord = borrowedBook(id, book, "Uncle Bob", "2017-08-20T12:34:56.789Z")
    private val correlationId = UUID.randomUUID().toString()
    private val localhost = "http://localhost:8081"

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

    //================================================================================
    // /api/books
    //================================================================================

    @Test
    fun `GET - when there are no books, the response only contains a self link`() {

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
                    },
                    "_links": {
                        "self": {
                            "href": "${localhost}/api/books/53397dc0-932d-4198-801a-3e00b2742ba7"
                        },
                        "return": {
                            "href": "${localhost}/api/books/53397dc0-932d-4198-801a-3e00b2742ba7/return"
                        }
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



        given().auth().basic("user", "us3r")
            .`when`().get("/api/books")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `GET - when there are books, the response contains them with all relevant links`() {
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
                    "borrowed": null,
                    "_links": {
                        "self": {
                            "href": "${localhost}/api/books/883a2931-325b-4482-8972-8cb6f7d33816"
                        },
                        "borrow": {
                            "href": "${localhost}/api/books/883a2931-325b-4482-8972-8cb6f7d33816/borrow"
                        }
                    }
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
                    },
                    "_links": {
                        "self": {
                            "href": "${localhost}/api/books/53397dc0-932d-4198-801a-3e00b2742ba7"
                        },
                        "return": {
                            "href": "${localhost}/api/books/53397dc0-932d-4198-801a-3e00b2742ba7/return"
                        }
                    }
                }
            ]
        """

        given().auth().basic("user", "us3r")
            .`when`().get("/api/books")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST - creates a book and responds with its resource representation`() {

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
                        "borrowed": null,
                        "_links": {
                            "self": {
                                "href": "${localhost}/api/books/${bookId}"
                            },
                            "borrow": {
                                "href": "${localhost}/api/books/${bookId}/borrow"
                            },
                            "delete": {
                                "href": "${localhost}/api/books/${bookId}"
                            }
                        }
                    }
                """

        given().auth().basic("curator", "curat0r")
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books").then()
            .statusCode(HttpStatus.SC_CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST - 400 BAD REQUEST for invalid ISBN`() {
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
                        "correlationId": "$correlationId",
                        "message": "The request's body is invalid. See details...",
                        "details": [
                            "The field isbn must match \"(\\d{3}-?)?\\d{10}\""
                        ]
                    }
                """

        given()
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `POST - 400 BAD REQUEST for malformed request`() {

        val requestBody = """
                """

        val expectedResponse = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The request's body could not be read. It is either empty or malformed."
                    }
                """

        given()
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .header("X-Correlation-ID", correlationId)
            .`when`().post("/api/books").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    //================================================================================
    // /api/books/{id}
    //================================================================================

    @Test
    fun `GET - 400 BAD REQUEST for missing required properties`() {

        val requestBody = """
                    {
                    }
                """

        val expectedResponse = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "timestamp":"2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The request's body is invalid. See details...",
                        "details": [
                            "The field isbn must not be blank",
                            "The field title must not be blank"
                        ]
                    }
                """

        given()
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .header("X-Correlation-ID", correlationId)
            .`when`().post("/api/books").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `GET - 404 NOT FOUND for non-existing book`() {

        val id = BookId.generate()

        val expectedResponse = """
                {
                    "status": 404,
                    "error": "Not Found",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The book with ID: $id does not exist!"
                }
            """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .`when`().get("/api/books/$id")
            .then().statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `GET - 400 BAD REQUEST for malformed ID`() {

        val expectedResponse = """
                {
                    "status": 400,
                    "error": "Bad Request",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The request's body is invalid. See details...",
                    "details": [
                        "The request's 'id' parameter is malformed."
                    ]
                }
            """

        given()
            .header("X-Correlation-ID", correlationId)
            .`when`().get("/api/books/malformed-id")
            .then().statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `DELETE - existing book is deleted and response is empty 204 NO CONTENT`() {

        every { bookDataStore.findById(id) } returns availableBookRecord
        every { bookDataStore.delete(availableBookRecord) } returns Unit

        given()
            .auth().basic("curator", "curat0r")
            .`when`().delete("/api/books/$id")
            .then().statusCode(HttpStatus.SC_NO_CONTENT).contentType("")
    }

    @Test
    fun `DELETE - 404 NOT FOUND for non-existing book`() {

        val expectedResponse = """
                    {
                        "status": 404,
                        "error": "Not Found",
                        "timestamp":"2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The book with ID: $id does not exist!"
                    }
                """

        given()
            .auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .`when`().delete("/api/books/$id").then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `DELETE - 400 BAD REQUEST for malformed ID`() {

        val expectedResponse = """
                {
                    "status": 400,
                    "error": "Bad Request",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The request's body is invalid. See details...",
                    "details": [
                        "The request's 'id' parameter is malformed."
                    ]
                }
            """

        given()
            .header("X-Correlation-ID", correlationId)
            .`when`().delete("/api/books/malformed-id")
            .then().statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    //================================================================================
    // /api/books/{id}/authors
    //================================================================================

    @Test
    fun `PUT - replaces authors of book and responds with its resource representation`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val requestBody = """
                    { 
                        "authors": ["Foo", "Bar"] 
                    }
                """

        val expectedResponse = """
                    {
                        "isbn": "${book.isbn}",
                        "title": "${book.title}",
                        "authors": [
                            "Foo",
                            "Bar"
                        ],
                        "numberOfPages": ${book.numberOfPages},
                        "borrowed": null,
                        "_links": {
                            "self": {
                                "href": "${localhost}/api/books/${id}"
                            },
                            "borrow": {
                                "href": "${localhost}/api/books/${id}/borrow"
                            },
                            "delete": {
                                "href": "${localhost}/api/books/${id}"
                            }
                        }
                    }
                """

        given().auth().basic("curator", "curat0r")
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/authors").then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }


    @Test
    fun `PUT - 404 NOT FOUND for non-existing book`() {

        val requestBody = """
                    { 
                        "authors": ["Foo", "Bar"] 
                    }
                """

        val expectedResponse = """
                {
                    "status": 404,
                    "error": "Not Found",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The book with ID: $id does not exist!"
                }
            """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/authors")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `PUT - 400 BAD REQUEST for missing required properties`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val requestBody = """
                    {
                    }
                """

        val expectedResponse = """
                {
                    "status": 400,
                    "error": "Bad Request",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The request's body is invalid. See details...",
                    "details": [
                        "The field 'authors' must not be empty."
                    ]
                }
            """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/authors")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `DELETE - removes authors from book and responds with its resource representation`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val expectedResponse = """
                {
                    "isbn": "${book.isbn}",
                    "title": "${book.title}",
                    "authors": [],
                    "numberOfPages": ${book.numberOfPages
                        },
                    "borrowed": null,
                    "_links": {
                        "self": {
                            "href": "${localhost}/api/books/${id}"
                        },
                        "borrow": {
                            "href": "${localhost}/api/books/${id}/borrow"
                        },
                        "delete": {
                            "href": "${localhost}/api/books/${id}"
                        }
                    }
                }
            """

        given().auth().basic("curator", "curat0r")
            .`when`().delete("/api/books/$id/authors")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `DELETE Author - 404 NOT FOUND for non-existing book`() {

        val expectedResponse = """
                {
                    "status": 404,
                    "error": "Not Found",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The book with ID: $id does not exist!"
                }
            """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .`when`().delete("/api/books/$id/authors")
            .then().contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    //================================================================================
    // /api/books/{id}/authors
    //================================================================================

    @Test
    fun `POST - borrows book and responds with its updated resource representation`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val requestBody = """
                    { 
                        "borrower": "Uncle Bob" 
                    }
                """

        val expectedResponse = """
                    {
                        "isbn": "9780132350884",
                        "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
                        "authors": [
                            "Robert C. Martin",
                            "Dean Wampler"
                        ],
                        "numberOfPages": 462,
                        "borrowed": {
                            "by": "Uncle Bob",
                            "on": "2017-08-20T12:34:56.789Z"
                        },
                        "_links": {
                            "self": {
                                "href": "${localhost}/api/books/${id}"
                            },
                            "return": {
                                "href": "${localhost}/api/books/${id}/return"
                            }
                        }
                    }
                """

        given()
            .auth().basic("user", "us3r")
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/borrow").then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST - 409 CONFLICT for already borrowed book`() {

        every { bookDataStore.findById(id) } returns borrowedBookRecord

        val requestBody = """
                    { 
                        "borrower": "Uncle Bob" 
                    }
                """

        val expectedResponse = """
                    {
                        "status": 409,
                        "error": "Conflict",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The book with ID: $id is already borrowed!"
                    }   
                """

        given()
            .auth().basic("user", "us3r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/borrow").then()
            .statusCode(HttpStatus.SC_CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test
    fun `POST - 404 NOT FOUND for non-existing book`() {

        val requestBody = """
                    { 
                        "borrower": "Uncle Bob" 
                    }
                """

        val expectedResponse = """
                    {
                        "status": 404,
                        "error": "Not Found",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The book with ID: $id does not exist!"
                    }   
                """

        given()
            .auth().basic("user", "us3r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/borrow").then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST - 400 BAD REQUEST for missing required properties`() {

        val requestBody = """
                    {
                    }
                """

        val expectedResponse = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The request's body is invalid. See details...",
                        "details": [
                            "The field borrower must not be null"
                        ]
                    }
                """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/borrow").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST Borrow - 400 BAD REQUEST for malformed request`() {

        val requestBody = """
                """

        val expectedResponse = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The request's body could not be read. It is either empty or malformed."
                    }
                """

        given()
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .header("X-Correlation-ID", correlationId)
            .`when`().post("/api/books/$id/borrow").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `POST Borrow - 400 BAD REQUEST for malformed ID`() {

        val requestBody = """
                { 
                    "borrower": "Uncle Bob" 
                }
                """

        val expectedResponse = """
                {
                    "status": 400,
                    "error": "Bad Request",
                    "timestamp":"2017-08-20T12:34:56.789Z",
                    "correlationId": "$correlationId",
                    "message": "The request's body is invalid. See details...",
                    "details": [
                        "The request's 'id' parameter is malformed."
                    ]
                }
            """

        given()
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/malformed-id/borrow")
            .then().statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    //================================================================================
    // /api/books/{id}/numberOfPages
    //================================================================================

    @Test
    fun `PUT - replaces number of pages of book and responds with its resource representation`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val requestBody = """
                    {
                        "title": "New Title"
                    }
                """

        val expectedResponse = """
                    {
                        "isbn": "9780132350884",
                        "title": "New Title",
                        "authors": [
                            "Robert C. Martin",
                            "Dean Wampler"
                        ],
                        "numberOfPages": 462,
                        "borrowed": null,
                        "_links": {
                            "self": {
                                "href": "${localhost}/api/books/${id}"
                            },
                            "borrow": {
                                "href": "${localhost}/api/books/${id}/borrow"
                            },
                            "delete": {
                                "href": "${localhost}/api/books/${id}"
                            }
                        }
                    }
                """

        given().auth().basic("curator", "curat0r")
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/title").then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `PUT Title - 404 NOT FOUND for non-existing book`() {

        val requestBody = """
                    {
                        "title": "New Title"
                    }
                """

        val expectedResponse = """
                    {
                        "status": 404,
                        "error": "Not Found",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The book with ID: $id does not exist!"
                    }
                """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/title").then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }

    @Test
    fun `PUT Title - 400 BAD REQUEST for missing required properties`() {

        every { bookDataStore.findById(id) } returns availableBookRecord

        val requestBody = """
                    {
                    }
                """

        val expectedResponse = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "timestamp": "2017-08-20T12:34:56.789Z",
                        "correlationId": "$correlationId",
                        "message": "The request's body is invalid. See details...",
                        "details": [
                            "The field 'title' must not be blank."
                        ]
                    }
                """

        given().auth().basic("curator", "curat0r")
            .header("X-Correlation-ID", correlationId)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/title").then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JsonMatcher.jsonEqualTo(expectedResponse))

    }


    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
        .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))

}