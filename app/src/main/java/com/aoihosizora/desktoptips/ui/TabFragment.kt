package com.aoihosizora.desktoptips.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(), IContextHelper {

    companion object {
        // const val TAG = "TabFragment"

        /**
         * Bundle Arg
         */
        const val BDL_TAB_IDX = "BDL_TAB_IDX"
    }

    // Fun: onCreateView onKeyBack initUI initFab refreshAfterUpdate onItemsClick
    // Var: listAdapter tabIdx
    // region 界面更新与交互

    private val listAdapter: TipItemAdapter?
        get() = view?.list_tipItem?.adapter as? TipItemAdapter

    private val tabIdx: Int by lazy {
        arguments!!.getInt(BDL_TAB_IDX, -1)
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
            // 1. Fab 蒙版展开
            if (fab.isExpanded && view_fab_back.visibility == View.VISIBLE) {
                fab.collapse()
                return true
            }

            // 2. Fab 非蒙版展开
            if (fab.isExpanded && view_fab_back.visibility == View.GONE)
                fab.collapse()

            // 3. List 多选
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

        // Data
        refreshAfterUpdate(isSaveData = false)

        // Srl
        view.srl.setColorSchemeResources(R.color.colorAccent)
        view.srl.setOnRefreshListener {
            Handler().postDelayed({
                refreshAfterUpdate(isSaveData = false)
                view.srl.isRefreshing = false
            }, 150)
        }

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
            onCheckedChanged = { isCheckMode, items -> run {
                // 多选可用性

                // 选中单项
                val isCheckSingle = listAdapter != null && listAdapter!!.checkMode && listAdapter!!.getAllChecked().size == 1
                if (isCheckSingle) {
                    view.fab_up.visibility = View.VISIBLE
                    view.fab_down.visibility = View.VISIBLE
                    // 位置
                    val allLength: Int = Global.tabs[tabIdx].tips.size                                  // 列表长度
                    val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked().first())   // 当前位置
                    view.fab_up.isEnabled = pos != 0
                    view.fab_down.isEnabled = pos != allLength - 1
                } else {
                    view.fab_up.visibility = View.GONE
                    view.fab_down.visibility = View.GONE
                }

                // 标题栏
                (activity as? MainActivity)?.run {

                    menu?.findItem(R.id.menu_select_all)?.isVisible = isCheckMode
                    supportActionBar?.setDisplayHomeAsUpEnabled(isCheckMode)
                    title =
                        if (isCheckMode)
                            "已选择 ${items.size} / ${Global.tabs[tabIdx].tips.size} 项"
                        else
                            context?.getString(R.string.act_title)
                }
            }}
        )

        view.list_tipItem.setItemViewCacheSize(0)
        view.list_tipItem.adapter = listAdapter
    }

    /**
     * 初始化 Fab UI / Action
     */
    private fun initFab(view: View) {

        view.fab.collapse()

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
                /////////////////////////////////////////

                // 选中长度
                val selLength: Int = listAdapter!!.getAllChecked().size

                fun isShow(flag: Boolean): Int {
                    return if (flag) View.VISIBLE else View.GONE
                }

                val isCheck = listAdapter!!.checkMode               // 选中模式
                val isCheckSingle = isCheck && selLength == 1       // 选中单项
                // val isCheckZero = isCheck && selLength == 0      // 选中零项
                // val isCheckMulti = isCheck && selLength > 1      // 选中多项

                view.fab_add.visibility = isShow(!isCheck)          // 非选中
                view.fab_exit_check.visibility = isShow(isCheck)    // 选中
                view.fab_more.visibility = isShow(isCheck)          // 选中
                view.fab_up.visibility = isShow(isCheckSingle)      // 选中，单选
                view.fab_down.visibility = isShow(isCheckSingle)    // 选中，单选

                // 位置相关 (单选)

                if (isCheckSingle) {
                    // 列表长度
                    val allLength: Int = Global.tabs[tabIdx].tips.size
                    // 当前位置
                    val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked().first())

                    view.fab_up.isEnabled = pos != 0
                    view.fab_down.isEnabled = pos != allLength - 1
                }
            }
        })

        // Button
        view.fab_add.setOnClickListener { // 新建
            newTips()
            view.fab.collapse()
        }

        view.fab_exit_check.setOnClickListener { // 退出多选
            view.fab.collapse()
            listAdapter?.checkMode = false
        }

        view.fab_up.setOnClickListener { // 上移
            moveUpDownTip(listAdapter?.getAllChecked()?.first(), isMoveUp = true)
        }

        view.fab_down.setOnClickListener { // 下移
            moveUpDownTip(listAdapter?.getAllChecked()?.first(), isMoveUp = false)
        }

        view.fab_more.setOnClickListener {  // 更多
            listAdapter?.let {
                onItemsClick(it.getAllChecked().toList())
            }
            view.fab.collapse()
        }
    }

    /**
     * 修改完数据后更新 适配器 和 存储
     */
    fun refreshAfterUpdate(isSaveData: Boolean = true) {
        view?.let {

            (view!!.list_tipItem.adapter as? TipItemAdapter)?.tipItems = Global.tabs[tabIdx].tips

            // view!!.list_tipItem.adapter?.notifyDataSetChanged()
            view!!.list_tipItem.notifyDataSetChanged()

            // TODO 多线程后台执行
            if (isSaveData)
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
                arrayOf("复制", "编辑", "删除", if (isHighLight) "取消高亮" else "高亮", "分组间移动", "在浏览器打开", "关闭")
            else
                arrayOf("复制", "查看", "删除", if (isHighLight) "取消高亮" else "高亮", "分组间移动", "在浏览器打开", "关闭")

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
                    "分组间移动"       -> moveTipsInGroups(tipItems)
                    "在浏览器打开"     -> openBrowserTips(tipItems)
                    "关闭"            -> dialog.dismiss()
                    else -> {
                        activity?.showToast("${commands[idx]}: $title")
                    }
                }
            }}
        )
    }

    // endregion 界面更新与交互

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Fun: newTips copyTips modifyTip contentTips deleteTips highLightTips moveTipsInGroups moveUpDownTip selectAll openBrowserTips
    // region 记录操作

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

                val trimNewText = newText.trim()

                // 非空
                if (trimNewText.isNotEmpty()) {
                    val tipItem = TipItem(trimNewText)
                    if (Global.tabs[tabIdx].tips.add(tipItem)) {
                        refreshAfterUpdate()
                        view?.list_tipItem?.scrollToPosition(Global.tabs[tabIdx].tips.size - 1)
                        activity?.showToast("已创建 \"${tipItem.content}\"")
                    } else {
                        activity?.showToast("创建失败 \"${tipItem.content}\"")
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
            activity?.showToast("已复制 \"$content\"")
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

                val trimNewText = newText.trim()

                // 非空，变化
                if (trimNewText.isNotEmpty() && preContent != trimNewText) {
                    tipItem.content = trimNewText
                    refreshAfterUpdate()

                    activity?.showSnackBar(
                        message = "已修改 \"$preContent\"",
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
            message = contents.joinToString("\n\n"),
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
            tipIndies.add(Global.tabs[tabIdx].tips.indexOf(it))
            contents.add(it.content)
        }
        val message =
            if (contents.size == 1)
                "确定删除记录 \"${contents.first()}\" 吗？"
            else
                "确定删除以下 ${contents.size} 条记录吗？\n\n${contents.joinToString("\n\n")}"

        val message2 =
            if (contents.size == 1)
                "已删除 \"${contents.first()}\""
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
                        for (idx in 0 until tipItems.size) {
                            Global.tabs[tabIdx].tips.add(tipIndies[idx], tipItems[idx])
                        }
                        refreshAfterUpdate()
                        activity?.showSnackBar(message = "已撤销删除", view = view!!)
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
        val message =
            if (isHighLight) "已高亮 " else "已取消高亮 " +
            if (tipItems.size == 1) "\"${contents.first()}\"" else "${tipItems.size} 条记录"

        activity?.showSnackBar(
            message = message,
            view = view!!,
            action = "撤销",
            listener = View.OnClickListener {
                for (idx in 0 until contents.size) {
                    tipItems[idx].highLight = flags[idx]
                }
                refreshAfterUpdate()
                activity?.showSnackBar(message = "已撤销高亮", view = view!!)
            }
        )
    }

    /**
     * 分组内移动
     */
    private fun moveTipsInGroups(tipItems: List<TipItem>) {
        // 分组标题
        val tabTitles: MutableList<String> = mutableListOf()
        for (idx in 0 until Global.tabs.size)
            tabTitles.add(Global.tabs[idx].title)

        // 原分组内位置记录
        val indexes: MutableList<Int> = mutableListOf()
        tipItems.forEach {
            indexes.add(Global.tabs[tabIdx].tips.indexOf(it))
        }

        activity?.showAlert(
            title = "移动${if (tipItems.size == 1) " \"${tipItems.first().content}\" " else " ${tipItems.size} 项"}至...",
            list = tabTitles.minus(Global.tabs[tabIdx].title).toTypedArray(),
            listener = DialogInterface.OnClickListener { _, idx -> run {
                val fromIdx = tabIdx
                val toIdx = tabTitles.indexOf(tabTitles.minus(Global.tabs[tabIdx].title)[idx])

                for (tipItem in tipItems) {
                    Global.tabs[fromIdx].tips.remove(tipItem)
                    Global.tabs[toIdx].tips.add(tipItem)
                }

                // refreshAfterUpdate()
                (activity as? MainActivity)?.let {
                    it.view_pager.adapter?.notifyDataSetChanged()
                    it.view_pager.currentItem = toIdx

                    it.fragments[toIdx].refreshAfterUpdate()
                    it.fragments[toIdx].list_tipItem?.scrollToPosition(Global.tabs[toIdx].tips.size - 1)
                    it.fragments[fromIdx].refreshAfterUpdate()

                    val message = "已移动 " +
                        if (tipItems.size == 1) "\"${tipItems.first().content}\""
                        else "${tipItems.size} 项"

                    activity?.showSnackBar(
                        message = message,
                        view = it.fragments[toIdx].view!!,
                        action = "撤销",
                        listener = View.OnClickListener { _ -> run {

                            for (i in 0 until tipItems.size) {
                                Global.tabs[toIdx].tips.remove(tipItems[i])
                                Global.tabs[fromIdx].tips.add(indexes[i], tipItems[i])
                            }

                            // refreshAfterUpdate()

                            it.view_pager.adapter?.notifyDataSetChanged()
                            it.view_pager.currentItem = fromIdx

                            it.fragments[fromIdx].refreshAfterUpdate()
                            it.fragments[toIdx].refreshAfterUpdate()

                            activity?.showSnackBar(message = "已撤销移动", view = view!!)
                        }}
                    )
                }
            }}
        )
    }

    /**
     * 上下移
     */
    private fun moveUpDownTip(tipItem: TipItem?, isMoveUp: Boolean) {
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
            val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked().first())

            view?.fab_up?.isEnabled     = pos != 0
            view?.fab_down?.isEnabled   = pos != allSize - 1
        }
    }

    /**
     * 全选 (MainAct Toolbar)
     */
    fun selectAll() {

        if (listAdapter?.checkMode == null || !listAdapter?.checkMode!!) return
        // 多选状态

        if (listAdapter?.getAllChecked()?.size != Global.tabs[tabIdx].tips.size) {
            // 全选
            Global.tabs[tabIdx].tips.forEach {
                listAdapter?.setItemChecked(it, true)
            }
        } else {
            // 全不选
            Global.tabs[tabIdx].tips.forEach {
                listAdapter?.setItemChecked(it, false)
            }
        }
        view?.list_tipItem?.notifyDataSetChanged()
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
                message = "是否打开以下 ${links.size} 个链接：\n\n" + links.joinToString("\n\n"),
                posText = "打开",
                posListener = DialogInterface.OnClickListener { _, _ -> run {
                    activity?.showBrowser(links)
                }},
                negText = "取消"
            )
        }
    }

    // endregion 记录操作
}