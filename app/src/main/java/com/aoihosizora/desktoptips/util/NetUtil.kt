package com.aoihosizora.desktoptips.util

import android.support.annotation.WorkerThread
import com.aoihosizora.desktoptips.model.Global
import com.aoihosizora.desktoptips.model.Tab
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

class NetUtil {

    companion object {

        /**
         * 同步本地 (updateFromDesktop)
         *
         * 确定端口 -> 监听本地地址 -> 电脑端发送 -> 本地接受处理
         */
        @WorkerThread
        fun receiveTabs(port: Int): String {
            try {
                val server = ServerSocket(port, 1)
                while (true) {
                    val client: Socket = server.accept()

                    val input = BufferedReader(InputStreamReader(client.getInputStream()))
                    val json: String = input.readText()
                    input.close()
                    client.close()

                    return json
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return ""
        }

        /**
         * 同步远程 (updateToDesktop)
         *
         * 远程监听地址 -> 确定远程地址 -> 本地发送数据 -> 等待 ACK
         */
        @WorkerThread
        fun sendTabs(ip: String, port: Int): Boolean {
            try {
                val socket = Socket(ip, port)
                val writer = PrintWriter(socket.getOutputStream(), true)

                val json = Tab.toJson(Global.tabs)
                if (json.isNotEmpty()) {
                    writer.println(json)
                    socket.close()
                    return true
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return false
        }
    }
}