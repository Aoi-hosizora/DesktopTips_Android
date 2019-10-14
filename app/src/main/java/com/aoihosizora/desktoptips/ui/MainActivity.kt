package com.aoihosizora.desktoptips.ui

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IContextHelper {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initData()
        initUI()
    }

    /**
     * 初始化界面，列表
     */
    private fun initUI() {
        supportActionBar?.let {
            tab_layout.elevation = it.elevation
            supportActionBar?.elevation = 0F
        }
        view_pager.adapter = TabPageAdapter(this, supportFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    /**
     * 获取数据，更新 Global
     */
    private fun initData() {
        // TODO
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
        val edt = EditText(this)
        edt.hint = "新分组标题"
        edt.setSingleLine(true)

        showAlert(
            title = "新分组",
            view = edt,
            posText = "取消",
            negText = "添加",
            negListener = DialogInterface.OnClickListener { _, _ -> run {

                val newTitle = edt.text.toString()
                // 空标题
                if (newTitle.trim().isEmpty()) return@run
                // 重复标题
                if (Global.tabTitles.indexOf(newTitle) != -1) {
                    showAlert(title = "新分组", message = "分组名 \"$newTitle\" 已存在。")
                    return@run
                }
                // 新建分组
                if (Global.tabTitles.add(newTitle)) {
                    view_pager.adapter?.notifyDataSetChanged()
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
        if (Global.tabTitles.size == 1) {
            showAlert(title = "删除分组", message = "无法删除最后一个分组。")
            return
        }

        showAlert(
            title = "删除分组",
            message = "确定删除分组 \"${Global.tabTitles[view_pager.currentItem]}\" ？",
            posText = "删除",
            posListener = DialogInterface.OnClickListener { _, _ -> run {

                Global.tabTitles.removeAt(view_pager.currentItem)
                view_pager.adapter?.notifyDataSetChanged()

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
        val edt = EditText(this)
        edt.setText(Global.tabTitles[view_pager.currentItem])
        edt.setSingleLine(true)

        showAlert(
            title = "重命名分组",
            view = edt,
            posText = "取消",
            negText = "重命名",
            negListener = DialogInterface.OnClickListener { _, _ -> run {

                val newTitle = edt.text.toString()
                // 空标题
                if (newTitle.trim().isEmpty()) return@run

                // 非当前标题 && 重复标题
                if (newTitle != Global.tabTitles[view_pager.currentItem] && Global.tabTitles.indexOf(newTitle) != -1) {
                    showAlert(title = "重命名分组", message = "分组名 \"$newTitle\" 已存在。")
                    return@run
                }
                // 重命名分组
                Global.tabTitles[view_pager.currentItem] = newTitle
                view_pager.adapter?.notifyDataSetChanged()

            }}
        )
    }
}
