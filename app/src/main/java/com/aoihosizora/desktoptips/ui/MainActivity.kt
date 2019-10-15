package com.aoihosizora.desktoptips.ui

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
     * 初始化界面，显示分栏
     */
    private fun initUI() {
        supportActionBar?.let {
            tab_layout.elevation = it.elevation
            supportActionBar?.elevation = 0F
        }

        tab_layout.visibility = View.VISIBLE

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
     * 创建菜单
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * 菜单点击
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_add -> addTab()
            R.id.menu_delete -> deleteTab()
            R.id.menu_rename -> renameTab()
            else -> showToast(item.title)
        }
        return super.onOptionsItemSelected(item)
    }

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

                // 空标题
                if (newTitle.trim().isEmpty()) return@run

                // 重复标题
                if (Tab.isDuplicate(newTitle)) {
                    showAlert(title = "新分组", message = "分组名 \"$newTitle\" 已存在。")
                    return@run
                }
                // 新建分组
                if (Global.tabs.add(Tab(newTitle))) {
                    view_pager.adapter?.notifyDataSetChanged()
                    Global.saveData(this@MainActivity)

                    view_pager.currentItem = tab_layout.tabCount - 1
                    showToast("分组 $newTitle 添加成功")
                } else {
                    showToast("分组 $newTitle 添加失败")
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

        showAlert(
            title = "删除分组",
            message = "确定删除分组 \"${Global.tabs[view_pager.currentItem].title}\" ？",
            posText = "删除",
            posListener = DialogInterface.OnClickListener { _, _ -> run {

                Global.tabs.removeAt(view_pager.currentItem)
                view_pager.adapter?.notifyDataSetChanged()
                Global.saveData(this@MainActivity)

                // 删除的标题在最后 -> 前移
                if (view_pager.currentItem == tab_layout.tabCount)
                    view_pager.currentItem--

            }},
            negText = "取消"
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

                // 空标题
                if (newTitle.trim().isEmpty()) return@run

                // 非当前标题 && 重复标题
                if (newTitle != Global.tabs[view_pager.currentItem].title
                    && Tab.isDuplicate(newTitle)) {
                    showAlert(title = "重命名分组", message = "分组名 \"$newTitle\" 已存在。")
                    return@run
                }
                // 重命名分组
                Global.tabs[view_pager.currentItem].title = newTitle
                view_pager.adapter?.notifyDataSetChanged()
                Global.saveData(this@MainActivity)

            }}
        )
    }
}
