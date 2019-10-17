package com.aoihosizora.desktoptips.ui

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.model.Tab
import com.aoihosizora.desktoptips.ui.adapter.TabPageAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IContextHelper {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initData()
    }

    /**
     * 通过 ViewPager Adapter 获取 当前碎片
     */
    private val currentFragment: TabFragment?
        get() = view_pager.adapter?.instantiateItem(view_pager, view_pager.currentItem) as? TabFragment

    /**
     * 回退
     */
    override fun onBackPressed() {
        val hdl1: Boolean? = currentFragment?.onKeyBack()
        if (hdl1 != null && hdl1) return

        super.onBackPressed()
    }

    /**
     * 初始化界面，显示分栏
     */
    private fun initUI() {
        // TitleBar Shadow
        supportActionBar?.let {
            tab_layout.elevation = it.elevation
            supportActionBar?.elevation = 0F
        }
        title = getString(R.string.act_title)
        tab_layout.visibility = View.VISIBLE

        // Tab Layout
        view_pager.adapter = TabPageAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    /**
     * 获取数据，初始化列表
     */
    private fun initData() {
        val progressDlg = showProgress(this, "加载数据中", false)
        Thread(Runnable {
            val ok = Global.loadData(this)

            runOnUiThread {
                // 加载完数据，初始化界面
                initUI()

                progressDlg.dismiss()
                if (!ok)
                    showAlert(
                        title = "加载数据", message = "数据文件加载错误，请检查文件。",
                        posText = "结束程序", posListener = DialogInterface.OnClickListener { _, _ -> finish() }
                    )
            }
        }).start()
    }

    /**
     * Toolbar 菜单
     */
    var menu: Menu? = null

    /**
     * 创建菜单
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * 菜单点击
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> onBackPressed()
            
            R.id.menu_add -> addTab()
            R.id.menu_delete -> deleteTab()
            R.id.menu_rename -> renameTab()
            R.id.menu_select_all -> currentFragment?.selectAll()
            else -> showToast(item.title)
        }
        return super.onOptionsItemSelected(item)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 新建分组
     */
    private fun addTab() {
        showInputDlg(
            title = "新分组",
            hint = "新分组标题",
            negText = "取消",
            posText= "添加",
            posClick = { _, _, newTitle -> run {

                val trimNewTitle = newTitle.trim()

                // 空标题
                if (trimNewTitle.isEmpty()) return@run

                // 重复标题
                if (Tab.isDuplicate(trimNewTitle)) {
                    showAlert(title = "新分组", message = "分组名 \"$trimNewTitle\" 已存在。")
                    return@run
                }
                // 新建分组
                if (Global.tabs.add(Tab(trimNewTitle))) {
                    view_pager.adapter?.notifyDataSetChanged()
                    Global.saveData(this@MainActivity)

                    view_pager.currentItem = tab_layout.tabCount - 1
                    showToast("分组 \"$trimNewTitle\" 添加成功")
                } else {
                    showToast("分组 \"$trimNewTitle\" 添加失败")
                }
            }}
        )
    }

    /**
     * 删除分组
     */
    private fun deleteTab() {
        // 最后一个标题
        if (Global.tabs.size == 1) {
            showAlert(title = "删除分组", message = "无法删除最后一个分组。")
            return
        }
        val size = Global.tabs[view_pager.currentItem].tips.size
        if (size != 0) {
            showAlert(title = "删除分组", message = "当前分组还有 $size 条记录，请先移动后在删除。")
            return
        }
        showAlert(
            title = "删除分组",
            message = "确定删除分组 \"${Global.tabs[view_pager.currentItem].title}\" ？",
            negText = "取消",
            posText = "删除",
            posListener = DialogInterface.OnClickListener { _, _ -> run {

                Global.tabs.removeAt(view_pager.currentItem)
                view_pager.adapter?.notifyDataSetChanged()
                Global.saveData(this@MainActivity)

                // 删除的标题在最后 -> 前移
                if (view_pager.currentItem == tab_layout.tabCount)
                    view_pager.currentItem--

                showToast("分组 \"${Global.tabs[view_pager.currentItem].title}\" 删除成功")
            }}
        )
    }

    /**
     * 重命名分组
     */
    private fun renameTab() {
        showInputDlg(
            title = "重命名分组",
            text = Global.tabs[view_pager.currentItem].title,
            negText = "取消",
            posText = "重命名",
            posClick = { _, _, newTitle -> run {

                val trimNewTitle = newTitle.trim()

                // 空标题
                if (trimNewTitle.isEmpty()) return@run

                // 非当前标题 && 重复标题
                if (trimNewTitle != Global.tabs[view_pager.currentItem].title
                    && Tab.isDuplicate(trimNewTitle)) {
                    showAlert(title = "重命名分组", message = "分组名 \"$trimNewTitle\" 已存在。")
                    return@run
                }
                // 重命名分组
                Global.tabs[view_pager.currentItem].title = trimNewTitle
                view_pager.adapter?.notifyDataSetChanged()
                Global.saveData(this@MainActivity)
                showToast("成功重命名为 \"${Global.tabs[view_pager.currentItem].title}\"")
            }}
        )
    }
}
