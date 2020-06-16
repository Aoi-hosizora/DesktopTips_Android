package com.aoihosizora.desktoptips.ui.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.ui.view.TabFragment

class TabPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val arg = Bundle()
        arg.putInt(TabFragment.BDL_TAB_IDX, position)

        val tabFrag = TabFragment()
        tabFrag.arguments = arg
        return tabFrag
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return Global.tabs[position].title
    }

    override fun getCount(): Int {
        return Global.tabs.size
    }
}
