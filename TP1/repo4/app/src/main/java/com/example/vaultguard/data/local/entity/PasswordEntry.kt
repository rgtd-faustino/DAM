package com.example.vaultguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


// esta classe é uma tabela de base de dados
@Entity(tableName = "password_entries")
// data class -> cria automaticamente equals(), hashcode(), toString() e copy(),
// é usada quando queremos guardar dados
data class PasswordEntry(
    // fazemos o id ser a primary key, auto generate para ser mais facil (1, 2, 3 ...)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String, // Plain text to show in list
    val username: String, // Encrypted (Base64)
    val password: String, // Encrypted (Base64)
    val notes: String, // Encrypted (Base64), empty if none
    val createdAt: Long = System.currentTimeMillis() // timestamp
)
