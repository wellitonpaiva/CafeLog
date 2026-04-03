package com.welliton

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
import kotlinx.serialization.json.Json

fun main() {
    val bagData = MemoryBagData()
    val httpClient = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module(bagData, httpClient) })
        .start(wait = true)
}

fun Application.module(bagData: BagData, httpClient: HttpClient) {
    install(Sessions) {
        cookie<UserInfo>("user")
    }
    configureSecurity(httpClient)
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


