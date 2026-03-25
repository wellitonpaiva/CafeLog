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
    fun testRoot() = testApplication {
        application {
            module(MemoryBagData() )
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }

    @Test
    fun `return empty when no bags found`() = testApplication {
        application {
            module(MemoryBagData())
        }
        val response = client.get("/bags")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[ ]", response.bodyAsText())
    }
}