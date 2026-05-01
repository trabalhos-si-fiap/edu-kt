package br.com.edu.features.support.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageDto(
    val id: Long,
    val sender: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SupportMessageInDto(
    val body: String,
)
