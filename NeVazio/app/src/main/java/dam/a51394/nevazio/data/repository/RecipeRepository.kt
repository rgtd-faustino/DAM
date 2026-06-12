package dam.a51394.nevazio.data.repository

import dam.a51394.nevazio.data.model.Recipe
import dam.a51394.nevazio.data.remote.RecipeApiService

class RecipeRepository(private val apiService: RecipeApiService) {

    // A implementação de uma cache em memória é crítica para evitar o esgotamento precoce
    // dos limites diários da API do Spoonacular (quota limit). Ao guardar a última pesquisa de receitas
    // e os detalhes já carregados, evitamos chamadas de rede redundantes quando a UI recompõe
    // ou o utilizador navega para trás e para a frente entre ecrãs.
    private var cachedIngredientsQuery: String? = null
    private var cachedRecipes: List<Recipe>? = null
    private val detailsCache = mutableMapOf<String, dam.a51394.nevazio.ui.recipe.RecipeUiState>()

    companion object {
        fun translatePtToEn(pt: String): String {
            val map = mapOf(
                "ovo" to "egg", "ovos" to "eggs", "leite" to "milk",
                "queijo" to "cheese", "frango" to "chicken", "carne" to "beef",
                "porco" to "pork", "peixe" to "fish", "salmão" to "salmon",
                "atum" to "tuna", "pão" to "bread", "arroz" to "rice",
                "massa" to "pasta", "esparguete" to "spaghetti", "tomate" to "tomato",
                "batata" to "potato", "cebola" to "onion", "alho" to "garlic",
                "cenoura" to "carrot", "alface" to "lettuce", "maçã" to "apple",
                "banana" to "banana", "laranja" to "orange", "limão" to "lemon",
                "manteiga" to "butter", "azeite" to "olive oil", "óleo" to "oil",
                "sal" to "salt", "pimenta" to "pepper", "açúcar" to "sugar",
                "farinha" to "flour", "água" to "water", "cogumelo" to "mushroom"
            )
            var res = pt.lowercase()
            map.forEach { (k, v) ->
                if (res.contains(k)) return v
            }
            return res
        }
    }

    suspend fun fetchRecipesForIngredients(ingredientNames: List<String>): List<Recipe> {
        return try {
            if (ingredientNames.isEmpty()) return emptyList()
            val translatedIngredients = ingredientNames.map { translatePtToEn(it) }
            // A ordenação alfabética garante que a mesma lista de ingredientes (mesmo que 
            // alterada na ordem devido a UI) gera exatamente a mesma string de query, 
            // maximizando os acertos na cache e poupando chamadas à API.
            val query = translatedIngredients.sorted().joinToString(",")
            
            if (query == cachedIngredientsQuery && cachedRecipes != null) {
                return cachedRecipes!!
            }
            
            val apiDtos = apiService.getRecipesByIngredients(ingredients = query)
            val dtos = apiDtos.filter { it.usedIngredientCount > 0 }
            if (dtos.isEmpty()) return emptyList()

            val idsString = dtos.joinToString(",") { it.id.toString() }
            val bulkInfo = try {
                apiService.getRecipeInformationBulk(ids = idsString)
            } catch (e: Exception) {
                emptyList()
            }

            dtos.map { dto ->
                val detailedInfo = bulkInfo.find { it.id == dto.id }
                val isVegetarian = detailedInfo?.vegetarian ?: false
                val time = detailedInfo?.readyInMinutes ?: 30
                val tags = buildList {
                    add("API Sugestão")
                    if (isVegetarian) add("Vegetariano")
                }

                val recipe = Recipe(
                    id = dto.id.toString(),
                    name = dto.title,
                    timeMinutes = time,
                    difficulty = "Variável",
                    tags = tags,
                    matchPercentage = if (dto.usedIngredientCount + dto.missedIngredientCount == 0) 100 
                                      else (dto.usedIngredientCount * 100) / (dto.usedIngredientCount + dto.missedIngredientCount),
                    missingIngredients = dto.missedIngredientCount,
                    imageUrl = dto.image
                )
                recipe
            }.also { finalRecipes ->
                // Guardar o resultado e a query em memória
                cachedIngredientsQuery = query
                cachedRecipes = finalRecipes
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchRecipeDetails(id: String): dam.a51394.nevazio.ui.recipe.RecipeUiState? {
        if (detailsCache.containsKey(id)) {
            return detailsCache[id]
        }
        
        return try {
            val dto = apiService.getRecipeInformation(id = id)
            val ingredients = dto.extendedIngredients.map {
                val ingName = it.name.lowercase()
                val icon = when {
                    ingName.contains("egg") -> "egg"
                    ingName.contains("cheese") -> "cheese"
                    ingName.contains("water") || ingName.contains("milk") -> "water_drop"
                    ingName.contains("meat") || ingName.contains("beef") || ingName.contains("pork") || ingName.contains("chicken") -> "restaurant"
                    ingName.contains("vegetable") || ingName.contains("broccoli") || ingName.contains("carrot") || ingName.contains("lettuce") || ingName.contains("spinach") -> "eco"
                    else -> "food"
                }

                dam.a51394.nevazio.data.model.Ingredient(
                    id = it.id.toString(),
                    name = it.name,
                    quantity = it.original,
                    expiryDate = java.util.Date(),
                    location = dam.a51394.nevazio.data.model.StorageLocation.FRIDGE,
                    iconName = icon,
                    status = dam.a51394.nevazio.data.model.ExpiryStatus.FRESH,
                    expiryLabel = ""
                ) to true // Assuming available for now
            }
            
            val steps = dto.analyzedInstructions?.firstOrNull()?.steps?.map { it.step } ?: emptyList()

            dam.a51394.nevazio.ui.recipe.RecipeUiState(
                title = dto.title,
                time = "${dto.readyInMinutes} min",
                difficulty = "Médio",
                isVegetarian = dto.vegetarian,
                tags = buildList {
                    if (dto.vegetarian) add("Vegetariano")
                },
                ingredients = ingredients,
                steps = steps
            ).also { uiState ->
                detailsCache[id] = uiState
            }
        } catch (e: Exception) {
            null
        }
    }
}
