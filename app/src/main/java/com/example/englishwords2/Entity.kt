package com.example.englishwords2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kelimeler")
data class KelimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ingilizce: String,
    val turkce: String,
    val tur: String   // <-- YENİ ALAN
) {
    constructor(
        ingilizce: String,
        turkce: String,
        tur: String = "GENEL"   // <-- Varsayılan
    ) : this(
        id = 0,
        ingilizce = ingilizce,
        turkce = turkce,
        tur = tur
    )
}


