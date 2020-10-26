package com.aoihosizora.desktoptips.ui.contract

import android.content.Context

interface MainActivityContract {

    interface IView {
        val groupPresenter: IGroupPresenter
        val networkPresenter: INetworkPresenter
        val context: Context

        fun runOnUiThread(action: Runnable)
    }

    interface IGroupPresenter {
        val view: IView

        /**
         * 加载数据进 Global
         */
        fun loadData(cb: (Boolean) -> Unit)

        /**
         * 添加新分组
         */
        fun addTab(
            title: String,
            onSuccess: (String) -> Unit,
            onDuplicated: (String) -> Unit,
            onFailed: (String) -> Unit
        )

        /**
         * 删除分组
         */
        fun deleteTab(
            index: Int,
            onSuccess: () -> Unit,
            onFailed: () -> Unit,
            onExisted: (String, Int) -> Unit
        )

        /**
         * 重命名分组
         */
        fun renameTab(
            index: Int,
            title: String,
            onSuccess: (String) -> Unit,
            onFailed: (String) -> Unit,
            onDuplicated: (String) -> Unit
        )
    }

    interface INetworkPresenter {
        val view: IView
    }
}
