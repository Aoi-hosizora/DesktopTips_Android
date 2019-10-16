package com.aoihosizora.desktoptips.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.aoihosizora.desktoptips.util.swap
import com.getbase.floatingactionbutton.FloatingActionsMenu

class TabFragment : Fragment(), IContextHelper {

    companion object {
        const val TAB_IDX = "TAB_IDX"
    }

    private val listAdapter: TipItemAdapter?
        get() = view?.list_tipItem?.adapter as? TipItemAdapter

    private val tabIdx: Int by lazy {
        arguments!!.getInt(TAB_IDX, -1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)
        initUI(view)
        return view
    }

    /**
     * 返回键，受 MainAct 委托
     * @return 是否操作
     */
    fun onKeyBack(): Boolean {
        view?.run {
            // 1. Fab 展开
            if (fab.isExpanded) {
                fab.collapse()
                return true
            }
            // 2. List 多选
            listAdapter?.let {
                if (it.checkMode) {
                    it.checkMode = false
                    return@onKeyBack true
                }
            }
        }
        return false
    }

    /**
     * 初始化碎片参数 Fab 和 适配器
     */
    private fun initUI(view: View) {
        // Fab
        initFab(view)

        // List
        view.list_tipItem.setEmptyView(view.view_empty)
        view.list_tipItem.layoutManager = LinearLayoutManager(activity)
        view.list_tipItem.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val listAdapter = TipItemAdapter(
            context = context!!,
            tipItems = Global.tabs[tabIdx].tips,
            
            onItemClick = { _, tipItem -> onItemClick(tipItem) },
            onItemLongClick = { _, tipItem -> run {
                listAdapter?.checkMode = true
                listAdapter?.setItemChecked(tipItem, true)
                view.fab.expand()
            }},
            onCheckStateChanged = { }
        )

        view.list_tipItem.setItemViewCacheSize(0)
        view.list_tipItem.adapter = listAdapter
    }

    /**
     * 初始化 Fab UI / Action
     */
    private fun initFab(view: View) {
        // Menu
        view.view_fab_back.setOnClickListener {
            view.fab.collapse()
        }

        // Action Listener
        view.fab.setOnFloatingActionsMenuUpdateListener(object : FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {

            override fun onMenuCollapsed() {
                view.view_fab_back.visibility = View.GONE
            }

            override fun onMenuExpanded() {
                view.view_fab_back.visibility = View.VISIBLE

                // 初始化菜单
                if (listAdapter == null) return

                /////////////////////////////////////////
                // 显示相关

                // 选中长度
                val selLength: Int = listAdapter!!.getAllChecked().size

                val isCheck: Boolean = listAdapter!!.checkMode
                val isMulti: Boolean = isCheck && selLength > 1

                val showIfCheck = if (isCheck) View.VISIBLE else View.GONE
                val unShowIfCheck = if (!isCheck) View.VISIBLE else View.GONE
                // val showIfMulti = if (isCheck && isMulti) View.VISIBLE else View.GONE
                val unShowIfMulti = if (isCheck && !isMulti) View.VISIBLE else View.GONE

                view.fab_add.visibility = unShowIfCheck
                view.fab_exit_check.visibility = showIfCheck
                view.fab_more.visibility = showIfCheck
                view.fab_up.visibility = unShowIfMulti
                view.fab_down.visibility = unShowIfMulti

                // 以下为单选
                if (!isCheck || isMulti) return

                /////////////////////////////////////////
                // 位置相关

                // 列表长度
                val allSize: Int = Global.tabs[tabIdx].tips.size
                // 当前位置
                val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked()[0])

                view.fab_up.isEnabled = pos != 0
                view.fab_down.isEnabled = pos != allSize - 1
            }
        })

        // Button
        view.fab_add.setOnClickListener { // 新建
            newTip()
            view.fab.collapse()
        }

        view.fab_exit_check.setOnClickListener { // 退出多选
            listAdapter?.checkMode = false
            view.fab.collapse()
        }

        view.fab_up.setOnClickListener { // 上移
            moveTip(listAdapter?.getAllChecked()?.get(0), isMoveUp = true)
        }

        view.fab_down.setOnClickListener { // 下移
            moveTip(listAdapter?.getAllChecked()?.get(0), isMoveUp = false)
        }

