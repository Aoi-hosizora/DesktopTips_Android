package com.aoihosizora.desktoptips.ui.presenter

import com.aoihosizora.desktoptips.global.Global
import com.aoihosizora.desktoptips.model.Tab
import com.aoihosizora.desktoptips.ui.contract.MainActivityContract

class MainActivityGroupPresenter(
    override val view: MainActivityContract.IView
) : MainActivityContract.IGroupPresenter {

    override fun loadData(cb: (Boolean) -> Unit) {
        val ok = Global.loadData(view.context)
        view.runOnUiThread(Runnable {
            cb(ok)
        })
    }

    override fun addTab(title: String, onSuccess: (String) -> Unit, onDuplicated: (String) -> Unit, onFailed: (String) -> Unit) {
        val newTitle = title.trim()
        if (newTitle.isEmpty()) {
            return
        }
        if (Global.checkDuplicateTab(newTitle, Global.tabs)) {
            onDuplicated(newTitle)
            return
        }
        if (!Global.tabs.add(Tab(newTitle))) {
            onFailed(newTitle)
            return
        }

        Global.saveData(view.context)
        onSuccess(newTitle)
    }

    override fun deleteTab(index: Int, onSuccess: () -> Unit, onFailed: () -> Unit, onExistContent: (String, Int) -> Unit) {
        if (Global.tabs.size <= index) {
            onFailed()
            return
        }
        val size = Global.tabs[index].tips.size
        if (size != 0) {
            onExistContent(Global.tabs[index].title, size)
            return
        }

        Global.tabs.removeAt(index)
        onSuccess()
    }

    override fun renameTab(index: Int, title: String, onSuccess: (String) -> Unit, onFailed: (String) -> Unit, onDuplicated: (String) -> Unit) {
        val newTitle = title.trim()
        if (newTitle.isEmpty() || Global.tabs[index].title == newTitle) {
            return
        }

        if (Global.checkDuplicateTab(newTitle, Global.tabs, Global.tabs[index])) {
            onDuplicated(newTitle)
            return
        }

        Global.tabs[index].title = newTitle
        Global.saveData(view.context)
        onSuccess(newTitle)
    }
}
