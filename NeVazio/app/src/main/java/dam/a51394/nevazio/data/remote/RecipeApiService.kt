package dam.a51394.nevazio.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApiService {
    @GET("recipes/findByIngredients")
    suspend fun getRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 2,
        @Query("apiKey") apiKey: String = "718b77dbd1454b2c8a660d77289527e3" // Needs real key
    ): List<SpoonacularRecipeDto>

    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @retrofit2.http.Path("id") id: String,
        @Query("apiKey") apiKey: String = "718b77dbd1454b2c8a660d77289527e3"
    ): SpoonacularRecipeDetailDto

    @GET("recipes/informationBulk")
    suspend fun getRecipeInformationBulk(
        @Query("ids") ids: String,
        @Query("apiKey") apiKey: String = "718b77dbd1454b2c8a660d77289527e3"
    ): List<SpoonacularRecipeDetailDto>
}

data class SpoonacularRecipeDetailDto(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int,
    val vegetarian: Boolean,
    val extendedIngredients: List<ExtendedIngredientDto>,
    val analyzedInstructions: List<AnalyzedInstructionDto>?
)

data class ExtendedIngredientDto(
    val id: Int,
    val name: String,
    val original: String
)

data class AnalyzedInstructionDto(
    val steps: List<InstructionStepDto>
)

data class InstructionStepDto(
    val number: Int,
    val step: String
)

data class SpoonacularRecipeDto(
    val id: Int,
    val title: String,
    val image: String,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int
)
