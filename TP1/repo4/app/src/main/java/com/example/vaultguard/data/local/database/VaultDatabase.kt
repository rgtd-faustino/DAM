package com.example.vaultguard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vaultguard.data.local.dao.PasswordDao
import com.example.vaultguard.data.local.entity.PasswordEntry

// diz ao room quais são as tabelas (cada entity é convertida numa tabela)
@Database(entities = [PasswordEntry::class], version = 1, exportSchema = false)
// abstract, o room implementar tudo automaticamente
abstract class VaultDatabase : RoomDatabase() {
    abstract val passwordDao: PasswordDao // para devolver o DAO
}
