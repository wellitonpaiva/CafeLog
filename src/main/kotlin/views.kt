package com.welliton

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Routing.main(bagData: BagData) {
    get("/") {
        val userSession: UserSession? = getSession(call)
        if (userSession != null) {
            val userInfo: UserInfo = getPersonalGreeting(httpClient, userSession)
            call.respondHtml(
                HttpStatusCode.OK,
                page("CafeLog - ${userInfo.givenName}'s Coffee Bags") {
                    ul {
                        bagData.fetchAll().forEach { bag ->
                            li { +"[${bag.date}] ${bag.name}" }
                        }
                    }
                }
            )
        }
    }
}

fun Routing.addBag() {
    get("/addBag") {
        call.respondHtml(HttpStatusCode.OK, page("CafeLog - Add Bag") {
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
        })
    }
}


fun page(header: String, content: BODY.() -> Unit): HTML.() -> Unit = {
    head {
        title { +"Beans " }
        link {
            rel = "stylesheet"
            href = "https://cdn.jsdelivr.net/npm/sakura.css/css/sakura.css"
            type = "text/css"
        }
    }
    body {
        h1 { +header }
        content()
    }
}