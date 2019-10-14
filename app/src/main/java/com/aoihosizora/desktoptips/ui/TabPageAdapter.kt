package com.aoihosizora.desktoptips.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.aoihosizora.desktoptips.model.Global

class TabPageAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        val arg = Bundle()
        arg.putInt(TabFragment.TAB_IDX, p0)

        val tabFrag = TabFragment()
        tabFrag.arguments = arg
        return tabFrag
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return Global.tabTitles[position]
    }

    override fun getCount(): Int {
        return Global.tabTitles.size
    }
}