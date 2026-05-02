package dam.a51394.core.api

import dam.a51394.core.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {
    
    @GET("v1/images/search")
    suspend fun fetchCatImages(
        @Query("limit") limit: Int = 10,
        @Query("has_breeds") hasBreeds: Int = 1
    ): List<CatImage>

    @GET("v1/images/{image_id}")
    suspend fun fetchCatImageDetail(
        @retrofit2.http.Path("image_id") imageId: String,
    ): CatImage

}
