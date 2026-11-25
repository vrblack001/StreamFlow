package dev.streamflow.android.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NpRequest
import org.schabi.newpipe.extractor.downloader.Response as NpResponse
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

/**
 * Custom OkHttp-based Downloader for NewPipeExtractor.
 *
 * FIX (NewPipe common issue): Use a singleton OkHttpClient to avoid socket exhaustion.
 * FIX: Set proper timeouts so the app doesn't hang indefinitely on slow connections.
 * FIX: Handle 429 (rate-limit) and 403 (bot-detected) errors gracefully.
 */
class DownloaderImpl private constructor(private val client: OkHttpClient) : Downloader() {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Android 14; Mobile; rv:126.0) Gecko/126.0 Firefox/126.0"
        private const val TIMEOUT_SECONDS = 30L

        @Volatile
        private var instance: DownloaderImpl? = null

        fun getInstance(): DownloaderImpl = instance ?: synchronized(this) {
            instance ?: DownloaderImpl(
                OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor { chain ->
                        val req = chain.request().newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .build()
                        chain.proceed(req)
                    }
                    .build()
            ).also { instance = it }
        }
    }

    override fun execute(request: NpRequest): NpResponse {
        val builder = Request.Builder().url(request.url())
        request.headers().forEach { (key, values) ->
            values.forEach { builder.addHeader(key, it) }
        }

        val body = request.dataToSend()?.let {
            RequestBody.create(null, it)
        }
        builder.method(request.httpMethod(), body)

        val response: Response = client.newCall(builder.build()).execute()

        // FIX: Throw ReCaptchaException so callers can show a helpful UI prompt
        if (response.code == 429 || response.code == 403) {
            throw ReCaptchaException(
                "YouTube is rate-limiting this request (${response.code}). " +
                        "Try switching the Piped instance or waiting a moment.",
                request.url()
            )
        }

        val responseBodyToReturn = response.body?.string() ?: ""
        return NpResponse(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBodyToReturn,
            response.request.url.toString()
        )
    }
}
