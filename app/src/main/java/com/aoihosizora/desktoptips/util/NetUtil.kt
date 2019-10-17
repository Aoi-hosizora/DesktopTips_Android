package com.aoihosizora.desktoptips.util

import android.support.annotation.WorkerThread

class NetUtil {

    companion object {

        /**
         * 同步本地 (updateFromDesktop)
         *
         * 确定端口 -> 监听本地地址 -> 电脑端发送 -> 本地接受处理
         */
        @WorkerThread
        fun receiveTabs(port: Int): String {
            // TODO
            return ""
        }

        /**
         * 同步远程 (updateToDesktop)
         *
         * 远程监听地址 -> 确定远程地址 -> 本地发送数据 -> 等待 ACK
         */
        @WorkerThread
        fun sendTabs(address: String): Boolean {
            // TODO
            return true
        }
    }
}