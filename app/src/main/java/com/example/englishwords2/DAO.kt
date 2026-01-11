package com.example.englishwords2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KelimeDao {

    // 🔹 Seed data eklemek için
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(kelimeler: List<KelimeEntity>)

    // 🔹 Oyun başında tüm kelimeleri almak için
    @Query("SELECT * FROM kelimeler")
    suspend fun getAllKelimeler(): List<KelimeEntity>

    @Query("""
    SELECT turkce FROM kelimeler 
    WHERE id != :dogruId 
    AND tur = :tur
    ORDER BY RANDOM() 
    LIMIT 3
""")
    suspend fun getYanlisSeceneklerByTur(
        dogruId: Int,
        tur: String
    ): List<String>




    @Query("SELECT * FROM kelimeler WHERE tur = :tur")
    suspend fun getKelimelerByTur(tur: String): List<KelimeEntity>


    // 🔹 Rastgele 1 kelime (yedek amaçlı)
    @Query("SELECT * FROM kelimeler ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomKelime(): KelimeEntity?

    // 🔹 Yanlış şıklar (doğru kelime hariç)


    // 🔹 Kontrol amaçlı: tabloda kaç kayıt var?
    @Query("SELECT COUNT(*) FROM kelimeler")
    suspend fun getKelimeSayisi(): Int
}
