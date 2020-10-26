package com.aoihosizora.desktoptips.service

import android.support.annotation.WorkerThread
import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.model.Tab
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class SyncData {

    // companion object {
    //
    //     /**
    //      * 监听本地的服务器 Socket, 接收数据
    //      */
    //     var rcvServerSocket: ServerSocket? = null
    //
    //     /**
    //      * 同步本地 (updateFromDesktop) (本机 S <- 桌面 C)
    //      *
    //      * 确定端口 -> 监听本地地址 -> 电脑端发送 -> 本地接受处理
    //      */
    //     @WorkerThread
    //     fun receiveTabs(port: Int): String {
    //         rcvServerSocket?.let {
    //             if (!it.isClosed) it.close()
    //         }
    //         rcvServerSocket = null
    //         try {
    //             rcvServerSocket = ServerSocket(port, 1)
    //             if (rcvServerSocket == null) return ""
    //
    //             while (true) {
    //                 val client: Socket = rcvServerSocket!!.accept()
    //
    //                 val input = BufferedReader(InputStreamReader(client.getInputStream()))
    //                 val json: String = input.readText()
    //                 input.close()
    //                 client.close()
    //                 rcvServerSocket!!.close()
    //
    //                 return json
    //             }
    //         } catch (ex: Exception) {
    //             ex.printStackTrace()
    //         } finally {
    //             rcvServerSocket?.let {
    //                 if (!it.isClosed) it.close()
    //             }
    //         }
    //         return ""
    //     }
    //
    //     /**
    //      * 连接远程的客户端 Socket, 发送数据
    //      */
    //     var sendClientSocket: Socket? = null
    //
    //     /**
    //      * 同步远程 (updateToDesktop) (本机 C -> 桌面 S)
    //      *
    //      * 远程监听地址 -> 确定远程地址 -> 本地发送数据 -> 等待 ACK
    //      */
    //     @WorkerThread
    //     fun sendTabs(ip: String, port: Int): Boolean {
    //         sendClientSocket?.let {
    //             if (it.isClosed)
    //                 it.close()
    //         }
    //         sendClientSocket = null
    //         try {
    //             sendClientSocket = Socket(ip, port)
    //             if (sendClientSocket == null) return false
    //
    //             val writer = PrintWriter(sendClientSocket!!.getOutputStream(), true)
    //
    //             val json = Tab.toJson(Global.tabs)
    //             if (json.isNotEmpty()) {
    //                 writer.println(json)
    //                 sendClientSocket!!.close()
    //                 return true
    //             }
    //         } catch (ex: Exception) {
    //             ex.printStackTrace()
    //         } finally {
    //             sendClientSocket?.let {
    //                 if (!it.isClosed) it.close()
    //             }
    //         }
    //         return false
    //     }
    //
    //     /**
    //      * 获得局域网内地址
    //      */
    //     fun getLanIp(): String {
    //         val iNetIfs: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
    //         for (iNetIf in iNetIfs) {
    //             for (ipAddress in iNetIf.inetAddresses) {
    //                 if (!ipAddress.isLoopbackAddress && (ipAddress is Inet4Address)) {
    //                     return ipAddress.hostAddress.toString()
    //                 }
    //             }
    //         }
    //         return ""
    //     }
    // }
}
