package com.example.vaultguard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vaultguard.data.local.entity.PasswordEntry
import kotlinx.coroutines.flow.Flow

// diz ao room (biblioteca de base de dados) que é um dao
@Dao // -> data access object -> interface que liga o código e a base de dados

// a interface define o que as funções fazem (mas não como)
interface PasswordDao {
    // apanha todas as passwords ordenadas crescente de A a Z
    @Query("SELECT * FROM password_entries ORDER BY title ASC")
    // Flow é um "stream" de dados reativo ou seja quando os dados mudam na base de dados
    // o Flow emite automaticamente a nova lista, tipo estar subscrito a um evento
    fun getAllEntries(): Flow<List<PasswordEntry>>

    // apanha a password que tiver o id que é depois substituído
    @Query("SELECT * FROM password_entries WHERE id = :id")
    // suspend para poder ser pausada sem bloquear a thread (bom uso para operações lentas como em
    // bases de dados)
    suspend fun getEntryById(id: Int): PasswordEntry? // ? -> pode devolver nulo se não encontrar

    // insert -> o room cria o sql de inserção automaticamente
    // se já houver uma entrada com o mesmo ID então substitui
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PasswordEntry)

    @Update
    suspend fun updateEntry(entry: PasswordEntry)

    @Delete
    suspend fun deleteEntry(entry: PasswordEntry)
}
