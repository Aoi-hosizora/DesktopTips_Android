package com.aoihosizora.desktoptips.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.model.Tab
import com.aoihosizora.desktoptips.ui.adapter.TabPageAdapter
import com.aoihosizora.desktoptips.ui.adapter.TipItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import com.aoihosizora.desktoptips.service.SyncData
import com.jwsd.libzxing.OnQRCodeScanCallback
import com.jwsd.libzxing.QRCodeManager
import java.lang.Exception
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity(), IContextHelper, ViewPager.OnPageChangeListener {

    companion object {
        // const val TAG = "MainActivity"

        /**
         * 相机 网络 权限申请返回码
         */
        const val REQUEST_PERMISSION_CODE = 1

        /**
         * 二维码 特殊前缀码
         */
        const val QR_CODE_MAGIC = "DESKTOP_TIPS_ANDROID://"
    }

    // Fun: onCreate onBackPressed initUI initData onCreateOptionsMenu
    // Var: currentFragment fragments menu
    // region 界面初始

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
     * 所有碎片
     */
    val fragments: List<TabFragment>
        get() {
            val ret: MutableList<TabFragment> = mutableListOf()
            for (idx in 0 until Global.tabs.size)
                (view_pager.adapter?.instantiateItem(view_pager, idx) as? TabFragment)?.let {
                    ret.add(it)
                }
            return ret.toList()
        }

    /**
     * 回退
     */
    override fun onBackPressed() {
        val hdl: Boolean? = currentFragment?.onKeyBack()
        if (hdl != null && hdl) return

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

        view_pager.addOnPageChangeListener(this)

        tab_layout.setupWithViewPager(view_pager)
    }

    /**
     * 获取数据，初始化列表
     */
    fun initData() {
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

    // endregion 界面初始

    // Fun: onPageSelected onOptionsItemSelected
    // Var: currTabIdx
    // region 界面交互

    override fun onPageScrollStateChanged(state: Int) { }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }

    /**
     * 当前分组号 (onPageSelected 用)
     */
    private var currTabIdx = -1

    /**
     * TabLayout 当前分组更换 (用 currTabIdx)
     */
    override fun onPageSelected(position: Int) {

        // 未加载的 Tab
        if (currTabIdx !in 0 until fragments.size) {
            currTabIdx = position
            return
        }

        val lastFrag = fragments[currTabIdx]

        // 关闭多选
        (lastFrag.list_tipItem?.adapter as? TipItemAdapter)?.let {
            if (it.checkMode)
                it.checkMode = false
        }

        // 关闭 Fab
        lastFrag.fab?.collapse()

        currTabIdx = position
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
            R.id.menu_update -> updateData()

            else -> showToast(item.title)
        }
        return super.onOptionsItemSelected(item)
    }

    // endregion 界面交互

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Fun: addTab deleteTab renameTab
    // region 分组操作

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

    // endregion 分组操作

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Fun: updateData updateFromDesktop updateToDesktop
    // region 同步操作

    /**
     * 更新同步
     */
    private fun updateData() {
        showAlert(
            title = "请选择同步方式 (同一局域网内)",
            list = arrayOf("从桌面版同步", "同步到桌面版", "取消"),
            listener = DialogInterface.OnClickListener { dialog, idx -> run {
                when (idx) {
                    0 -> { // 从桌面版同步
                        updateFromDesktop()
                    }
                    1 -> { // 同步到桌面版
                        updateToDesktop()
                    }
                    2 -> dialog.dismiss()
                }
            }}
        )
    }

    /**
     * 从桌面版同步 (本机 S <- 桌面 C) !!! 常用
     *
     * 确定端口 -> 监听本地端口 -> 等待远程发包过来 (S) -> 处理数据 保存更新
     */
    private fun updateFromDesktop() {

        // 检查权限
        checkPermission()

        val lanIp = SyncData.getLanIp()

        if (lanIp.isEmpty()) {
            showAlert(title = "错误", message = "本机获取局域网内地址错误")
            return
        }

        // 确定本地端口
        showInputDlg(
            title = "确定本地监听端口 (本机局域网内地址为 $lanIp)",
            text = "8776",
            negText = "取消",
            posText = "监听",
            posClick = { _, _, text -> run {

                // 端口检查
                val port: Int
                try {
                    port = Integer.parseInt(text)
                    if (port !in 0 .. 65535)
                        throw NumberFormatException()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                    showAlert(
                        title = "错误",
                        message = "输入的端口号 \"$text\" 无效。"
                    )
                    return@showInputDlg
                }

                var closeFlag = false

                // 加载框
                val progressDlg = showProgress(
                    context = this,
                    message = "等待接收数据...\n(监听地址为 $lanIp:$port)",
                    cancelable = true,
                    onCancelListener = DialogInterface.OnCancelListener {
                        closeFlag = true
                        it.dismiss()
                        SyncData.rcvServerSocket?.run {
                            if (!isClosed) close()
                        }

                        showToast("已取消同步")
                    }
                )

                // 新线程接收信息
                Thread(Runnable {
                    try {
                        // 阻塞
                        val json = SyncData.receiveTabs(port)
                        if (closeFlag) {                                                                    // <<< 已取消
                            runOnUiThread { if (progressDlg.isShowing) progressDlg.dismiss() }
                            throw Exception("closeFlag")
                        }

                        // runOnUiThread { showAlert("", json) }
                        // return@Runnable

                        if (json.isEmpty()) {                                                               // <<< 数据接收错误
                            runOnUiThread { showAlert(title = "错误", message = "数据接收错误。") }
                            throw Exception("json.isEmpty")
                        }

                        // 获得数据
                        runOnUiThread { if (progressDlg.isShowing) progressDlg.setMessage("正在保存数据...") }

                        // 反序列化
                        val rcv = Tab.fromJson(json)
                        if (rcv != null) {
                            Global.tabs = rcv
                            Global.saveData(this@MainActivity)
                        } else {                                                                            // <<< 数据无效
                            runOnUiThread { showAlert(title = "错误", message = "数据无效。") }
                            throw Exception("fromJson")
                        }

                        // 保存数据
                        Global.saveData(this)

                        // 返回结果
                        runOnUiThread {
                            showAlert(title = "同步数据", message = "数据同步完成。\n\n$json")
                            initUI()
                            // view_pager.adapter?.notifyDataSetChanged()
                            for (frag in fragments)
                                frag.refreshAfterUpdate(isSaveData = false)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
                        try {
                            progressDlg.dismiss()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                }).start()
            }}
        )
    }

    /**
     * 同步到桌面版 (本机 C -> 桌面 S) !!! 危险
     *
     * 扫描二维码 -> 获取远程 IP Port -> 发 Socket Json 包 (C) -> 等 Ack
     */
    private fun updateToDesktop() {

        // 检查权限
        checkPermission()

        /**
         * 检查地址格式
         */
        fun checkFormat(ip: String, port: String): Boolean {
            val ipRe = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
            val portRe = "^([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$"

            return ip.matches(Regex(ipRe)) && port.matches(Regex(portRe))
        }

        // 二维码扫描得出地址
        QRCodeManager.getInstance()
            .with(this)
            .scanningQRCode(object : OnQRCodeScanCallback {

                override fun onCancel() {
                    showToast("已取消操作")
                }

                override fun onError(errorMsg: Throwable?) {
                    showToast("二维码读取失败")
                    errorMsg?.printStackTrace()
                }

                override fun onCompleted(result: String?) {

                    // 地址
                    val ip: String
                    val port: Int

                    try {
                        if (result == null) throw Exception()                       // 数据错误

                        if (!result.startsWith(QR_CODE_MAGIC)) throw Exception()    // 无特殊码

                        val data = result.substring(QR_CODE_MAGIC.length)
                        if (!data.contains(":")) throw Exception()            // 无端口

                        val sp = data.split(":")
                        if (sp.size != 2) throw Exception()                         // 格式错误
                        if (!checkFormat(sp[0], sp[1])) throw Exception()           // 数据错误
                        ip = sp[0]
                        port = sp[1].toInt()
                    } catch (ex: Exception) {
                        showToast("二维码无效")
                        ex.printStackTrace()
                        return
                    }

                    // 获取地址

                    var closeFlag = false

                    // 加载框
                    val progressDlg = showProgress(
                        context = this@MainActivity,
                        message = "正在发送数据...",
                        cancelable = true,
                        onCancelListener = DialogInterface.OnCancelListener {
                            closeFlag = true
                            SyncData.sendClientSocket?.run {
                                if (!isClosed) close()
                            }
                            it.dismiss()
                            showToast("已取消同步")
                        }
                    )

                    // 获得远程地址，发包
                    Thread(Runnable {
                        // 阻塞
                        val ok = SyncData.sendTabs(ip, port)

                        // 获得结果
                        runOnUiThread {
                            try {
                                progressDlg.dismiss()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }

                            if (closeFlag) return@runOnUiThread

                            if (ok) // 发送成功
                                showAlert(title = "同步数据", message = "数据发送完成。")
                            else // 发送失败
                                showAlert(title = "错误", message = "数据发送失败。")
                        }
                    }).start()
                }
            })
    }

    /**
     * 注册 QRCodeManager
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        QRCodeManager.getInstance().with(this).onActivityResult(requestCode, resultCode, data)
    }

    // endregion 同步操作

    // Fun: checkPermission onRequestPermissionsResult
    // region 权限获取

    /**
     * 检查相机网络权限
     */
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions( this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET),
                REQUEST_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    showToast("授权失败")
            }
        }
    }

    // endregion 权限获取
}
