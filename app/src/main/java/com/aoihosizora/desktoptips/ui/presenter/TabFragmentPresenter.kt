package com.aoihosizora.desktoptips.ui.presenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.model.TipItem
import com.aoihosizora.desktoptips.ui.contract.TabFragmentContract
import com.aoihosizora.desktoptips.util.swap

class TabFragmentPresenter(
    override val view: TabFragmentContract.IView
) : TabFragmentContract.IPresenter {

    override fun addTipItem(content: String, onSuccess: (String) -> Unit, onFailed: (String) -> Unit) {
        val newContent = content.trim()
        if (newContent.isEmpty()) {
            return
        }

        val tipItem = TipItem(content)
        if (!Global.tabs[view.tabIdx].tips.add(tipItem)) {
            onFailed(content)
            return
        }

        Global.saveData(view.getContext()!!)
        onSuccess(content)
    }

    override fun copyTipItems(tipItems: List<TipItem>, onSuccess: (String) -> Unit) {
        val contents: MutableList<String> = mutableListOf()
        tipItems.forEach {
            contents.add(it.content)
        }
        val content = contents.joinToString(", ")
        val cm = view.getContext()?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val data = ClipData.newPlainText("Label", content)
        cm?.let {
            it.primaryClip = data
            onSuccess(content)
        }
    }

    override fun updateTipItem(tipItem: TipItem, content: String, onSuccess: (String) -> Unit, onFailed: (String) -> Unit) {
        val newContent = content.trim()
        if (newContent.isEmpty()) {
            return
        }

        tipItem.content = newContent
        Global.saveData(view.getContext()!!)
        onSuccess(newContent)
    }

    override fun deleteTipItems(items: List<TipItem>, onSuccess: () -> Unit, onFailed: () -> Unit) {
        if (items.isEmpty()) {
            return
        }

        val tips = Global.tabs[view.tabIdx].tips
        tips.forEach { tips.remove(it) }
        Global.saveData(view.getContext()!!)
        onSuccess()
    }

    override fun moveTipItemUp(tipItem: TipItem, onSuccess: () -> Unit, onFailed: () -> Unit) {
        val tips = Global.tabs[view.tabIdx].tips
        val currIdx = tips.indexOf(tipItem)
        if (currIdx == 0) {
            return
        }

        Global.tabs[view.tabIdx].tips.swap(currIdx, currIdx - 1)
        Global.saveData(view.getContext()!!)
        onSuccess()
    }

    override fun moveTipItemDown(tipItem: TipItem, onSuccess: () -> Unit, onFailed: () -> Unit) {
        val tips = Global.tabs[view.tabIdx].tips
        val currIdx = tips.indexOf(tipItem)
        val len = tips.size
        if (currIdx == len - 1) {
            return
        }

        Global.tabs[view.tabIdx].tips.swap(currIdx, currIdx + 1)
        Global.saveData(view.getContext()!!)
        onSuccess()
    }
}
