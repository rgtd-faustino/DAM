package dam.A51394.mygalleryapp.model

import com.google.gson.annotations.SerializedName

data class CatImage(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int?,

    @SerializedName("height")
    val height: Int?,

    @SerializedName("breeds")
    val breeds: List<Breed>? = null,

    var isFavourite: Boolean = false
)

data class Breed(
    @SerializedName("name")
    val name: String,

    @SerializedName("origin")
    val origin: String?,

    @SerializedName("temperament")
    val temperament: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("life_span")
    val lifeSpan: String?,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?
)
