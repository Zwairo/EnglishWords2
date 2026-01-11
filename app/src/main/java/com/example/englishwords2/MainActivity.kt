package com.example.englishwords2


import android.content.SharedPreferences
import android.content.res.ColorStateList
import com.example.englishwords2.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private var coinEklendi = false


    private lateinit var kalanKelimeler: MutableList<KelimeEntity>
    private lateinit var dogruKelime: KelimeEntity

    private var skor = 0
    private var oyunBitti = false
    private lateinit var prefs: SharedPreferences
    private var highScore = 0
    private lateinit var soundPool: SoundPool
    private  lateinit var gameMode: String
    private var soundCorrect = 0
    private var soundWrong = 0
    private val defaultButtonTextColor by lazy {
        binding.btn1.currentTextColor
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameMode = intent.getStringExtra("GAME_MODE").toString()
        db = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences("score_prefs", MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)


        soundPool = SoundPool.Builder().setMaxStreams(3).build()

        soundCorrect = soundPool.load(this, R.raw.correct, 1)
        soundWrong = soundPool.load(this, R.raw.wrong, 1)




        binding.textHighScore.text = "High Score: $highScore"

        binding.btnRestart.setOnClickListener {
            coinEkle()
            skor = 0


            binding.gameOverCard.visibility = View.GONE
            binding.answersLayout.visibility = View.VISIBLE
            binding.cardWord.visibility = View.VISIBLE

            oyunuBaslat()
        }
        onBackPressedDispatcher.addCallback(this) {
            coinEkle()     // 👈 geri tuşunda coin eklenecek
            finish()       // 👈 activity kapansın
        }


        oyunuBaslat()
    }


    private fun oyunuBaslat() {

        skor = 0
        oyunBitti = false

        binding.textScore.text = "Skor: $skor"
        binding.txtGameOver.visibility = View.GONE
        binding.btnRestart.visibility = View.GONE

        setButonlarEnabled(false) // 🔴 başta kapalı

        lifecycleScope.launch {

            // 🔹 DB dolana kadar bekle
            var kelimeler = db.kelimeDao().getKelimelerByTur(gameMode)

            while (kelimeler.isEmpty()) {
                delay(100) // 0.1 saniye
                kelimeler = db.kelimeDao().getKelimelerByTur(gameMode)
            }

            // 🔹 Artık DB hazır
            kalanKelimeler = kelimeler.toMutableList()

            setButonlarEnabled(true)
            yeniSoruYukle()
        }
    }



    private fun yeniSoruYukle() {
        if (oyunBitti) return
        resetAnswerButtons()
        // 🎉 KAZANMA DURUMU
        if (kalanKelimeler.isEmpty()) {
            oyunuKazan()
            return
        }

        dogruKelime = kalanKelimeler.random()

        lifecycleScope.launch {
            val yanlislar = db.kelimeDao()
                .getYanlisSeceneklerByTur(
                    dogruKelime.id,
                    dogruKelime.tur
                )


            val secenekler = mutableListOf<String>()
            secenekler.add(dogruKelime.turkce)
            secenekler.addAll(yanlislar)
            secenekler.shuffle()

            binding.textEnglish.text = dogruKelime.ingilizce

            val butonlar = listOf(
                binding.btn1,
                binding.btn2,
                binding.btn3,
                binding.btn4
            )

            butonlar.forEachIndexed { index, button ->
                button.text = secenekler[index]
                button.setBackgroundColor(Color.LTGRAY)
                button.setOnClickListener {
                    cevapKontrol(button, secenekler[index])
                }
            }
        }
    }

    private fun cevapKontrol(button: Button, secilen: String) {
        if (oyunBitti) return

        setButonlarEnabled(false)

        if (secilen == dogruKelime.turkce) {
            skor++
            binding.textScore.text = "Skor: $skor"
            button.setBackgroundColor(Color.GREEN)
            soundPool.play(soundCorrect, 1f, 1f, 1, 0, 1f)


            // ✅ Doğru bilinen kelimeyi listeden çıkar
            kalanKelimeler.remove(dogruKelime)

            lifecycleScope.launch {
                delay(600)
                setButonlarEnabled(true)
                yeniSoruYukle()
            }

        } else {
            button.setBackgroundColor(Color.RED)
            button.setTextColor(Color.WHITE)
            soundPool.play(soundWrong, 1f, 1f, 1, 0, 1f)

            val butonlar = listOf(
                binding.btn1,
                binding.btn2,
                binding.btn3,
                binding.btn4
            )

            butonlar.forEach {
                if (it.text == dogruKelime.turkce) {
                    it.setBackgroundColor(Color.GREEN)
                }
            }

            oyunuBitir()
        }
    }
    private fun resetAnswerButtons() {
        val buttons = listOf(binding.btn1, binding.btn2, binding.btn3, binding.btn4)

        buttons.forEach {
            it.setTextColor(defaultButtonTextColor)
            it.backgroundTintList = ColorStateList.valueOf(
                getColor(R.color.textPrimary)
            )
            it.isEnabled = true
        }
    }

    private fun showGameOver(
        title: String,
        message: String,

    ) {
        oyunBitti = true
        setButonlarEnabled(false)

        // High score
        if (skor > highScore) {
            highScore = skor
            prefs.edit().putInt("high_score", highScore).apply()
        }

        // Coin ekle (her doğru = 1 coin → skor kadar)


        lifecycleScope.launch {
            delay(1500)

            binding.txtGameOver.text = title
            binding.txtFinalScore.text = message

            binding.gameOverCard.visibility = View.VISIBLE
            binding.btnRestart.visibility = View.VISIBLE

            binding.answersLayout.visibility = View.GONE
            binding.cardWord.visibility = View.GONE
        }
    }

    private fun oyunuBitir() {
        showGameOver(
            title = "❌ OYUN BİTTİ",
            message = "Skor: $skor",


        )
    }
    private fun oyunuKazan() {
        showGameOver(
            title = "🎉 KAZANDIN!",
            message = "Tüm soruları bildin\nSkor: $skor"


        )
    }



    private fun setButonlarEnabled(enabled: Boolean) {
        binding.btn1.isEnabled = enabled
        binding.btn2.isEnabled = enabled
        binding.btn3.isEnabled = enabled
        binding.btn4.isEnabled = enabled
    }


    private fun coinEkle() {
        if (coinEklendi) return
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val mevcutCoin = prefs.getInt(Constants.KEY_COIN, 0)

        val yeniCoin = mevcutCoin + skor

        Log.d("COIN_TEST", "Eski: $mevcutCoin Yeni: $yeniCoin")

        prefs.edit()
            .putInt(Constants.KEY_COIN, yeniCoin)
            .apply()
    }



}
