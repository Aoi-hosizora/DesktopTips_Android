package com.aoihosizora.desktoptips.ui.presenter

import com.aoihosizora.desktoptips.ui.contract.MainActivityContract

class MainActivityUpdatePresenter(
    override val view: MainActivityContract.IView
) : MainActivityContract.IUpdatePresenter {

    companion object {
        // const val QR_CODE_MAGIC = "DESKTOP_TIPS_ANDROID://"
    }

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
}