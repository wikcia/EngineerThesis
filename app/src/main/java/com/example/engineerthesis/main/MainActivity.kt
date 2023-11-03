package com.example.engineerthesis.main


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.example.engineerthesis.R
import com.example.engineerthesis.bluetooth.FragmentMain
import com.example.engineerthesis.bluetooth.TextSendListener
import com.google.android.material.tabs.TabLayout

/**
 * Note: This only works if you manually grant BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions to the application.
 */
class MainActivity : AppCompatActivity(), TextSendListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val adapter = MyPagerAdapter(supportFragmentManager)

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

    }

    /* Main Activity acts as a mediator between FragmentMain, where the bluetooth communication interface is defined, and other fragments */
    override fun onTextSend(text: String) {
        // Pass the text to FragmentMain to be sent via Bluetooth to the car

        val fragmentManager = supportFragmentManager
        val fragmentMain = fragmentManager.fragments.find { it.arguments?.getString("tag") == "Fragment_0" } as FragmentMain?
        fragmentMain?.sendTextViaBluetooth(text)
    }
}
