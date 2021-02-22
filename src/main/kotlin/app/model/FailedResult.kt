package app.model

import kotlinx.serialization.Serializable

@Serializable
data class FailedResult(
    val exception: String,
    val input: Division
) : Result
