package com.aoihosizora.desktoptips.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
import kotlinx.android.synthetic.main.fragment_tab.view.*
import android.support.v7.widget.DividerItemDecoration
import android.widget.EditText
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
        val adapter = view?.list_tipItem?.adapter
        val commands = arrayOf("复制", "编辑", "删除",
            if (tipItem.highLight) "取消高亮" else "高亮",
            "在浏览器打开", "关闭"
        )
        activity?.showAlert(
            title = tipItem.content,
            list = commands,
            listener = DialogInterface.OnClickListener { dialog, idx -> run {
                when (idx) {
                    0 -> { // 复制
                        val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val data = ClipData.newPlainText("Label", tipItem.content)
                        cm?.let {
                            it.primaryClip = data
                            activity?.showToast("${tipItem.content} 已复制")
                        }
                    }
                    1 -> { // 编辑
                        // 初始值
                        val preContent = tipItem.content

                        val edt = EditText(context!!)
                        edt.setText(tipItem.content)
                        edt.setSingleLine(true)

                        activity?.showAlert(
                            title = "编辑",
                            view = edt,
                            negText = "取消",
                            posText = "修改",
                            posListener = DialogInterface.OnClickListener { _, _ -> run {

                                if (edt.text.isNotEmpty() && preContent != edt.text.toString()) {
                                    tipItem.content = edt.text.toString()
                                    adapter?.notifyDataSetChanged()
                                    Global.saveData(activity!!)

                                    activity?.showSnackBar(
                                        message = "已经修改：${preContent}",
                                        view = view!!,
                                        action = "撤销",
                                        listener = View.OnClickListener {
                                            tipItem.content = preContent
                                            adapter?.notifyDataSetChanged()
                                            activity?.showSnackBar(message = "已撤销修改", view = view!!)
                                        }
                                    )
                                }
                            }}
                        )
                    }
                    2 -> { // 删除
                        // 初始位置
                        val tipIdx = Global.tabs[tabIdx].tips.indexOf(tipItem)

                        activity?.showAlert(
                            title = "删除",
                            message = "确定删除 \"${tipItem.content}\" 吗？",
                            posText = "删除",
                            posListener = DialogInterface.OnClickListener { _, _ -> run {
                                Global.tabs[tabIdx].tips.remove(tipItem)
                                adapter?.notifyDataSetChanged()
                                Global.saveData(activity!!)

                                activity?.showSnackBar(
                                    message = "已删除：${tipItem.content}",
                                    view = view!!,
                                    action = "撤销",
                                    listener = View.OnClickListener {
                                        Global.tabs[tabIdx].tips.add(tipIdx, tipItem)
                                        adapter?.notifyDataSetChanged()
                                        activity?.showSnackBar(message = "已恢复删除", view = view!!)
                                    }
                                )
                            }},
                            negText = "取消"
                        )
                    }
                    3 -> { // 高亮
                        tipItem.highLight = !tipItem.highLight
                        adapter?.notifyDataSetChanged()
                        Global.saveData(activity!!)
                    }
                    4 -> { // 浏览器
                        val sp = tipItem.content.split(" ")
                        val links: MutableList<String> = mutableListOf()
                        for (token in sp) {
                            if (token.startsWith("http://") || token.startsWith("https://"))
                                links.add(token)
                        }
                        activity?.showAlert(
                            title = "用浏览器打开",
                            message = "是否打开以下 ${links.size} 个链接：\n\n" + links.joinToString("\n"),
                            posText = "打开",
                            posListener = DialogInterface.OnClickListener { _, _ -> run {
                                activity?.showBrowser(links)
                            }},
                            negText = "取消"
                        )
                    }
                    5 -> { // 关闭
                        dialog.dismiss()
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
        // activity?.showToast("LongClick: ${tipItem.content}")
    }
}