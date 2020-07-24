package library.service.security

import io.mockk.every
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import library.service.api.books.bookDataStore
import library.service.api.books.bookIdGenerator
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import utils.Books
import utils.classification.AcceptanceTest
import java.time.OffsetDateTime
import javax.ws.rs.core.MediaType


@QuarkusTest
@AcceptanceTest
internal class SecurityAcceptanceTest {

    private final val id = BookId.generate()
    private final val book = Books.CLEAN_CODE
    private val borrowedBookRecord = borrowedBook(id, book, "Uncle Bob", "2017-08-20T12:34:56.789Z")
    private final val availableBookRecord = availableBook(id, book)


    @BeforeEach
    fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }

    }


    @CsvSource("user, us3r, 403", "curator, curat0r, 201", "admin, adm1n, 201")
    @ParameterizedTest(name = "creating a book as a {0} will result in a {2} response")
    fun `books can only be created by curators and admins`(user: String, password: String, expectedStatus: Int) {

        val bookId = BookId.generate()
        every { bookIdGenerator.generate() } returns bookId
        val requestBody = """{ "isbn": "${book.isbn}", "title": "${book.title}" } """

        every { bookDataStore.findAll() } returns emptyList()

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books")
            .then().statusCode(expectedStatus)

    }

    //================================================================================
    // book properties can only be updated by curators and admins
    //================================================================================

    @CsvSource("user, us3r, 403", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "creating a book as a {0} will result in a {2} response")
    fun `change authors property`(user: String, password: String, expectedStatus: Int) {

        val requestBody = """{ "authors": ["Foo", "Bar"] } """

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/authors")
            .then().statusCode(expectedStatus)

    }

    @CsvSource("user, us3r, 403", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "updating a book's number of pages as a {0} will result in a {2} response")
    fun `change number of pages property`(user: String, password: String, expectedStatus: Int) {

        val requestBody = """{ "numberOfPages": 128 } """

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/numberOfPages")
            .then().statusCode(expectedStatus)

    }

    @CsvSource("user, us3r, 403", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "updating a book's title as a {0} will result in a {2} response")
    fun `change title property`(user: String, password: String, expectedStatus: Int) {

        val requestBody = """{ "title": "Foo Bar" } """

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().put("/api/books/$id/title")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 403", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "removing a book's authors as a {0} will result in a {2} response")
    fun `remove authors property`(user: String, password: String, expectedStatus: Int) {

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .`when`().delete("/api/books/$id/authors")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 403", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "removing a book's number of pages as a {0} will result in a {2} response")
    fun `remove number of pages property`(user: String, password: String, expectedStatus: Int) {

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .`when`().delete("/api/books/$id/numberOfPages")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 403", "curator, curat0r, 204", "admin, adm1n, 204")
    @ParameterizedTest(name = "deleting a book as a {0} will result in a {2} response")
    fun `books can only be deleted by curators and admins`(user: String, password: String, expectedStatus: Int) {

        every { bookDataStore.findById(id) } returns availableBookRecord
        every { bookDataStore.delete(availableBookRecord) } returns Unit

        given().auth().basic(user, password)
            .`when`().delete("/api/books/$id")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 200", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "borrowing a book as a {0} will result in a {2} response")
    fun `any user can borrow books`(user: String, password: String, expectedStatus: Int) {

        val requestBody = """{ "borrower": "Rob Stark" }"""

        every { bookDataStore.findById(id) } returns availableBookRecord

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/borrow")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 200", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "returning a book as a {0} will result in a {2} response")
    fun `any user can return books`(user: String, password: String, expectedStatus: Int) {

        val requestBody = """{ } """

        every { bookDataStore.findById(id) } returns borrowedBookRecord

        given().auth().basic(user, password)
            .contentType(MediaType.APPLICATION_JSON).body(requestBody)
            .`when`().post("/api/books/$id/return")
            .then().statusCode(expectedStatus)
    }

    @CsvSource("user, us3r, 200", "curator, curat0r, 200", "admin, adm1n, 200")
    @ParameterizedTest(name = "listing all books as a {0} will result in a {2} response")
    fun `any user can list all books`(user: String, password: String, expectedStatus: Int) {

        val borrowedBook = borrowedBook(
            id = BookId.from("53397dc0-932d-4198-801a-3e00b2742ba7"),
            book = Books.CLEAN_CODER,
            borrowedBy = "Uncle Bob",
            borrowedOn = "2017-08-20T12:34:56.789Z"
        )

        every { bookDataStore.findAll() } returns listOf(borrowedBook)

        given().auth().basic(user, password)
            .`when`().get("/api/books")
            .then().statusCode(expectedStatus)
    }

    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
        .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))


}