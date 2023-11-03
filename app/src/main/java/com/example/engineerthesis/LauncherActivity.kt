package com.example.engineerthesis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import com.example.engineerthesis.main.MainActivity

class LauncherActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        supportActionBar?.hide()

        progressBar = findViewById(R.id.progressBar2)

        val progressHandler = Handler()

        val maxProgress = 100 // Maximum progress bar value
        val progressInterval = 17 // Progress update interval in milliseconds
        val delayMillis = 1800

        progressBar.max = maxProgress

        val intent = Intent(this@LauncherActivity, MainActivity::class.java)

        // Code triggered after 1800 seconds
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, delayMillis.toLong())

        // Update the progress bar every "progressInterval" milliseconds
        progressHandler.post(object : Runnable {
            var progress = 0

            override fun run() {
                if (progress >= maxProgress) {
                    // When maximum progress is reached, hide the progress bar
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
