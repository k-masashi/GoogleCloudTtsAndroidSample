package com.example.googlecloudttssample

import android.media.MediaPlayer
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException


class GoogleTtsSampleClient {

    private var mediaPlayer: MediaPlayer? = null
    private val apiKey = "API Keyを入力します"
    private val url = "https://texttospeech.googleapis.com/v1beta1/text:synthesize"

    private val sampleSentence = "The SSML standard is defined by the W3C."
    private val sampleSsml = "<speak>The <say-as interpret-as=\\\"characters\\\">SSML</say-as>" +
            "standard <break time=\\\"1s\\\"/>is defined by the " +
            "<sub alias=\\\"World Wide Web Consortium\\\">W3C</sub>.</speak>"

    fun startSample() = runBlocking {
        async(Dispatchers.Default) {
            OkHttpClient().newCall(createRequest(sampleSentence)).execute()
        }.await().let { response ->
            if (response.isSuccessful) {
                response.body()?.string().let { body ->
                    val audioResponse = Gson().fromJson(
                        body,
                        AudioResponse::class.java
                    )
                    playAudio(audioResponse)
                }
            }
        }
    }

    fun startSsmlSample() = runBlocking {
        async(Dispatchers.Default) {
            OkHttpClient().newCall(createRequest(sampleSentence)).execute()
        }.await().let { response ->
            if (response.isSuccessful) {
                response.body()?.string().let { body ->
                    val audioResponse = Gson().fromJson(
                        body,
                        AudioResponse::class.java
                    )
                    playAudio(audioResponse)
                }
            }
        }
    }


    private fun createRequest(text: String) = Request.Builder()
            .url(url)
            .addHeader("X-Goog-Api-Key", apiKey)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(createRequestBody(text))
            .build()

    private fun createRequestBody(text: String): RequestBody {
        val requestParams = RequestParams(
            input = SynthesisInput(ssml = text),
            voice = VoiceSelectionParams(languageCode = "en-US"),
            audioConfig = AudioConfig(audioEncoding = "LINEAR16")
        )
        val json = Gson().toJson(requestParams)
        return RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            json
        )
    }

    private fun playAudio(audioResponse: AudioResponse) {
        try {
            val dataSource = "data:audio/mp3;base64,${audioResponse.audioContent}"
            mediaPlayer = MediaPlayer().apply {
                setDataSource(dataSource)
                prepare()
                start()
            }
        } catch (IoEx: IOException) {
            // TODO: error handling
        }

    }

    data class AudioResponse(
        val audioContent: String?
    )

    data class RequestParams(
        val input: SynthesisInput,
        val voice: VoiceSelectionParams,
        val audioConfig: AudioConfig
    )

    data class SynthesisInput(
        val ssml: String
    )

    data class VoiceSelectionParams(
        val languageCode: String
    )

    data class AudioConfig(
        val audioEncoding: String
    )
}