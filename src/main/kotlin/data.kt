package com.welliton

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