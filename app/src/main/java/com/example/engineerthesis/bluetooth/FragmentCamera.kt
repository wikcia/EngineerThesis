package com.example.engineerthesis.bluetooth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import android.content.Intent
import android.widget.Button
import com.example.engineerthesis.R

class FragmentCamera : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)


        val makePhotoButton = view.findViewById<Button>(R.id.makePhotoButton)
        makePhotoButton.setOnClickListener {
            startPhotoMakingActivity(view)
        }

        return view
    }

    // Metoda wywoływana po kliknięciu przycisku "Make a Photo"
    private fun startPhotoMakingActivity(view: View) {
        val intent = Intent(activity, PhotoMakingActivity::class.java)
        startActivity(intent)
    }
}

