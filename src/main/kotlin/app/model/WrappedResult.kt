package app.model

import kotlinx.serialization.Serializable

@Serializable
data class WrappedResult(
    val exception: Exception? = null,
    val value: Any
)
