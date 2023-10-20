package com.example.engineerthesis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import com.example.engineerthesis.bluetooth.MainActivity

class LauncherActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        supportActionBar?.hide()

        progressBar = findViewById(R.id.progressBar2)

        val progressHandler = Handler()

        val maxProgress = 100 // Maksymalna wartość paska postępu
        val progressInterval = 17 // Interwał aktualizacji postępu w milisekundach
        val delayMillis = 1800 // Czas opóźnienia w milisekundach (10 sekund)

        progressBar.max = maxProgress

        val intent = Intent(this@LauncherActivity, MainActivity::class.java)

        // Kod uruchamiany po upływie 10 sekund
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, delayMillis.toLong())

        // Aktualizacja paska postępu co "progressInterval" milisekund
        progressHandler.post(object : Runnable {
            var progress = 0

            override fun run() {
                if (progress >= maxProgress) {
                    // Po osiągnięciu maksymalnego postępu ukryj pasek postępu
                    progressBar.visibility = View.INVISIBLE
                } else {
                    progressBar.progress = progress
                    progressHandler.postDelayed(this, progressInterval.toLong())
                }
                progress++
            }
        })
    }
}
