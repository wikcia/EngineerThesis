package com.example.engineerthesis.photo

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import com.example.engineerthesis.R
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val makePhotoButton = findViewById<Button>(R.id.makePhotoButton)
        makePhotoButton.setOnClickListener {
            startPhotoMakingActivity()
        }
    }

    private fun startPhotoMakingActivity() {
        val intent = Intent(this, PhotoMakingActivity::class.java)
        startActivity(intent)
    }
}



