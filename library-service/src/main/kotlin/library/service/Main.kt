package library.service

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class Main {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Running main method")
            Quarkus.run(*args)
        }
    }
}