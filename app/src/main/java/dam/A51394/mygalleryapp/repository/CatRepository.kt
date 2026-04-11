package dam.A51394.mygalleryapp.repository

import dam.A51394.mygalleryapp.api.CatApiService
import dam.A51394.mygalleryapp.model.CatImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CatRepository {

    private val apiService: CatApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        apiService = retrofit.create(CatApiService::class.java)
    }

    suspend fun getCatImages(limit: Int = 10): List<CatImage> {
        return apiService.fetchCatImages(limit)
    }
}
