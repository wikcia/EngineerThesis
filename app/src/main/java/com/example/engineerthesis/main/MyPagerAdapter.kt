package com.example.engineerthesis.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.engineerthesis.bluetooth.FragmentDisplayText
import com.example.engineerthesis.bluetooth.FragmentMain
import com.example.engineerthesis.photo.FragmentCamera


class MyPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        val fragment = when (position) {
            0 -> FragmentMain()
            2 -> FragmentCamera()
            1 -> FragmentDisplayText()
            else -> FragmentMain()
        }

        // Assign tag based on item position
        val tag = "Fragment_$position"
        fragment.arguments = Bundle().apply { putString("tag", tag) }

        return fragment
    }

    override fun getCount(): Int {
        return 3
    }
}