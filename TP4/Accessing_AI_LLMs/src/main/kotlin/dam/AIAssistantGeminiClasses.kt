package dam

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

/**
 * GeminiAIAssistant class provides an interface to communicate with Google's Gemini AI models.
 * This class handles API authentication, request formatting, response parsing, and error handling.
 * It implements retry logic for rate-limited requests and validates JSON responses.
 *
 * @param properties Properties containing the API key for authentication with Gemini services
 */
class AIAssistantGeminiClasses(override val properties: Properties) : AIAssistant {

    override fun getSystem() = "GEMINI"
    override val apiKeyName = "GEMINI_API_KEY"

    // Model selection - different models have different capabilities and costs
    //  override var model = "gemini-1.0-pro" // NOK - Primary model for most tasks
    // override var model = "gemini-1.0-ultra" // NOK - Most capable model (if available)
    //override var model = "gemini-1.5-flash" // OK - Faster, less expensive
    // override var model = "gemini-1.5-pro" // OK - Primary model for most tasks
    // override var model = "gemini-2.0-flash" // OK - Most capable model (if available)
    // override var model = "gemini-2.0-pro" // NOK - Most capable model (if available)
    // override var model = "gemini-2.5-flash-preview" // NOK - Most capable model (if available) //override var model = "gemini-2.5-flash-preview-04-17" // NOK - Most capable model (if available)

    // este foi o único modelo que funcionou
    override var model = "gemini-2.5-flash" // NOK - Most capable model (if available)

    // Data classes for Gemini API request structure
    data class Part(
        val text: String
    )

    data class Content(
        val role: String,
        val parts: List<Part>
    )

    data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null
    )

    // tive de trocar a ordem da temperature e maxoutputtokens porque quando ciravamos um objeto desta classe
    // com os parametros (temp, max) em vez de ficarem esses atributos da classe com os valores dos parametros
    // estava a ficar a temp correta mas depois o topK é que ficava com o max
    data class GenerationConfig(
        val temperature: Double? = 0.4,      // Default reasonable balance
        val maxOutputTokens: Int? = 800,     // Controls response length
        val topK: Int? = 40,                 // Limits selection to top K most likely tokens
        val topP: Double? = 0.95,            // Nucleus sampling - covers 95% of probability mass
        val candidateCount: Int? = 1         // Number of alternative responses to generate
    )

    // Gson instance for JSON serialization
    private val gson = Gson()

    /**
     * Constructs and formats a structured request from the given input prompt.
     * This method is intended to prepare the necessary request structure for
     * sending to an AI-powered model or API.
     *
     * @param prompt The user's input query or prompt that needs to be formatted into a request
     */
    override fun buildRequest(prompt: String): Request {
        // nao podemos meter o properties.getProperty() diretamente no data class GenerationConfig porque ele nao tem
        // acesso ao properties porque pertence ao AIAssistantGeminiClasses e o GenerationConfig é uma classe separada
        // entao lemos os valores antes e passamos ja prontos para o construtor
        val temperature = properties.getProperty("TEMPERATURE").toDouble()
        val maxTokens = properties.getProperty("MAX_TOKENS").toInt()

        // Create request structure using data classes
        val part = Part(text = prompt)
        val content = Content(
            role = "user",
            parts = listOf(part)
        )
        val geminiRequest = GeminiRequest(
            contents = listOf(content)
            , generationConfig = GenerationConfig(temperature, maxTokens))

        // Convert to JSON string using Gson
        val requestBody = gson.toJson(geminiRequest)

        // Configure the HTTP request with proper headers and authentication
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/$model:generateContent?key=$apiKey")  // Gemini API endpoint
            .addHeader("Content-Type", "application/json")  // Specify content type
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))  // Set the request body
            .build()
        return request
    }
}