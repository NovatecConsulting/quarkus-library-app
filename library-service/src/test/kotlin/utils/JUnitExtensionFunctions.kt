package utils

import org.junit.jupiter.api.Assertions
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
fun <T : Throwable> assertThrows(expectedType: KClass<T>, executable: () -> Unit): T {
    return Assertions.assertThrows(expectedType.java, executable)
}