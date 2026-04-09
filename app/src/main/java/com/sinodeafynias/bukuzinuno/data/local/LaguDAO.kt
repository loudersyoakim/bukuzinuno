package com.sinodeafynias.bukuzinuno.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LaguDao {

    // Memasukkan daftar lagu dari JSON atau Firebase, timpa jika id-nya sama
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemuaLagu(lagu: List<Lagu>)

    // Mengambil semua lagu untuk halaman Daftar (A-Z)
    @Query("SELECT * FROM lagu ORDER BY nomor_urut ASC")
    fun getSemuaLagu(): Flow<List<Lagu>>

    // Mencari lagu berdasarkan judul, lirik, atau nomor
    @Query("SELECT * FROM lagu WHERE judul LIKE '%' || :keyword || '%' OR lirik LIKE '%' || :keyword || '%' OR nomor LIKE '%' || :keyword || '%'")
    fun cariLagu(keyword: String): Flow<List<Lagu>>

    // Mengambil daftar lagu yang difavoritkan
    @Query("SELECT * FROM lagu WHERE isFavorit = 1")
    fun getLaguFavorit(): Flow<List<Lagu>>

    // Update status favorit (Toggle)
    @Query("UPDATE lagu SET isFavorit = :status WHERE id = :idLagu")
    suspend fun updateFavorit(idLagu: String, status: Boolean)

    @Query("SELECT * FROM lagu")
    suspend fun getSemuaLaguList(): List<Lagu>
}