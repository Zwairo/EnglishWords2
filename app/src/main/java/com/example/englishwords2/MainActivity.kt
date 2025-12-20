package com.example.englishwords2


import android.content.SharedPreferences
import com.example.englishwords2.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    private lateinit var kalanKelimeler: MutableList<KelimeEntity>
    private lateinit var dogruKelime: KelimeEntity

    private var skor = 0
    private var oyunBitti = false
    private lateinit var prefs: SharedPreferences
    private var highScore = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        prefs = getSharedPreferences("score_prefs", MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)

        binding.textHighScore.text = "High Score: $highScore"

        binding.btnRestart.setOnClickListener {
            oyunuBaslat()
        }

        oyunuBaslat()
    }

    private fun oyunuBaslat() {
        binding.textHighScore.text = "High Score: $highScore"

        skor = 0
        oyunBitti = false

        binding.textSkor.text = "Skor: $skor"
        binding.textOyunBitti.visibility = View.GONE
        binding.btnRestart.visibility = View.GONE

        setButonlarEnabled(true)

        lifecycleScope.launch {
            kalanKelimeler = db.kelimeDao().getAllKelimeler().toMutableList()
            yeniSoruYukle()
        }
    }

    private fun yeniSoruYukle() {
        if (oyunBitti) return

        // 🎉 KAZANMA DURUMU
        if (kalanKelimeler.isEmpty()) {
            oyunuKazan()
            return
        }

        dogruKelime = kalanKelimeler.random()

        lifecycleScope.launch {
            val yanlislar = db.kelimeDao()
                .getYanlisSecenekler(dogruKelime.id)

            val secenekler = mutableListOf<String>()
            secenekler.add(dogruKelime.turkce)
            secenekler.addAll(yanlislar)
            secenekler.shuffle()

            binding.textIngilizce.text = dogruKelime.ingilizce

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
            binding.textSkor.text = "Skor: $skor"
            button.setBackgroundColor(Color.GREEN)

            // ✅ Doğru bilinen kelimeyi listeden çıkar
            kalanKelimeler.remove(dogruKelime)

            lifecycleScope.launch {
                delay(600)
                setButonlarEnabled(true)
                yeniSoruYukle()
            }

        } else {
            button.setBackgroundColor(Color.RED)
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

    private fun oyunuBitir() {
        if (skor > highScore) {
            highScore = skor
            prefs.edit().putInt("high_score", highScore).apply()
        }

        oyunBitti = true
        setButonlarEnabled(false)

        binding.textOyunBitti.text = "Oyun Bitti\nSkorunuz: $skor"
        binding.textOyunBitti.visibility = View.VISIBLE
        binding.btnRestart.visibility = View.VISIBLE
    }

    private fun oyunuKazan() {
        oyunBitti = true
        setButonlarEnabled(false)

        binding.textOyunBitti.text = "🎉 OYUN BİTTİ\nKAZANDINIZ\nSkorunuz: $skor"
        binding.textOyunBitti.visibility = View.VISIBLE
        binding.btnRestart.visibility = View.VISIBLE
    }

    private fun setButonlarEnabled(enabled: Boolean) {
        binding.btn1.isEnabled = enabled
        binding.btn2.isEnabled = enabled
        binding.btn3.isEnabled = enabled
        binding.btn4.isEnabled = enabled
    }
}
