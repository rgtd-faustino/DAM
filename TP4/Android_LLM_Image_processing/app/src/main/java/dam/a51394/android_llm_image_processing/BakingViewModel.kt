package dam.a51394.android_llm_image_processing

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(bitmap: Bitmap, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    // este método serve essencialmente para o modelo AI adivinhar o preço do bolo escolhido pelo
    // utilizador e informar o mesmo se esteve perto da resposta da AI ou não, de maneira engraçada
    fun guessPrice(bitmap: Bitmap, userGuess: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        // fazemos com que a AI passe por um chef de pastelaria para condicionar a
                        // sua resposta para que pense que saiba a resposta
                        // usamos tres aspas para os paragrafos serem introduzidos facilmente
                        text("""
                        You are a professional pastry chef and bakery pricing expert.
                        The user thinks this baked good costs $userGuess.
                        Analyze the image and estimate the real price in a bakery.
                        Then tell the user if they guessed too high, too low, or about right.
                        Keep it fun and short.
                    """.trimIndent()) // removes as identações iniciais desnecessárias
                    }
                )
                // output da AI
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}