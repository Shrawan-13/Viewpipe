package com.example.data.remote

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OkHttpDownloader : Downloader() {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun execute(request: Request): Response {
        val method = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = dataToSend.toRequestBody()
        }

        val builder = okhttp3.Request.Builder()
            .method(method, requestBody)
            .url(url)

        headers.forEach { entry ->
            entry.value.forEach { value ->
                builder.addHeader(entry.key, value)
            }
        }

        val response = client.newCall(builder.build()).execute()
        
        val responseHeaders = mutableMapOf<String, List<String>>()
        response.headers.names().forEach { name ->
            responseHeaders[name] = response.headers.values(name)
        }
        
        val bodyStr = response.body?.string() ?: ""

        return Response(
            response.code,
            response.message,
            responseHeaders,
            bodyStr,
            request.url()
        )
    }
}
