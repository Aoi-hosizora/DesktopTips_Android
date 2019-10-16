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
            
            onItemClick = { _, tipItem -> onItemsClick(listOf(tipItem)) },
            onItemLongClick = { _, tipItem -> run {
                listAdapter?.checkMode = true
                listAdapter?.setItemChecked(tipItem, true)
                view.fab.expand()
            }},
            onCheckedChanged = { _, _ -> run {
                // if (isChecked) {
                    // 更新多选信息
                    onSelectChange()
                // }
            }}
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
                // 多选模式不屏蔽蒙版
                if (!listAdapter!!.checkMode)
                    view.view_fab_back.visibility = View.VISIBLE

                // 初始化菜单
                if (listAdapter == null) return

                // 显示相关
                onSelectChange()
            }
        })

        // Button
        view.fab_add.setOnClickListener { // 新建
            newTips()
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
            listAdapter?.let {
                onItemsClick(it.getAllChecked())
            }
            view.fab.collapse()
        }
    }

    /**
     * 选中情况更改，Fab Menu 显示更新
     */
    private fun onSelectChange() {
        if (view == null) return

        /////////////////////////////////////////
        // 选中长度
        val selLength: Int = listAdapter!!.getAllChecked().size

        val isCheck: Boolean = listAdapter!!.checkMode
        val isMulti: Boolean = isCheck && selLength > 1

        val showIfCheck = if (isCheck) View.VISIBLE else View.GONE
        val unShowIfCheck = if (!isCheck) View.VISIBLE else View.GONE
        // val showIfMulti = if (isCheck && isMulti) View.VISIBLE else View.GONE
        val unShowIfMulti = if (isCheck && !isMulti) View.VISIBLE else View.GONE

        view!!.fab_add.visibility = unShowIfCheck
        view!!.fab_exit_check.visibility = showIfCheck
        view!!.fab_more.visibility = showIfCheck
        view!!.fab_up.visibility = unShowIfMulti
        view!!.fab_down.visibility = unShowIfMulti

        // 以下为非多选
        if (selLength > 1) return

        /////////////////////////////////////////
        // 位置相关

        if (selLength == 0) {
            // 没有选择
            view!!.fab_up.isEnabled = false
            view!!.fab_down.isEnabled = false
        } else {
            // 列表长度
            val allLength: Int = Global.tabs[tabIdx].tips.size
            // 当前位置
            val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked()[0])

            view!!.fab_up.isEnabled = pos != 0
            view!!.fab_down.isEnabled = pos != allLength - 1
        }
    }

    /**
     * 修改完数据后更新 适配器 和 存储
     */
    private fun refreshAfterUpdate() {
        view?.let {
            view?.list_tipItem?.notifyDataSetChanged()
            listAdapter?.notifyDataSetChanged()

            // TODO 多线程后台执行
            Global.saveData(activity!!)
        }
    }

    /**
     * 项目单击
     */
    private fun onItemsClick(tipItems: List<TipItem>) {
        var isHighLight = false
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
            if (it.highLight)
                isHighLight = true
        }

        val commands =
            if (tipItems.size == 1)
                arrayOf("复制", "编辑", "删除", if (isHighLight) "取消高亮" else "高亮", "在浏览器打开", "关闭")
            else
                arrayOf("复制", "查看", "删除", if (isHighLight) "取消高亮" else "高亮", "在浏览器打开", "关闭")

        val title =
            if (contents.size == 1)
                contents.first()
            else
                "共 ${contents.size} 项：${contents.joinToString(", ")}"

        activity?.showAlert(
            title = title,
            list = commands,
            listener = DialogInterface.OnClickListener { dialog, idx -> run {
                when (commands[idx]) {
                    "复制"            -> copyTips(tipItems)
                    "编辑"            -> modifyTip(tipItems.first())
                    "查看"            -> contentTips(tipItems)
                    "删除"            -> deleteTips(tipItems)
                    "高亮"            -> highLightTips(tipItems, true)
                    "取消高亮"         -> highLightTips(tipItems, false)
                    "在浏览器打开"     -> openBrowserTips(tipItems)
                    "关闭"            -> dialog.dismiss()
                    else -> {
                        activity?.showToast("${commands[idx]}: $title")
                    }
                }
            }}
        )
    }

    /**
     * 新建
     */
    private fun newTips() {
        activity?.showInputDlg(
            title = "新记录",
            hint = "新记录内容",
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
    private fun copyTips(tipItems: List<TipItem>) {
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
        }
        val content = contents.joinToString(", ")

        val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val data = ClipData.newPlainText("Label", content)
        cm?.let {
            it.primaryClip = data
            activity?.showToast("已复制 $content")
        }
    }

    /**
     * 修改
     */
    private fun modifyTip(tipItem: TipItem) {
        // 初始值
        val preContent = tipItem.content

        activity?.showInputDlg(
            title = "编辑记录",
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
     * 查看内容
     */
    private fun contentTips(tipItems: List<TipItem>) {
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
        }
        activity?.showAlert(
            title = "查看内容：共 ${contents.size} 项",
            message = contents.joinToString("\n"),
            negText = "返回",
            posText = "复制",
            posListener = DialogInterface.OnClickListener { _, _ -> copyTips(tipItems) }
        )
    }

    /**
     * 删除
     */
    private fun deleteTips(tipItems: List<TipItem>) {
        // 初始位置
        val tipIndies: MutableList<Int> = mutableListOf()
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
            tipIndies.add(Global.tabs[tabIdx].tips.indexOf(it))
        }
        val message =
            if (contents.size == 1)
                "确定删除记录 \"${contents.first()}\" 吗？"
            else
                "确定删除以下 ${contents.size} 条记录吗？\n${contents.joinToString("\n")}}"

        val message2 =
            if (contents.size == 1)
                "已删除：\"${contents.first()}\""
            else
                "已删除 ${contents.size} 条记录"


        activity?.showAlert(
            title = "删除记录",
            message = message,
            negText = "取消",
            posText = "删除",
            posListener = DialogInterface.OnClickListener { _, _ -> run {
                tipItems.forEach { Global.tabs[tabIdx].tips.remove(it) }
                refreshAfterUpdate()

                activity?.showSnackBar(
                    message = message2,
                    view = view!!,
                    action = "撤销",
                    listener = View.OnClickListener {
                        for (idx in 0 until contents.size) {
                            Global.tabs[tabIdx].tips.add(tipIndies[idx], tipItems[idx])
                        }
                        refreshAfterUpdate()
                        activity?.showSnackBar(message = "已恢复删除", view = view!!)
                    }
                )
            }}
        )
    }

    /**
     * 高亮
     */
    private fun highLightTips(tipItems: List<TipItem>, isHighLight: Boolean) {
        // 初始值
        val flags: MutableList<Boolean> = mutableListOf()
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
            flags.add(it.highLight)
        }
        tipItems.forEach { it.highLight = isHighLight }
        refreshAfterUpdate()
        val message = "${if (isHighLight) "已高亮 " else "已取消高亮 "}${if (tipItems.size == 1) contents.first() else "${tipItems.size} 条记录"}"

        activity?.showSnackBar(
            message = message,
            view = view!!,
            action = "撤销",
            listener = View.OnClickListener {
                for (idx in 0 until contents.size) {
                    tipItems[idx].highLight = flags[idx]
                }
                refreshAfterUpdate()
                activity?.showSnackBar(message = "已撤销操作", view = view!!)
            }
        )
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

            // 显示更新
            val allSize: Int = Global.tabs[tabIdx].tips.size
            val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked()[0])

            view?.fab_up?.isEnabled     = pos != 0
            view?.fab_down?.isEnabled   = pos != allSize - 1
        }
    }

    /**
     * 打开浏览器
     */
    private fun openBrowserTips(tipItems: List<TipItem>) {
        val sp: MutableList<String> = mutableListOf()
        tipItems.forEach { sp.addAll(it.content.split(" ")) }
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