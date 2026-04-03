package com.welliton

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json

data class AppConfig(
    val url: String,
    val dbUser: String,
    val dbPassword: String,
    val clientId: String,
    val clientSecret: String
) {
    constructor() : this(
        url = System.getenv("DB_URL"),
        dbUser = System.getenv("DB_USER"),
        dbPassword = System.getenv("DB_PASSWORD"),
        clientId = System.getenv("GOOGLE_CLIENT_ID"),
        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET")
    )
}

fun main() {
    val appConfig = AppConfig()
    configureDatabases(appConfig)
    val bagData = DbCoffee()
    val userData = DbUser()
    val httpClient = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = {
        module(bagData, userData, httpClient, appConfig)
    }).start(wait = true)
}

fun Application.module(bagData: BagData, userData: UserData, httpClient: HttpClient, appConfig: AppConfig) {
    install(Sessions) {
        cookie<User>("user")
    }
    configureSecurity(httpClient, userData, appConfig)
    install(ContentNegotiation) {
        json()
    }
    configureRouting(bagData)
}

fun Application.configureRouting(bagData: BagData) {
    routing {
        staticResources("/", "static")
        post("/bag") {
            call.requireUser()?.let { user ->
                call.receive<Bag>().let { bag ->
                    bagData.add(bag, user.id)
                    call.respond(HttpStatusCode.Created, bag)
                }
            }
        }
        authenticate("auth-oauth-google") {
            main(bagData)
            addBag()
        }
    }
}


