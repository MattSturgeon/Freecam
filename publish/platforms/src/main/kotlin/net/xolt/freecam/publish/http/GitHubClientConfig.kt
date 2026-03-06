package net.xolt.freecam.publish.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import net.xolt.freecam.publish.logger.LogLevel
import net.xolt.freecam.publish.logger.Logger
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal fun HttpClientConfig<*>.configureGitHubClient(
    token: String,
    logLevel: LogLevel = LogLevel.VERBOSE,
    retryExceptions: Int = 4,
    retryHttpErrors: Int = 4,
) {
    install(DefaultRequest) {
        bearerAuth(token)
        accept(ContentType.Application.GitHubJson)
    }
    install(Logging) {
        logger = object : KtorLogger {
            override fun log(message: String) =
                Logger.log(logLevel) { message }
        }
        level = Logger.level.let {
            // Avoid ktor logging if our logger will ignore
            if (!Logger.logs(logLevel)) KtorLogLevel.NONE
            // VERBOSE → INFO
            else if (it == LogLevel.VERBOSE) KtorLogLevel.INFO
            // DEBUG+ → HEADERS
            else if (it > LogLevel.VERBOSE) KtorLogLevel.HEADERS
            else KtorLogLevel.NONE
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        })
    }
    install(HttpRequestRetry) {
        retryOnException(maxRetries = retryExceptions)
        retryOnRateLimitsOrServerErrors(maxRetries = retryHttpErrors)
        exponentialDelay(respectRetryAfterHeader = true)
        modifyRequest { request ->
            request.headers.append(
                name = HttpHeaders.XRetryCount,
                value = retryCount.toString(),
            )
        }
    }
    HttpResponseValidator {
        handleGitHubErrors()
    }
    expectSuccess = true
}

internal fun HttpRequestRetryConfig.retryOnRateLimitsOrServerErrors(maxRetries: Int = 4) {
    retryIf(maxRetries) { _, response ->
        when (response.status) {
            // 429 TooManyRequests is usually caused by ratelimits
            HttpStatusCode.TooManyRequests -> true

            // 403 Forbidden is sometimes annotated with RetryAfter
            HttpStatusCode.Forbidden ->
                HttpHeaders.RetryAfter in response.headers
                || response.headers[HttpHeaders.XRateLimitRemaining] == "0"

            // 5xx server errors are usually transient
            else -> response.status.value in 500..599
        }
    }
}

internal fun HttpCallValidatorConfig.handleGitHubErrors() {
    handleResponseExceptionWithRequest { cause, _ ->
        (cause as? ResponseException)
            ?.toGitHubResponseExceptionOrNull()
            ?.let { throw it }
    }
}

internal fun githubUrl(vararg path: String) = buildUrl {
    protocol = URLProtocol.HTTPS
    host = "api.github.com"
    path(*path)
}