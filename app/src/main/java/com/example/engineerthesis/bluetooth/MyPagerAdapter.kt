package com.example.engineerthesis.bluetooth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class MyPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentMain()
            1 -> FragmentCamera()
            else -> FragmentMain()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}