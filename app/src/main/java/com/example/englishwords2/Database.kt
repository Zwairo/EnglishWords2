package com.example.englishwords2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [KelimeEntity::class],
    version = 2,   // 🔴 VERSION ARTIRILDI
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kelimeDao(): KelimeDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kelimeler.db"
                )
                    // 🔴 TABLOYU SİL – YENİDEN OLUŞTUR
                    .fallbackToDestructiveMigration()

                    // 🔹 İlk kurulumda seed data eklenir
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context)
                                    .kelimeDao()
                                    .insertAll(
                                        KelimeSeedData.getKelimeler()
                                    )
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}


