import com.welliton.MemoryBagData
import com.welliton.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals


class MainTest {

    @Test
    fun `return empty when no bags found`() = testApplication {
        application {
            module(MemoryBagData())
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(resource("main.html"), response.bodyAsText())
    }
}

private fun resource(name: String): String =
    {}.javaClass.getResourceAsStream(name)!!.readAllBytes().toString(Charsets.UTF_8)