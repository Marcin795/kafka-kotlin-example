package app.model

import kotlinx.serialization.Serializable

@Serializable
data class DivisionResult(
    val result: Int,
    val input: Division
) : Result