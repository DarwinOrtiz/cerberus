package com.salmoukas.cerberus.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return StatusListFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Status"
    }

    override fun getCount(): Int {
        return 1
    }
}
