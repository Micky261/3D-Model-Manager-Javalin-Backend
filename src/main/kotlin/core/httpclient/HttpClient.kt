package core.httpclient

import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.client.HttpClient as KtorHttpClient

object HttpClient {
    const val USER_AGENT = "3DMM-bot/1.0"

    val instance: KtorHttpClient by lazy {
        KtorHttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(UserAgent) {
                agent = USER_AGENT
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 30_000
            }
            expectSuccess = false
        }
    }
}
