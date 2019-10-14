package com.aoihosizora.desktoptips.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.model.TipItem
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
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
     * 初始化碎片参数
     */
    private fun initUI(view: View) {
        view.list_tipItem.setEmptyView(view.view_empty)
        view.list_tipItem.layoutManager = LinearLayoutManager(activity)

        val listAdapter = TipItemAdapter(
            tipItems =  Global.tabs[tabIdx].tips,
            onItemClick = { _, tipItem -> run {
                activity?.showToast("Click: ${tipItem.content}")
            }},
            onItemLongClick = { _, tipItem -> run {
                activity?.showToast("LongClick: ${tipItem.content}")
                true
            }}
        )

        view.list_tipItem.adapter = listAdapter
    }
}