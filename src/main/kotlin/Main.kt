package com.welliton

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable


@Serializable
data class Bag(
    val name: String,
    val country: String,
    val varietal: List<String>,
    val process: List<String>,
    val altitude: Double,
    val score: Float,
    val notes: List<String>,
    val roaster: String,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val date: LocalDateTime
)

interface BagData {
    fun fetchAll(): List<Bag>
    fun add(bag: Bag): Boolean
}

class MemoryBagData(val bags: MutableList<Bag> = mutableListOf()) : BagData {
    override fun fetchAll() = bags
    override fun add(bag: Bag) = bags.add(bag)

}

fun main() {
    val bagData = MemoryBagData()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module(bagData) })
        .start(wait = true)
}

fun Application.module(bagData: BagData) {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }
    configureSecurity()
    install(ContentNegotiation) {
        json()
    }
    configureRouting(bagData)
}

fun Application.configureRouting(bagData: BagData) {
    routing {
        staticResources("/", "static")
        authenticate("auth-oauth-google") {
            post("/bag") {
                val bag = call.receive<Bag>()
                bagData.add(bag)
                call.respond(HttpStatusCode.Created, bag)
            }
            main(bagData)
            addBag()
        }
    }
}


