package com.aoihosizora.desktoptips.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.android.synthetic.main.fragment_tab.view.*

class TabFragment : Fragment(), IContextHelper {

    companion object {
        const val TAB_IDX = "TAB_IDX"
    }

    private val tabIdx: Int by lazy {
        arguments!!.getInt(TAB_IDX, -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)
        initUI(view)

        return view
    }

    /**
     * 初始化碎片参数，
     */
    private fun initUI(view: View) {

        view.txt_main.text = if (tabIdx in 0 until Global.tabTitles.size)
            Global.tabTitles[tabIdx]
            else "Not Found"

        view.btn_main.setOnClickListener {
            activity?.showAlert(message = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Global.tabs), title = "")
        }
    }
}