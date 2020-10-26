package com.aoihosizora.desktoptips.ui.contract

import android.content.Context
import com.aoihosizora.desktoptips.model.TipItem

interface TabFragmentContract {

    interface IView {
        val presenter: IPresenter
        val tabIdx: Int
        fun getContext(): Context?
    }

    interface IPresenter {
        val view: IView

        /**
         * 插入
         */
        fun addTipItem(
            content: String,
            onSuccess: (String) -> Unit,
            onFailed: (String) -> Unit
        )

        /**
         * 复制内容
         */
        fun copyTipItems(
            tipItems: List<TipItem>,
            onSuccess: (String) -> Unit
        )

        /**
         * 修改内容
         */
        fun updateTipItem(
            tipItem: TipItem,
            content: String,
            onSuccess: (String) -> Unit,
            onFailed: (String) -> Unit
        )

        /**
         * 删除标签
         */
        fun deleteTipItems(
            items: List<TipItem>,
            onSuccess: () -> Unit,
            onFailed: () -> Unit
        )

        /**
         * 上移
         */
        fun moveTipItemUp(
            tipItem: TipItem,
            onSuccess: () -> Unit,
            onFailed: () -> Unit
        )

        /**
         * 下移
         */
        fun moveTipItemDown(
            tipItem: TipItem,
            onSuccess: () -> Unit,
            onFailed: () -> Unit
        )
    }
}
