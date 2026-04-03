package com.sinodeafynias.bukuzinuno.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lagu")
data class Lagu(
    @PrimaryKey val id: String,
    val nomor_urut: Int,
    val nomor: String,
    val judul: String,
    val kategori: String,
    val nada: String,
    val lirik: List<String>,
    val version: Int = 1,
    val isFavorit: Boolean = false
)