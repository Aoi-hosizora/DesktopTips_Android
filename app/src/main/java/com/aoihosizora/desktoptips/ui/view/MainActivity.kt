package com.aoihosizora.desktoptips.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.ui.IContextHelper
import com.aoihosizora.desktoptips.ui.adapter.TabPageAdapter
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
import com.aoihosizora.desktoptips.ui.contract.MainActivityContract
import com.aoihosizora.desktoptips.ui.presenter.MainActivityGroupPresenter
import com.aoihosizora.desktoptips.ui.presenter.MainActivityUpdatePresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*

class MainActivity : AppCompatActivity(), IContextHelper, MainActivityContract.IView {

    override val groupPresenter = MainActivityGroupPresenter(this)
    override val updatePresenter = MainActivityUpdatePresenter(this)
    override val context: Context = this

    companion object {
        val ALL_PERMISSIONS = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )
        const val REQUEST_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        initData() // with initUI
    }

    private fun initData() {
        val progressDlg = showProgress(this, "加载数据中", false)
        groupPresenter.loadData { ok ->
            progressDlg.dismiss()
            if (ok) {
                initView()
            } else {
                showAlert(
                    title = "加载数据",
                    message = "数据文件加载错误，请检查文件。",
                    posText = "结束程序",
                    posListener = { _, _ -> finish() }
                )
            }
        }
    }

    private fun initView() {
        title = getString(R.string.act_title)
        supportActionBar?.let {
            tab_layout.elevation = it.elevation
            supportActionBar?.elevation = 0F
        }

        view_pager.adapter = TabPageAdapter(supportFragmentManager)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) = onPageSelect(position)
        })

        tab_layout.visibility = View.VISIBLE
        tab_layout.setupWithViewPager(view_pager)
    }

    var menu: Menu? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    val fragments: List<TabFragment>
        get() {
            val ret = mutableListOf<TabFragment>()
            for (idx in 0 until Global.tabs.size) {
                (view_pager.adapter?.instantiateItem(view_pager, idx) as? TabFragment)?.let {
                    ret.add(it)
                }
            }
            return ret.toList()
        }

    private val currentFragment: TabFragment?
        get() = view_pager.adapter?.instantiateItem(
            view_pager,
            view_pager.currentItem
        ) as? TabFragment

    /**
     * 退出多选模式或者 finish()
     */
    override fun onBackPressed() {
        val hdl: Boolean? = currentFragment?.onKeyBack()
        if (hdl != null && hdl) {
            return
        }
        super.onBackPressed()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private var currTabIdx = -1

    /**
     * TabView 修改 Page
     */
    fun onPageSelect(position: Int) {
        if (currTabIdx !in fragments.indices) {
            currTabIdx = position
            return
        }

        val lastFrag = fragments[currTabIdx]
        (lastFrag.list_view?.adapter as? TipItemAdapter)?.let {
            it.checkMode = false
        }
        lastFrag.fab?.collapse()
        currTabIdx = position
    }

    /**
     * 菜单选择
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_add -> addTab()
            R.id.menu_delete -> deleteTab()
            R.id.menu_rename -> renameTab()
            R.id.menu_select_all -> currentFragment?.selectAll()
            // R.id.menu_update -> updateData()

            else -> showToast("不支持的选项: ${item.title}")
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
            posText = "添加",
            posClick = { _, _, newTitle ->
                groupPresenter.addTab(newTitle, onSuccess = {
                    view_pager.adapter?.notifyDataSetChanged()
                    view_pager.currentItem = tab_layout.tabCount - 1
                    showToast("分组 $it 添加成功")
                }, onFailed = {
                    showToast("分组 $it 添加失败")
                }, onDuplicated = {
                    showAlert(title = "新分组", message = "分组名 $it 已存在。")
                })
            }
        )
    }

    /**
     * 删除分组
     */
    private fun deleteTab() {
        if (Global.tabs.size == 1) {
            showAlert(title = "删除分组", message = "无法删除最后一个分组。")
            return
        }
        val index = view_pager.currentItem
        showAlert(
            title = "删除分组",
            message = "确定删除分组 ${Global.tabs[index].title}？",
            negText = "取消",
            posText = "删除",
            posListener = { _, _ ->
                groupPresenter.deleteTab(index, onSuccess = {
                    view_pager.adapter?.notifyDataSetChanged()
                    showToast("分组 ${Global.tabs[index].title} 删除成功")
                    if (view_pager.currentItem == tab_layout.tabCount) {
                        view_pager.currentItem--
                    }
                }, onFailed = {
                    showToast("分组 ${Global.tabs[index].title} 删除失败")
                }, onExistContent = { title, size ->
                    showAlert(
                        title = "删除分组",
                        message = "分组 $title 还有 $size 条记录，请先移动后再删除。"
                    )
                })
            }
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
            posClick = { _, _, newTitle ->
                groupPresenter.renameTab(view_pager.currentItem, newTitle, onSuccess = {
                    showAlert(title = "重命名分组", message = "成功重命名为 $it 。")
                    view_pager.adapter?.notifyDataSetChanged()
                }, onFailed = {
                    showAlert(title = "重命名分组", message = "重命名分组名失败。")
                }, onDuplicated = {
                    showAlert(title = "重命名分组", message = "分组名 $it 已存在。")
                })
            }
        )
    }

    private fun checkPermission() {
        val requiredPermissions: List<String> = ALL_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (requiredPermissions.isNotEmpty()) {
            showAlert(
                title = "权限", message = "本应用需要读取本地内存，网络连接以及相机权限用于扫描二维码，请授权。",
                posText = "确定", posListener = { _, _ ->
                    ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), REQUEST_PERMISSION_CODE)
                }, negText = "取消", negListener = { _, _ -> finish() }
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showAlert(
                    title = "授权", message = "授权失败。",
                    posText = "退出", posListener = { _, _ -> finish() }
                )
            } else {
                showToast("授权成功。")
            }
        }
    }
}
