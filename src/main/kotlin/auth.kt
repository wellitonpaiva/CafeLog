package com.welliton

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun Application.configureSecurity(httpClient: HttpClient) {
    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://oauth2.googleapis.com/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                )
            }
            client = httpClient
        }
    }
    routing {
        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(fetchUserInfo(httpClient, UserSession(principal?.accessToken.toString())))
                call.respondRedirect("/")
            }
        }
    }
}

@Serializable
data class UserSession(val accessToken: String)

suspend fun ApplicationCall.requireUser(): UserInfo? =
    sessions.get<UserInfo>().also { userSession ->
        if (userSession == null) {
            respondRedirect("http://localhost:8080/login?redirectUrl=${request.uri}")
        }
    }

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    @SerialName("given_name")
    val givenName: String? = null,
    val picture: String? = null,
)

suspend fun fetchUserInfo(httpClient: HttpClient, userSession: UserSession): UserInfo =
    httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo?access_token=${userSession.accessToken}").body()
