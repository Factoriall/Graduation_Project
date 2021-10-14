package org.techtown.graduateproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.techtown.graduateproject.fragments.RecordHistoryFragment
import org.techtown.graduateproject.fragments.RecordOverallFragment

class RecordPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) RecordOverallFragment()
        else RecordHistoryFragment()
    }
}