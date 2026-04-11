package dam.A51394.mygalleryapp.api

import dam.A51394.mygalleryapp.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {
    
    @GET("v1/images/search")
    suspend fun fetchCatImages(
        @Query("limit") limit: Int = 10
    ): List<CatImage>

}
