package app.model

import kotlinx.serialization.Serializable

@Serializable
data class Division(
    val dividend: Int,
    val divisor: Int
)
