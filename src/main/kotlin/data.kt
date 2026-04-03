package com.welliton

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Instant

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
    val date: Instant
) {
    fun formattedDate() = date.format(DateTimeComponents.Format {
        monthNumber()
        char('/')
        day()
        char('/')
        year()
    })
}

@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class User(
    val id: String,
    val name: String,
    @SerialName("given_name")
    val givenName: String? = null,
)

interface BagData {
    fun fetchAll(userId: String): List<Bag>
    fun add(bag: Bag, userId: String): Boolean
}

interface UserData {
    fun fetchUser(id: String): User?
    fun addUser(user: User): Boolean
}

class DbUser : UserData {
    override fun fetchUser(id: String): User? = transaction {
        CoffeeUser.selectAll().where { CoffeeUser.id eq id }.map {
            User(
                id = it[CoffeeUser.id],
                name = it[CoffeeUser.name],
                givenName = it[CoffeeUser.givenName]
            )
        }.firstOrNull()
    }

    override fun addUser(user: User): Boolean = transaction {
        CoffeeUser.insert {
            it[id] = user.id
            it[name] = user.name
            it[givenName] = user.givenName
        }
    }.insertedCount == 1
}

class DbCoffee : BagData {
    override fun fetchAll(userId: String): List<Bag> = transaction {
        CoffeeBag.selectAll().where { CoffeeBag.userId eq userId }.map {
            Bag(
                name = it[CoffeeBag.name],
                country = it[CoffeeBag.country],
                varietal = it[CoffeeBag.varietal],
                process = it[CoffeeBag.process],
                altitude = it[CoffeeBag.altitude],
                score = it[CoffeeBag.score],
                notes = it[CoffeeBag.notes],
                roaster = it[CoffeeBag.roaster],
                date = Instant.parse(it[CoffeeBag.date])
            )
        }
    }

    override fun add(bag: Bag, userId: String): Boolean = transaction {
        CoffeeBag.insert {
            it[this.name] = bag.name
            it[this.country] = bag.country
            it[this.varietal] = bag.varietal
            it[this.process] = bag.process
            it[this.altitude] = bag.altitude
            it[this.score] = bag.score
            it[this.notes] = bag.notes
            it[this.roaster] = bag.roaster
            it[this.date] = bag.date.toString()
            it[this.userId] = userId
        }
    }.insertedCount == 1
}

object CoffeeBag : Table("bag") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val country = varchar("country", 100)
    val varietal = array<String>("varietal")
    val process = array<String>("process")
    val altitude = double("altitude")
    val score = float("score")
    val notes = array<String>("notes")
    val roaster = varchar("roaster", 255)
    val date = varchar("date", 50)
    val userId = varchar("user_id", 100) references CoffeeUser.id
    override val primaryKey = PrimaryKey(id)
}

object CoffeeUser : Table("user") {
    val id = varchar("id", 100)
    val name = varchar("name", 255)
    val givenName = varchar("given_name", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

fun configureDatabases(appConfig: AppConfig) {
    Database.connect(
        url = appConfig.url,
        driver = "org.postgresql.Driver",
        user = appConfig.dbUser,
        password = appConfig.dbPassword
    )
    transaction { SchemaUtils.create(CoffeeBag, CoffeeUser) }
}