package dam.A51394.mygalleryapp.api

import dam.A51394.mygalleryapp.BuildConfig
import dam.A51394.mygalleryapp.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CatApiService {
    
    @GET("v1/images/search")
    suspend fun fetchCatImages(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY,
        @Query("limit") limit: Int = 10,
        @Query("has_breeds") hasBreeds: Int = 1
    ): List<CatImage>

    @GET("v1/images/{image_id}")
    suspend fun fetchCatImageDetail(
        @retrofit2.http.Path("image_id") imageId: String,
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY
    ): CatImage

}
