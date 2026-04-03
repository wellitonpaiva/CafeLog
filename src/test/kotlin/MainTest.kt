import io.ktor.server.testing.*
import kotlin.test.Test


class MainTest {

    @Test
    fun `return empty when no bags found`() = testApplication {


    }
}

private fun resource(name: String): String =
    {}.javaClass.getResourceAsStream(name)!!.readAllBytes().toString(Charsets.UTF_8)