package com.example.vaultguard.di

import android.content.Context
import androidx.room.Room
import com.example.vaultguard.data.local.dao.PasswordDao
import com.example.vaultguard.data.local.database.VaultDatabase
import com.example.vaultguard.data.remote.api.HaveIBeenPwnedApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides // fornece a instãncia
    @Singleton // só se cria uma
    // cria a base de dados uma vez e partilha a
    fun provideVaultDatabase(@ApplicationContext context: Context): VaultDatabase {
        return Room.databaseBuilder(
            context,
            VaultDatabase::class.java,
            "vault_database"
            // se for incrementada a versão da DB sem escrever uma migração, apaga tudo e recria
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePasswordDao(database: VaultDatabase): PasswordDao {
        return database.passwordDao
    }


    @Provides
    @Singleton
    // o retrofit cria automaticamente a implementação da interface: faz um pedido HTTP GET e devolve a resposta
    fun provideHaveIBeenPwnedApi(): HaveIBeenPwnedApi {
        return Retrofit.Builder()
            .baseUrl("https://api.pwnedpasswords.com/")
            .build()
            .create(HaveIBeenPwnedApi::class.java)
    }
}