        view.fab_more.setOnClickListener {  // 更多
            activity?.showToast("TODO")
            view.fab.collapse()
        }
    }

    /**
     * 修改完数据后更新适配器和存储
     */
    private fun refreshAfterUpdate() {
        view?.let {
            view?.list_tipItem?.notifyDataSetChanged()
            listAdapter?.notifyDataSetChanged()
            Global.saveData(activity!!)
        }
    }

    /**
     * 项目单击
     */
    private fun onItemClick(tipItem: TipItem) {
        val commands = arrayOf("复制", "编辑", "删除",
            if (tipItem.highLight) "取消高亮" else "高亮",
            "在浏览器打开", "关闭"
        )

        activity?.showAlert(
            title = tipItem.content,
            list = commands,
            listener = DialogInterface.OnClickListener { dialog, idx -> run {
                when (idx) {
                    0 -> copyTip(tipItem) // 复制
                    1 -> modifyTip(tipItem) // 编辑
                    2 -> deleteTip(tipItem) // 删除
                    3 -> highLightTip(tipItem) // 高亮
                    4 -> openBrowserTip(tipItem) // 浏览器
                    5 -> dialog.dismiss() // 关闭
                    else -> {
                        activity?.showToast("${commands[idx]}: ${tipItem.content}")
                    }
                }
            }}
        )
    }

    /**
     * 新建
     */
    private fun newTip() {
        activity?.showInputDlg(
            title = "新建",
            hint = "新记录",
            negText = "取消",
            posText = "添加",
            posClick = { _, _ , newText -> run {

                if (newText.isNotEmpty()) {
                    val tipItem = TipItem(newText)
                    if (Global.tabs[tabIdx].tips.add(tipItem)) {
                        refreshAfterUpdate()
                        activity?.showToast("已创建：\"${tipItem.content}\"")
                    } else {
                        activity?.showToast("创建失败：\"${tipItem.content}\"")
                    }
                }
            }}
        )
    }

    /**
     * 复制
     */
    private fun copyTip(tipItem: TipItem) {
        val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val data = ClipData.newPlainText("Label", tipItem.content)
        cm?.let {
            it.primaryClip = data
            activity?.showToast("${tipItem.content} 已复制")
        }
    }

    /**
     * 修改
     */
    private fun modifyTip(tipItem: TipItem) {
        // 初始值
        val preContent = tipItem.content

        activity?.showInputDlg(
            title = "编辑",
            text = tipItem.content,
            negText = "取消",
            posText = "修改",
            posClick = { _, _ , newText -> run {

                if (newText.isNotEmpty() && preContent != newText) {
                    tipItem.content = newText
                    refreshAfterUpdate()

                    activity?.showSnackBar(
                        message = "已修改：$preContent",
                        view = view!!,
                        action = "撤销",
                        listener = View.OnClickListener {
                            tipItem.content = preContent
                            refreshAfterUpdate()
                            activity?.showSnackBar(message = "已撤销修改", view = view!!)
                        }
                    )
                }
            }}
        )
    }

    /**
     * 删除
     */
    private fun deleteTip(tipItem: TipItem) {
        // 初始位置
        val tipIdx = Global.tabs[tabIdx].tips.indexOf(tipItem)

        activity?.showAlert(
            title = "删除",
            message = "确定删除 \"${tipItem.content}\" 吗？",
            posText = "删除",
            posListener = DialogInterface.OnClickListener { _, _ -> run {
                Global.tabs[tabIdx].tips.remove(tipItem)
                refreshAfterUpdate()

                activity?.showSnackBar(
                    message = "已删除：${tipItem.content}",
                    view = view!!,
                    action = "撤销",
                    listener = View.OnClickListener {
                        Global.tabs[tabIdx].tips.add(tipIdx, tipItem)
                        refreshAfterUpdate()
                        activity?.showSnackBar(message = "已恢复删除", view = view!!)
                    }
                )
            }},
            negText = "取消"
        )
    }

    /**
     * 高亮
     */
    private fun highLightTip(tipItem: TipItem) {
        tipItem.highLight = !tipItem.highLight
        refreshAfterUpdate()
        activity?.showToast("${tipItem.content} ${if (tipItem.highLight) "已高亮" else "已取消高亮"}")
    }

    /**
     * 上下移
     */
    private fun moveTip(tipItem: TipItem?, isMoveUp: Boolean) {
        if (tipItem != null) {
            val currIdx = Global.tabs[tabIdx].tips.indexOf(tipItem)
            val len = Global.tabs[tabIdx].tips.size

            if (isMoveUp && currIdx == 0) return
            if (!isMoveUp && currIdx == len - 1) return

            if (isMoveUp)   Global.tabs[tabIdx].tips.swap(currIdx, currIdx - 1)
            else            Global.tabs[tabIdx].tips.swap(currIdx, currIdx + 1)

            refreshAfterUpdate()

            val allSize: Int = Global.tabs[tabIdx].tips.size
            val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked()[0])

            view?.fab_up?.isEnabled     = pos != 0
            view?.fab_down?.isEnabled   = pos != allSize - 1
        }
    }

    /**
     * 打开浏览器
     */
    private fun openBrowserTip(tipItem: TipItem) {
        val sp = tipItem.content.split(" ")
        val links: MutableList<String> = mutableListOf()
        for (token in sp) {
            if (token.startsWith("http://") || token.startsWith("https://"))
                links.add(token)
        }
        if (links.isEmpty()) {
            activity?.showAlert(title = "用浏览器打开", message = "当前项中不包含任何链接。")
        } else {
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
    }
}