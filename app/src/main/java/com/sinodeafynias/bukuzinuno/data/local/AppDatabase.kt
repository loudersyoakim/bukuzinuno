package com.sinodeafynias.bukuzinuno.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Lagu::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // <--- INI WAJIB ADA AGAR LIST<STRING> BISA DISIMPAN
abstract class AppDatabase : RoomDatabase() {

    abstract fun laguDao(): LaguDao // <--- PASTIKAN TULISANNYA BEGINI

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lagu_database"
                )
                    // Jika kamu pernah run aplikasi sebelumnya, ini akan mencegah crash kalau struktur berubah
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}