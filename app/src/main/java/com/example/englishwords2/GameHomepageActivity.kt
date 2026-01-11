package com.example.englishwords2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.englishwords2.databinding.ActivityGameHomepageBinding
import androidx.core.content.edit

class GameHomepageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameHomepageBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameModes = listOf(
            GameMode(
                id = 1,
                tur = "GENEL",
                title = "Genel Kelimeler",
                subtitle = "Sonsuz Mod, Süreli Mod",
                isLocked = false,
            ),
            GameMode(
                id = 2,
                tur="VERBS",
                title = "Fiiller (VERBS)",
                subtitle = "İngilizce Fiil Bilme Oyunu",
                isLocked = !isModeUnlocked(2),
                unlockCost = 1
            ),GameMode(
                id = 3,
                tur="ADJECTIVES",
                title = "Sıfatlar (ADJECTIVES)",
                subtitle = "İngilizce Sıfat Bilme Oyunu",
                isLocked = !isModeUnlocked(3),
                unlockCost = 1
            )

        )

        binding.recyclerGameModes.layoutManager =
            GridLayoutManager(this, 2)

        binding.recyclerGameModes.adapter =
            GameModeAdapter(gameModes) { selectedMode ->
                handleGameModeClick(selectedMode)
            }

    }

    override fun onResume() {
        super.onResume()
        coinGuncelle()
    }
    private fun coinGuncelle() {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val coin = prefs.getInt(Constants.KEY_COIN, 0)

        binding.txtCoin.text = "$coin"
    }

    private fun getCoin(): Int {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        return prefs.getInt(Constants.KEY_COIN, 0)
    }

    private fun setCoin(value: Int) {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        prefs.edit { putInt(Constants.KEY_COIN, value) }
    }

    private fun isModeUnlocked(modeId: Int): Boolean {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        return prefs.getBoolean("mode_unlocked_$modeId", false)
    }

    private fun unlockMode(modeId: Int) {
        val prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        prefs.edit {
            putBoolean("mode_unlocked_$modeId", true)
        }
    }



    private fun handleGameModeClick(mode: GameMode) {

        if (!mode.isLocked) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GAME_MODE",mode.tur)
            startActivity(intent)
            return
        }

        val coin = getCoin()

        if (coin >= mode.unlockCost) {
            // coin düş
            setCoin(coin - mode.unlockCost)

            // kilidi aç
            unlockMode(mode.id)

            Toast.makeText(
                this,
                "${mode.title} açıldı!",
                Toast.LENGTH_SHORT
            ).show()

            recreate() // RecyclerView yenilensin

        } else {
            Toast.makeText(
                this,
                "Yetersiz coin (${mode.unlockCost} gerekli)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
