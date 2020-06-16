package com.aoihosizora.desktoptips.ui.contract

interface TabFragmentContract {

    interface IView {
        val presenter: IPresenter
    }

    interface IPresenter {
        val view: IView
    }
}