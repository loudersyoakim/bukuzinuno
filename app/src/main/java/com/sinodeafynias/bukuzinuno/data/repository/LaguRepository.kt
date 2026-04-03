package com.sinodeafynias.bukuzinuno.data.repository

import com.sinodeafynias.bukuzinuno.data.local.Lagu
import com.sinodeafynias.bukuzinuno.data.local.LaguDao // <--- Sudah diganti jadi LaguDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LaguRepository(private val laguDao: LaguDao) { // <--- Sudah diganti jadi LaguDao

    val semuaLagu: Flow<List<Lagu>> = laguDao.getSemuaLagu()

    val laguFavorit: Flow<List<Lagu>> = laguDao.getLaguFavorit()

    fun cariLagu(keyword: String): Flow<List<Lagu>> {
        return laguDao.cariLagu(keyword)
    }

    suspend fun updateFavorit(id: String, status: Boolean) {
        withContext(Dispatchers.IO) {
            laguDao.updateFavorit(id, status)
        }
    }

    suspend fun insertSemuaLagu(lagu: List<Lagu>) {
        withContext(Dispatchers.IO) {
            laguDao.insertSemuaLagu(lagu)
        }
    }
}