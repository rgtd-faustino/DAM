package dam.a51394.core.repository

import android.content.Context
import dam.a51394.core.api.CatApiService
import dam.a51394.core.data.CacheManager
import dam.a51394.core.model.CatImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import java.io.IOException

class CatRepository(context: Context) {

    private val apiService: CatApiService
    val cacheManager = CacheManager(context)

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        apiService = retrofit.create(CatApiService::class.java)
    }

    suspend fun getCatImages(limit: Int = 10): Result<List<CatImage>> {
        return try {
            val images = apiService.fetchCatImages(limit = limit)
            cacheManager.addImagesToCache(images)
            Result.success(images)
        } catch (e: HttpException) {
             Result.failure(Exception("Erro no servidor: ${e.code()}"))
        } catch (e: IOException) {
            val cachedImages = cacheManager.getCachedImages()
            if (cachedImages.isNotEmpty()) {
                Result.success(cachedImages)
            } else {
                Result.failure(Exception("Sem ligação à internet e sem dados salvos na cache."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.localizedMessage}"))
        }
    }

    suspend fun getCatImageDetail(imageId: String): Result<CatImage> {
        return try {
            val image = apiService.fetchCatImageDetail(imageId)
            Result.success(image)
        } catch (e: HttpException) {
            Result.failure(Exception("Erro ao obter detalhes (Servidor: ${e.code()})"))
        } catch (e: IOException) {
            Result.failure(Exception("Sem ligação à internet para obter detalhes."))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado nos detalhes: ${e.localizedMessage}"))
        }
    }
}
