package dam.a51394.nevazio.di

import dam.a51394.nevazio.data.repository.AuthRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import dam.a51394.nevazio.data.repository.RecipeRepository
import dam.a51394.nevazio.data.remote.RecipeApiService
import dam.a51394.nevazio.ui.home.HomeViewModel
import dam.a51394.nevazio.ui.login.LoginViewModel
import dam.a51394.nevazio.ui.register.RegisterViewModel
import dam.a51394.nevazio.ui.recipes.RecipesViewModel
import dam.a51394.nevazio.ui.shopping.ShoppingViewModel
import dam.a51394.nevazio.ui.recipe.RecipeViewModel
import dam.a51394.nevazio.ui.profile.ProfileViewModel
import dam.a51394.nevazio.worker.ExpiryAlertWorker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.androidx.workmanager.dsl.worker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // Network
    single {
        Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApiService::class.java)
    }

    // Repositories
    single { AuthRepository() }
    single { FridgeRepository() }
    single { RecipeRepository(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { RecipesViewModel(get(), get(), get()) }
    viewModel { ShoppingViewModel(get(), get()) }
    viewModel { params -> RecipeViewModel(params.get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get()) }

    // Worker
    worker { ExpiryAlertWorker(get(), get(), get(), get()) }
}
