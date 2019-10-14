package com.aoihosizora.desktoptips.ui

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
import kotlinx.android.synthetic.main.fragment_tab.view.*
import android.support.v7.widget.DividerItemDecoration
import com.aoihosizora.desktoptips.model.TipItem

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
     * 初始化碎片参数和适配器
     */
    private fun initUI(view: View) {
        view.list_tipItem.setEmptyView(view.view_empty)
        view.list_tipItem.layoutManager = LinearLayoutManager(activity)
        view.list_tipItem.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val listAdapter = TipItemAdapter(
            context = context!!,
            tipItems =  Global.tabs[tabIdx].tips,
            
            onItemClick = { tipItem -> onItemClick(tipItem) },
            onItemLongClick = { tipItem -> onItemLongClick(tipItem) }
        )

        view.list_tipItem.adapter = listAdapter
    }

    /**
     * 项目单击
     */
    private fun onItemClick(tipItem: TipItem) {
        val commands = arrayOf(
            "复制", "编辑", "删除",
            if (tipItem.highLight) "取消高亮" else "高亮",
            "打开浏览器", "关闭"
        )
        activity?.showAlert(
            title = "操作\n(${tipItem.content})",
            list = commands,
            listener = DialogInterface.OnClickListener { _, idx -> run {
                when (idx) {
                    3 -> {
                        tipItem.highLight = !tipItem.highLight
                        view?.list_tipItem?.adapter?.notifyDataSetChanged()
                    }
                    else -> {
                        activity?.showToast("${commands[idx]}: ${tipItem.content}")
                    }
                }
            }}
        )
    }

    /**
     * 项目长按
     */
    private fun onItemLongClick(tipItem: TipItem) {
        activity?.showToast("LongClick: ${tipItem.content}")
    }
}