package br.com.edu.features.support.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.support.data.remote.SupportApi
import br.com.edu.features.support.data.remote.SupportMessageDto
import br.com.edu.features.support.data.remote.SupportMessageInDto
import br.com.edu.features.support.domain.Sender
import br.com.edu.features.support.domain.SupportMessage

class SupportRepository(
    private val api: SupportApi = ApiClient.create(),
) {
    suspend fun list(): List<SupportMessage> = api.list().map { it.toDomain() }

    suspend fun send(body: String): List<SupportMessage> =
        api.send(SupportMessageInDto(body = body)).map { it.toDomain() }
}

private fun SupportMessageDto.toDomain() = SupportMessage(
    id = id,
    sender = if (sender == "support") Sender.SUPPORT else Sender.USER,
    body = body,
    createdAt = createdAt,
)
