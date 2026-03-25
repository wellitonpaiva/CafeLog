package com.welliton

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Bag(
    val name: String,
    val country: String,
    val varietal: List<String>,
    val process: List<String>,
    val altitude: Double,
    val score: Float,
    val notes: List<String>,
    val roaster: String,
    val date: LocalDateTime
)

interface BagData {
    fun fetchAll(): List<Bag>
    fun add(bag: Bag): Boolean
}

class MemoryBagData(val bags: MutableList<Bag> = mutableListOf()): BagData {
    override fun fetchAll() = bags
    override fun add(bag: Bag) = bags.add(bag)

}

fun main() {
    val bagData = MemoryBagData()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module(bagData) })
        .start(wait = true)
}

fun Application.module(bagData: BagData) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
        }
    }
    configureRouting(bagData)
}

fun Application.configureRouting(bagData: BagData) {
    routing {
        staticResources("/", "static")
        get("/") {
            call.respondHtml {
                head {
                    link {
                        href = "https://cdn.jsdelivr.net/npm/sakura.css/css/sakura.css"
                        rel = "stylesheet"
                    }
                }
                body {
                    h1 { +"CafeLog" }
                    ul {
                        bagData.fetchAll().forEach { bag ->
                            li { +"[${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(bag.date)}] ${bag.name}" }
                        }
                    }
                }
            }
        }
        get("/addBag") {
            call.respondHtml {
                head {
                    link {
                        href = "https://cdn.jsdelivr.net/npm/sakura.css/css/sakura.css"
                        rel = "stylesheet"
                    }
                }
                body {
                    h1 { +"CafeLog - Add Bag" }
                    form {
                        id = "addBagForm"
                        label { +"Name: " }
                        input(type = InputType.text) {
                            name = "name"
                            required = true
                        }
                        label { +"Country: " }
                        input(type = InputType.text) {
                            name = "country"
                            required = true
                        }
                        label { +"Varietal (comma-separated): " }
                        input(type = InputType.text) {
                            name = "varietal"
                            placeholder = "e.g. Bourbon, Typica"
                        }
                        label { +"Process (comma-separated): " }
                        input(type = InputType.text) {
                            name = "process"
                            placeholder = "e.g. Washed, Natural"
                        }
                        label { +"Altitude (meters): " }
                        input(type = InputType.number) {
                            name = "altitude"
                            required = true
                        }
                        label { +"Score (1-100): " }
                        input(type = InputType.number) {
                            name = "score"
                            required = true
                        }
                        label { +"Notes (comma-separated): " }
                        input(type = InputType.text) {
                            name = "notes"
                            placeholder = "e.g. Fruity, Balanced"
                        }
                        label { +"Roaster: " }
                        input(type = InputType.text) {
                            name = "roaster"
                            required = true
                        }
                        input(type = InputType.submit) {
                            value = "Add Bag"
                        }
                    }
                    script {
                        type = ScriptType.textJavaScript
                        src = "/add.js"
                    }
                }
            }
        }
        post("/bag") {
            val bag = call.receive<Bag>()
            bagData.add(bag)
            call.respond(HttpStatusCode.Created, bag)
        }
    }
}

