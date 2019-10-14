package com.aoihosizora.desktoptips.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import kotlinx.android.synthetic.main.fragment_tab.view.*

class TabFragment : Fragment(), IContextHelper {

    companion object {
        const val TAB_IDX = "TAB_IDX"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)

        arguments?.let {
            val idx = it.getInt(TAB_IDX, -1)
            view.txt_main.text = if (idx in 0 until Global.tabTitles.size) Global.tabTitles[idx] else "Not Found"
        }

        return view
    }
}