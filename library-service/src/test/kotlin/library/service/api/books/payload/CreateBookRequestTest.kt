package library.service.api.books.payload

import org.junit.jupiter.api.Nested
import utils.classification.UnitTest

@UnitTest
internal class CreateBookRequestTest : AbstractPayloadTest<CreateBookRequest>() {

    override val payloadType = CreateBookRequest::class

    override val jsonExample: String = """ { "isbn": "0123456789", "title": "Hello World" } """
    override val deserializedExample = CreateBookRequest("0123456789", "Hello World")

    @Nested
    inner class `bean validation` {

        @Nested
        inner class `for isbn` {

            /*@ValueSource(strings = ["0575081244", "978-0575081244", "9780575081244"])
            @ParameterizedTest
            fun `valid value examples`(isbn: String) {
                assertThat(validate(isbn)).isEmpty()
            }*/

            /*@Nested
            inner class `invalid value examples` {

                private val blankError = "must not be blank"
                private val patternError = """must match "(\d{3}-?)?\d{10}""""

                @Test
                fun `null`() {
                    assertThat(validate(null)).containsOnly(blankError)
                }

            }*/

            private fun validate(isbn: String?) = validate(CreateBookRequest(isbn = isbn, title = "Hello World"))
        }
    }
}


