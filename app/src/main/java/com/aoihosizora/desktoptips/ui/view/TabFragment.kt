package com.aoihosizora.desktoptips.ui.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.model.TipItem
import com.aoihosizora.desktoptips.ui.IContextHelper
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
import com.aoihosizora.desktoptips.ui.contract.TabFragmentContract
import com.aoihosizora.desktoptips.ui.presenter.TabFragmentPresenter
import com.getbase.floatingactionbutton.FloatingActionsMenu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*
import kotlinx.android.synthetic.main.fragment_tab.view.*

class TabFragment : Fragment(), IContextHelper, TabFragmentContract.IView {

    override val presenter = TabFragmentPresenter(this)

    companion object {
        const val BUNDLE_TAB_INDEX = "BUNDLE_TAB_INDEX"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)

        initView(view)
        initListener(view)
        onRefreshList()

        return view
    }

    /**
     * 加载界面
     */
    private fun initView(view: View) {
        // srl
        view.srl.setColorSchemeResources(R.color.colorAccent)
        view.srl.setOnRefreshListener {
            onRefreshList()
            view.srl.isRefreshing = false
        }

        // fab
        view.view_fab_mask.setOnClickListener { view.fab.collapse() }
        view.fab.collapse()
        view.fab.setOnFloatingActionsMenuUpdateListener(object : FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
            override fun onMenuCollapsed() {
                view.view_fab_mask.visibility = View.GONE
            }

            override fun onMenuExpanded() {
                if (!listAdapter!!.checkMode) {
                    view.view_fab_mask.visibility = View.VISIBLE // 多选模式不屏蔽蒙版
                }
                onFabMenuExpanded()
            }
        })

        // list
        view.list_view.setEmptyView(view.view_empty)
        view.list_view.layoutManager = LinearLayoutManager(activity)
        view.list_view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        view.list_view.setItemViewCacheSize(0)
        view.list_view.adapter = TipItemAdapter(
            context = context!!,
            tipItems = Global.tabs[tabIdx].tips,
            onItemClick = { _, tipItem -> onItemsClick(listOf(tipItem)) },
            onItemLongClick = { _, tipItem -> onItemLongClick(tipItem) },
            onCheckedChanged = { isCheckMode, items -> onCheckedChanged(isCheckMode, items) }
        )
    }

    /**
     * 加载 listener
     */
    private fun initListener(view: View) {
        // 新建
        view.fab_add.setOnClickListener {
            view.fab.collapse()
            newTips()
        }
        // 退出多选
        view.fab_exit_check.setOnClickListener {
            view.fab.collapse()
            listAdapter?.checkMode = false
        }
        // 上移
        view.fab_up.setOnClickListener {
            moveUpDownTip(listAdapter?.getAllChecked()?.first(), isMoveUp = true)
        }
        // 下移
        view.fab_down.setOnClickListener {
            moveUpDownTip(listAdapter?.getAllChecked()?.first(), isMoveUp = false)
        }
        // 更多
        view.fab_more.setOnClickListener {
            view.fab.collapse()
            listAdapter?.let {
                onItemsClick(it.getAllChecked().toList())
            }
        }
    }

    private fun onFabMenuExpanded() {
        if (listAdapter == null) {
            return
        }

        val selLength: Int = listAdapter!!.getAllChecked().size
        val isCheck = listAdapter!!.checkMode         // 选中模式
        val isCheckSingle = isCheck && selLength == 1 // 选中单项
        fun isShow(flag: Boolean): Int = if (flag) View.VISIBLE else View.GONE

        view?.run {
            fab_add.visibility = isShow(!isCheck)       // 非选中
            fab_exit_check.visibility = isShow(isCheck) // 选中
            fab_more.visibility = isShow(isCheck)       // 选中
            fab_up.visibility = isShow(isCheckSingle)   // 选中，单选
            fab_down.visibility = isShow(isCheckSingle) // 选中，单选

            // 位置相关 (单选)
            if (isCheckSingle) {
                val allLength: Int = Global.tabs[tabIdx].tips.size
                val pos: Int = Global.tabs[tabIdx].tips.indexOf(listAdapter!!.getAllChecked().first())

                fab_up.isEnabled = pos != 0
                fab_down.isEnabled = pos != allLength - 1
            }
        }
    }

    private val listAdapter: TipItemAdapter?
        get() = view?.list_view?.adapter as? TipItemAdapter

    override val tabIdx: Int by lazy {
        arguments!!.getInt(BUNDLE_TAB_INDEX, -1)
    }

    /**
     * 返回键，MainAct 委托
     */
    fun onKeyBack(): Boolean {
        view?.run {
            // 1. Fab 蒙版展开
            if (fab.isExpanded && view_fab_mask.visibility == View.VISIBLE) {
                fab.collapse()
                return true
            }

            // 2. Fab 非蒙版展开
            if (fab.isExpanded && view_fab_mask.visibility == View.GONE) {
                fab.collapse()
            }

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

    private fun onRefreshList() {
        listAdapter?.tipItems = Global.tabs[tabIdx].tips
        listAdapter?.notifyDataSetChanged()
        list_view.notifyDataSetChanged()
    }

    private fun onItemLongClick(tipItem: TipItem) {
        listAdapter?.checkMode = true
        listAdapter?.setItemChecked(tipItem, true)
        view?.fab?.expand()
    }

    private fun onCheckedChanged(isCheckMode: Boolean, items: List<TipItem>) {
        val tips = Global.tabs[tabIdx].tips
        listAdapter?.let {
            // 选中单项
            if (it.checkMode && it.getAllChecked().size == 1) {
                val allLength = tips.size // 列表长度
                val currPos = tips.indexOf(listAdapter!!.getAllChecked().first()) // 当前位置
                view?.fab_up?.isEnabled = currPos != 0
                view?.fab_down?.isEnabled = currPos != allLength - 1
            } else {
                view?.fab_up?.visibility = View.GONE
                view?.fab_down?.visibility = View.GONE
            }

            // 标题栏
            (activity as? MainActivity)?.run {
                menu?.findItem(R.id.menu_select_all)?.isVisible = isCheckMode
                supportActionBar?.setDisplayHomeAsUpEnabled(isCheckMode)
                title = if (isCheckMode)
                    "已选择 ${items.size} / ${Global.tabs[tabIdx].tips.size} 项"
                else
                    context.getString(R.string.act_title)
            }
        }
    }

    private fun onItemsClick(tipItems: List<TipItem>) {
        // var isHighLight = false
        val contents: MutableList<String> = mutableListOf()
        // tipItems.forEach {
        //     contents.add(it.content)
        //     if (it.highLight) {
        //         isHighLight = true
        //     }
        // }

        val commands = if (tipItems.size == 1)
            arrayOf(
                "复制", "编辑", "删除",
                // if (isHighLight) "取消高亮" else "高亮",
                "分组间移动", "在浏览器打开", "关闭"
            )
        else
            arrayOf(
                "复制", "查看", "删除",
                // if (isHighLight) "取消高亮" else "高亮",
                "分组间移动", "在浏览器打开", "关闭"
            )

        val title = if (contents.size == 1)
            contents.first()
        else
            "共 ${contents.size} 项：${contents.joinToString(", ")}"

        activity?.showAlert(
            title = title,
            list = commands,
            listener = { dialog, idx ->
                when (commands[idx]) {
                    "复制" -> copyTips(tipItems)
                    "编辑" -> modifyTip(tipItems.first())
                    "查看" -> contentTips(tipItems)
                    "删除" -> deleteTips(tipItems)
                    // "高亮" -> highLightTips(tipItems, true)
                    // "取消高亮" -> highLightTips(tipItems, false)
                    "分组间移动" -> moveTipsInGroups(tipItems)
                    "在浏览器打开" -> openBrowserTips(tipItems)
                    "关闭" -> dialog.dismiss()
                    else -> activity?.showToast("不支持的命令 ${commands[idx]}")
                }
            }
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 新建
     */
    private fun newTips() {
        activity?.showInputDialog(
            title = "新记录",
            hint = "新记录内容",
            negText = "取消",
            posText = "添加",
            posClick = { _, _, newText ->
                presenter.addTipItem(newText,
                    onSuccess = {
                        onRefreshList()
                        view?.list_view?.scrollToPosition(Global.tabs[tabIdx].tips.size - 1)
                        activity?.showToast("已创建 $it")
                    }, onFailed = {
                        activity?.showToast("创建失败 $it")
                    }
                )
            }
        )
    }

    /**
     * 复制
     */
    private fun copyTips(tipItems: List<TipItem>) {
        presenter.copyTipItems(tipItems, onSuccess = {
            activity?.showToast("已复制 $it")
        })
    }

    /**
     * 修改
     */
    private fun modifyTip(tipItem: TipItem) {
        val preContent = tipItem.content
        activity?.showInputDialog(
            title = "编辑记录",
            text = tipItem.content,
            negText = "取消",
            posText = "修改",
            posClick = { _, _, newText ->
                presenter.updateTipItem(
                    tipItem, newText,
                    onSuccess = {
                        onRefreshList()
                        activity?.showSnackBar(
                            message = "已修改 $preContent",
                            view = view!!,
                            action = "撤销",
                            listener = {
                                presenter.updateTipItem(tipItem, preContent, onSuccess = {
                                    onRefreshList()
                                    activity?.showSnackBar(message = "已撤销修改", view = view!!)
                                }, onFailed = {
                                    activity?.showSnackBar(message = "撤销修改错误", view = view!!)
                                })
                            }
                        )
                    }, onFailed = {
                        activity?.showToast("修改标签为 $it 错误")
                    }
                )
            }
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
            posText = "复制",
            posListener = { _, _ -> copyTips(tipItems) },
            negText = "返回"
        )
    }

    /**
     * 删除
     */
    private fun deleteTips(tipItems: List<TipItem>) {
        val tipIndies: MutableList<Int> = mutableListOf() // 初始位置
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            tipIndies.add(Global.tabs[tabIdx].tips.indexOf(it))
            contents.add(it.content)
        }

        val message = if (contents.size == 1)
            "确定删除记录 ${contents.first()} 吗？"
        else
            "确定删除以下 ${contents.size} 条记录吗？\n\n${contents.joinToString("\n\n")}"

        activity?.showAlert(
            title = "删除记录",
            message = message,
            negText = "取消",
            posText = "删除",
            posListener = { _, _ ->
                presenter.deleteTipItems(
                    tipItems,
                    onSuccess = {
                        listAdapter?.checkMode = false
                        onRefreshList()
                        activity?.showToast(
                            if (contents.size == 1) "已删除 ${contents.first()}"
                            else "已删除 ${contents.size} 条记录"
                        )
                    }, onFailed = {
                        listAdapter?.checkMode = false
                        activity?.showToast("删除标签错误")
                    }
                )
            }
        )
    }

    // /**
    //  * 高亮
    //  */
    // private fun highLightTips(tipItems: List<TipItem>, isHighLight: Boolean) {
    //     // 初始值
    //     val flags: MutableList<Boolean> = mutableListOf()
    //     val contents: MutableList<String> = mutableListOf()
    //     tipItems.forEach {
    //         contents.add(it.content)
    //         flags.add(it.highLight)
    //     }
    //     tipItems.forEach { it.highLight = isHighLight }
    //     refreshAfterUpdate()
    //     val message =
    //         if (isHighLight) "已高亮 " else "已取消高亮 " +
    //                 if (tipItems.size == 1) "\"${contents.first()}\"" else "${tipItems.size} 条记录"
    //
    //     activity?.showSnackBar(
    //         message = message,
    //         view = view!!,
    //         action = "撤销",
    //         listener = {
    //             for (idx in 0 until contents.size) {
    //                 tipItems[idx].highLight = flags[idx]
    //             }
    //             refreshAfterUpdate()
    //             activity?.showSnackBar(message = "已撤销高亮", view = view!!)
    //         }
    //     )
    // }

    /**
     * 上下移
     */
    private fun moveUpDownTip(tipItem: TipItem?, isMoveUp: Boolean) {
        val tips = Global.tabs[tabIdx].tips
        val update: () -> Unit = {
            onRefreshList()
            val allSize: Int = tips.size
            val pos: Int = tips.indexOf(listAdapter!!.getAllChecked().first())
            view?.fab_up?.isEnabled = pos != 0
            view?.fab_down?.isEnabled = pos != allSize - 1
        }
        tipItem?.let {
            if (isMoveUp) {
                presenter.moveTipItemUp(
                    tipItem,
                    onSuccess = { update() }, onFailed = {
                        activity?.showToast("标签上移错误")
                    }
                )
            } else {
                presenter.moveTipItemDown(
                    tipItem,
                    onSuccess = { update() }, onFailed = {
                        activity?.showToast("标签下移错误")
                    }
                )
            }
        }
    }

    /**
     * 全选 (MainAct Toolbar)
     */
    fun selectAll() {
        // 多选状态
        val tips = Global.tabs[tabIdx].tips
        if (listAdapter?.checkMode != true) {
            return
        }

        if (listAdapter?.getAllChecked()?.size != tips.size) {
            tips.forEach { // 全选
                listAdapter?.setItemChecked(it, true)
            }
        } else {
            tips.forEach { // 全不选
                listAdapter?.setItemChecked(it, false)
            }
        }
        view?.list_view?.notifyDataSetChanged()
    }

    /**
     * 打开浏览器
     */
    private fun openBrowserTips(tipItems: List<TipItem>) {
        val sp = tipItems.flatMap { it.content.split(" ") }
        val links = sp.filter { it.startsWith("http://") || it.startsWith("https://") }
        if (links.isEmpty()) {
            activity?.showAlert(title = "用浏览器打开", message = "当前项中不包含任何链接。")
        } else {
            activity?.showAlert(
                title = "用浏览器打开",
                message = "是否打开以下 ${links.size} 个链接：\n\n" + links.joinToString("\n\n"),
                posText = "打开",
                posListener = { _, _ -> activity?.openBrowser(links) },
                negText = "取消"
            )
        }
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
            listener = { _, idx ->
                val fromIdx = tabIdx
                val toIdx = tabTitles.indexOf(tabTitles.minus(Global.tabs[tabIdx].title)[idx])

                for (tipItem in tipItems) {
                    Global.tabs[fromIdx].tips.remove(tipItem)
                    Global.tabs[toIdx].tips.add(tipItem)
                }
                listAdapter?.checkMode = false

                // refreshAfterUpdate()
                (activity as? MainActivity)?.let {
                    it.view_pager.adapter?.notifyDataSetChanged()
                    it.view_pager.currentItem = toIdx

                    it.fragments[toIdx].onRefreshList()
                    Global.saveData(activity!!)
                    it.fragments[toIdx].list_view?.scrollToPosition(Global.tabs[toIdx].tips.size - 1)
                    it.fragments[fromIdx].onRefreshList()
                    Global.saveData(activity!!)

                    val message = "已移动 " +
                            if (tipItems.size == 1) "\"${tipItems.first().content}\""
                            else "${tipItems.size} 项"

                    activity?.showSnackBar(
                        message = message,
                        view = it.fragments[toIdx].view!!,
                        action = "撤销",
                        listener = { _ ->
                            for (i in tipItems.indices) {
                                Global.tabs[toIdx].tips.remove(tipItems[i])
                                try {
                                    Global.tabs[fromIdx].tips.add(indexes[i], tipItems[i])
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                    Global.tabs[fromIdx].tips.add(tipItems[i])
                                }
                            }

                            it.view_pager.adapter?.notifyDataSetChanged()
                            it.view_pager.currentItem = fromIdx

                            it.fragments[fromIdx].onRefreshList()
                            Global.saveData(activity!!)
                            it.fragments[toIdx].onRefreshList()
                            Global.saveData(activity!!)

                            activity?.showSnackBar(message = "已撤销移动", view = view!!)
                        }
                    )
                }
            }
        )
    }
}
