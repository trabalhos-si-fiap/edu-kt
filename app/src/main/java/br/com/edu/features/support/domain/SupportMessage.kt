package br.com.edu.features.support.domain

enum class Sender { USER, SUPPORT }

data class SupportMessage(
    val id: Long,
    val sender: Sender,
    val body: String,
    val createdAt: String,
)
