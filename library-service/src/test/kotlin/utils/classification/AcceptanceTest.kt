package utils.classification

import org.junit.jupiter.api.Tag

/**
 *
 * An acceptance test is a test against a running instance of the application
 * without faking (mocking / stubbing etc.) _any_ part of it. In addition
 * any interaction with the application should be done through official
 * interfaces like the API.
 *
 */
@Retention
@Target(AnnotationTarget.CLASS)
@Tag("acceptance-test")
annotation class AcceptanceTest