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
import com.aoihosizora.desktoptips.ui.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab.*

class MainActivity : AppCompatActivity(), IContextHelper, MainActivityContract.IView {

    override val presenter = MainPresenter(this)
    override val context: Context = this

    companion object {
        val ALL_PERMISSIONS = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )
        const val REQUEST_PERMISSION_CODE = 1
        // const val QR_CODE_MAGIC = "DESKTOP_TIPS_ANDROID://"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        initData() // with initUI
    }

    private fun initData() {
        val progressDlg = showProgress(this, "加载数据中", false)
        presenter.loadData { ok ->
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
        title = "DesktopTips"
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

    fun onPageSelect(position: Int) {
        if (currTabIdx !in fragments.indices) {
            currTabIdx = position
            return
        }

        val lastFrag = fragments[currTabIdx]
        (lastFrag.list_tipItem?.adapter as? TipItemAdapter)?.let {
            it.checkMode = false
        }
        lastFrag.fab?.collapse()
        currTabIdx = position
    }

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
                presenter.addTab(newTitle, onSuccess = {
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
                presenter.deleteTab(index, onSuccess = {
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
                presenter.renameTab(view_pager.currentItem, newTitle, onSuccess = {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // region 同步操作

    // /**
    //  * 更新同步
    //  */
    // private fun updateData() {
    //     showAlert(
    //         title = "请选择同步方式 (同一局域网内)",
    //         list = arrayOf("从桌面版同步", "同步到桌面版", "取消"),
    //         listener = { dialog, idx ->
    //             run {
    //                 when (idx) {
    //                     0 -> { // 从桌面版同步
    //                         updateFromDesktop()
    //                     }
    //                     1 -> { // 同步到桌面版
    //                         updateToDesktop()
    //                     }
    //                     2 -> dialog.dismiss()
    //                 }
    //             }
    //         }
    //     )
    // }
    //
    // /**
    //  * 从桌面版同步 (本机 S <- 桌面 C) !!! 常用
    //  *
    //  * 确定端口 -> 监听本地端口 -> 等待远程发包过来 (S) -> 处理数据 保存更新
    //  */
    // private fun updateFromDesktop() {
    //
    //     val lanIp = SyncData.getLanIp()
    //
    //     if (lanIp.isEmpty()) {
    //         showAlert(title = "错误", message = "本机获取局域网内地址错误")
    //         return
    //     }
    //
    //     // 确定本地端口
    //     showInputDlg(
    //         title = "确定本地监听端口 (本机局域网内地址为 $lanIp)",
    //         text = "8776",
    //         negText = "取消",
    //         posText = "监听",
    //         posClick = { _, _, text ->
    //             run {
    //
    //                 // 端口检查
    //                 val port: Int
    //                 try {
    //                     port = Integer.parseInt(text)
    //                     if (port !in 0..65535)
    //                         throw NumberFormatException()
    //                 } catch (ex: NumberFormatException) {
    //                     ex.printStackTrace()
    //                     showAlert(
    //                         title = "错误",
    //                         message = "输入的端口号 \"$text\" 无效。"
    //                     )
    //                     return@showInputDlg
    //                 }
    //
    //                 var closeFlag = false
    //
    //                 // 加载框
    //                 val progressDlg = showProgress(
    //                     context = this,
    //                     message = "等待接收数据...\n(监听地址为 $lanIp:$port)",
    //                     cancelable = true,
    //                     onCancelListener = {
    //                         closeFlag = true
    //                         it.dismiss()
    //                         SyncData.rcvServerSocket?.run {
    //                             if (!isClosed) close()
    //                         }
    //
    //                         showToast("已取消同步")
    //                     }
    //                 )
    //
    //                 // 新线程接收信息
    //                 Thread(Runnable {
    //                     try {
    //                         // 阻塞
    //                         val json = SyncData.receiveTabs(port)
    //                         if (closeFlag) {                                                                    // <<< 已取消
    //                             runOnUiThread { if (progressDlg.isShowing) progressDlg.dismiss() }
    //                             throw Exception("closeFlag")
    //                         }
    //
    //                         // runOnUiThread { showAlert("", json) }
    //                         // return@Runnable
    //
    //                         if (json.isEmpty()) {                                                               // <<< 数据接收错误
    //                             runOnUiThread { showAlert(title = "错误", message = "数据接收错误。") }
    //                             throw Exception("json.isEmpty")
    //                         }
    //
    //                         // 获得数据
    //                         runOnUiThread { if (progressDlg.isShowing) progressDlg.setMessage("正在保存数据...") }
    //
    //                         // 反序列化
    //                         val rcv = Tab.fromJson(json)
    //                         if (rcv != null) {
    //                             Global.tabs = rcv
    //                             Global.saveData(this@MainActivity)
    //                         } else {                                                                            // <<< 数据无效
    //                             runOnUiThread { showAlert(title = "错误", message = "数据无效。") }
    //                             throw Exception("fromJson")
    //                         }
    //
    //                         // 保存数据
    //                         Global.saveData(this)
    //
    //                         // 返回结果
    //                         runOnUiThread {
    //                             showAlert(title = "同步数据", message = "数据同步完成。\n\n$json")
    //                             initView()
    //                             // view_pager.adapter?.notifyDataSetChanged()
    //                             for (frag in fragments)
    //                                 frag.refreshAfterUpdate(isSaveData = false)
    //                         }
    //                     } catch (ex: Exception) {
    //                         ex.printStackTrace()
    //                     } finally {
    //                         try {
    //                             progressDlg.dismiss()
    //                         } catch (ex: Exception) {
    //                             ex.printStackTrace()
    //                         }
    //                     }
    //
    //                 }).start()
    //             }
    //         }
    //     )
    // }
    //
    // /**
    //  * 同步到桌面版 (本机 C -> 桌面 S) !!! 危险
    //  *
    //  * 扫描二维码 -> 获取远程 IP Port -> 发 Socket Json 包 (C) -> 等 Ack
    //  */
    // private fun updateToDesktop() {
    //
    //     /**
    //      * 检查地址格式
    //      */
    //     fun checkFormat(ip: String, port: String): Boolean {
    //         val ipRe =
    //             "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    //         val portRe =
    //             "^([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$"
    //
    //         return ip.matches(Regex(ipRe)) && port.matches(Regex(portRe))
    //     }
    //
    //     // 二维码扫描得出地址
    //     QRCodeManager.getInstance()
    //         .with(this)
    //         .scanningQRCode(object : OnQRCodeScanCallback {
    //
    //             override fun onCancel() {
    //                 showToast("已取消操作")
    //             }
    //
    //             override fun onError(errorMsg: Throwable?) {
    //                 showToast("二维码读取失败")
    //                 errorMsg?.printStackTrace()
    //             }
    //
    //             override fun onCompleted(result: String?) {
    //
    //                 // 地址
    //                 val ip: String
    //                 val port: Int
    //
    //                 try {
    //                     if (result == null) throw Exception()                       // 数据错误
    //
    //                     if (!result.startsWith(QR_CODE_MAGIC)) throw Exception()    // 无特殊码
    //
    //                     val data = result.substring(QR_CODE_MAGIC.length)
    //                     if (!data.contains(":")) throw Exception()            // 无端口
    //
    //                     val sp = data.split(":")
    //                     if (sp.size != 2) throw Exception()                         // 格式错误
    //                     if (!checkFormat(sp[0], sp[1])) throw Exception()           // 数据错误
    //                     ip = sp[0]
    //                     port = sp[1].toInt()
    //                 } catch (ex: Exception) {
    //                     showToast("二维码无效")
    //                     ex.printStackTrace()
    //                     return
    //                 }
    //
    //                 // 获取地址
    //
    //                 var closeFlag = false
    //
    //                 // 加载框
    //                 val progressDlg = showProgress(
    //                     context = this@MainActivity,
    //                     message = "正在发送数据...",
    //                     cancelable = true,
    //                     onCancelListener = {
    //                         closeFlag = true
    //                         SyncData.sendClientSocket?.run {
    //                             if (!isClosed) close()
    //                         }
    //                         it.dismiss()
    //                         showToast("已取消同步")
    //                     }
    //                 )
    //
    //                 // 获得远程地址，发包
    //                 Thread(Runnable {
    //                     // 阻塞
    //                     val ok = SyncData.sendTabs(ip, port)
    //
    //                     // 获得结果
    //                     runOnUiThread {
    //                         try {
    //                             progressDlg.dismiss()
    //                         } catch (ex: Exception) {
    //                             ex.printStackTrace()
    //                         }
    //
    //                         if (closeFlag) return@runOnUiThread
    //
    //                         if (ok) // 发送成功
    //                             showAlert(title = "同步数据", message = "数据发送完成。")
    //                         else // 发送失败
    //                             showAlert(title = "错误", message = "数据发送失败。")
    //                     }
    //                 }).start()
    //             }
    //         })
    // }
    //
    // /**
    //  * 注册 QRCodeManager
    //  */
    // override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //     super.onActivityResult(requestCode, resultCode, data)
    //     QRCodeManager.getInstance().with(this).onActivityResult(requestCode, resultCode, data)
    // }

    // endregion 同步操作

    /**
     * 检查相机网络权限
     */
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
