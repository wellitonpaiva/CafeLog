package com.welliton

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
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
}

class MemoryBagData(val bags: List<Bag> = emptyList()): BagData {
    override fun fetchAll(): List<Bag> = bags

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
        }
    }
    configureRouting(bagData)
}

fun Application.configureRouting(bagData: BagData) {
    routing {
        get("/") {
            call.respondHtml {
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

    }
}

