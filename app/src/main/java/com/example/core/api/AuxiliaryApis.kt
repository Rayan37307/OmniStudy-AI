package com.example.core.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class WikiSummary(
    val title: String,
    val extract: String,
    val description: String = "",
    val thumbnailUrl: String? = null,
    val language: String = "bn"
)

data class DefinitionItem(
    val definition: String,
    val example: String? = null
)

data class WordMeaning(
    val partOfSpeech: String,
    val definitions: List<DefinitionItem>
)

data class DictionaryResult(
    val word: String,
    val phonetic: String = "",
    val meanings: List<WordMeaning> = emptyList()
)

class AuxiliaryApis {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Tries Bengali Wikipedia first (100% free), then falls back to English Wikipedia.
     */
    suspend fun fetchWikipediaSummary(query: String, preferBengali: Boolean = true): Result<WikiSummary> = withContext(Dispatchers.IO) {
        try {
            val cleanQuery = query.trim().replace(" ", "_")
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")

            val endpoints = if (preferBengali) {
                listOf(
                    "https://bn.wikipedia.org/api/rest_v1/page/summary/$encoded" to "bn",
                    "https://en.wikipedia.org/api/rest_v1/page/summary/$encoded" to "en"
                )
            } else {
                listOf(
                    "https://en.wikipedia.org/api/rest_v1/page/summary/$encoded" to "en",
                    "https://bn.wikipedia.org/api/rest_v1/page/summary/$encoded" to "bn"
                )
            }

            for ((url, lang) in endpoints) {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "OmniStudyAI-Bengali/1.0 (Android Educational App)")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            val title = json.optString("title", query)
                            val extract = json.optString("extract", "")
                            val description = json.optString("description", "")
                            val thumbnail = json.optJSONObject("thumbnail")?.optString("source")

                            if (extract.isNotBlank()) {
                                return@withContext Result.success(
                                    WikiSummary(
                                        title = title,
                                        extract = extract,
                                        description = description,
                                        thumbnailUrl = thumbnail,
                                        language = lang
                                    )
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Try next language endpoint
                }
            }

            Result.failure(Exception("উইকিপিডিয়াতে এই বিষয়ের তথ্য পাওয়া যায়নি (Wikipedia article not found)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDictionaryDefinition(word: String): Result<DictionaryResult> = withContext(Dispatchers.IO) {
        try {
            val cleanWord = word.trim().lowercase().filter { it.isLetter() || it.isWhitespace() }
            if (cleanWord.isEmpty()) {
                return@withContext Result.failure(Exception("অনুগ্রহ করে একটি সঠিক শব্দ দিন (Please provide a valid word)."))
            }

            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$encoded"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "OmniStudyAI/1.0 (Android Educational App)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // If English dictionary has no entry, check Wikipedia definition
                val wikiFallback = fetchWikipediaSummary(word, preferBengali = true)
                if (wikiFallback.isSuccess) {
                    val summary = wikiFallback.getOrThrow()
                    return@withContext Result.success(
                        DictionaryResult(
                            word = summary.title,
                            phonetic = summary.description,
                            meanings = listOf(
                                WordMeaning(
                                    partOfSpeech = if (summary.language == "bn") "বাংলা ব্যাখ্যা" else "Definition",
                                    definitions = listOf(DefinitionItem(summary.extract))
                                )
                            )
                        )
                    )
                }
                return@withContext Result.failure(Exception("'$word' শব্দটির অর্থ পাওয়া যায়নি"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val jsonArray = JSONArray(body)
            if (jsonArray.length() == 0) {
                return@withContext Result.failure(Exception("No definitions found"))
            }

            val firstEntry = jsonArray.getJSONObject(0)
            val wordTitle = firstEntry.optString("word", word)
            val phonetic = firstEntry.optString("phonetic", "")

            val meaningsList = mutableListOf<WordMeaning>()
            val meaningsJson = firstEntry.optJSONArray("meanings")
            if (meaningsJson != null) {
                for (i in 0 until meaningsJson.length()) {
                    val meaningObj = meaningsJson.getJSONObject(i)
                    val pos = meaningObj.optString("partOfSpeech", "")
                    val defsJson = meaningObj.optJSONArray("definitions")
                    val defList = mutableListOf<DefinitionItem>()
                    if (defsJson != null) {
                        for (j in 0 until defsJson.length()) {
                            val defObj = defsJson.getJSONObject(j)
                            val defText = defObj.optString("definition", "")
                            val example = if (defObj.has("example")) defObj.optString("example") else null
                            if (defText.isNotBlank()) {
                                defList.add(DefinitionItem(defText, example))
                            }
                        }
                    }
                    if (defList.isNotEmpty()) {
                        meaningsList.add(WordMeaning(pos, defList))
                    }
                }
            }

            Result.success(
                DictionaryResult(
                    word = wordTitle,
                    phonetic = phonetic,
                    meanings = meaningsList
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
