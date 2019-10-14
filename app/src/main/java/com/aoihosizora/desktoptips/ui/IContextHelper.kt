package com.aoihosizora.desktoptips.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast

/**
 * 显示 toast / alert 辅助类
 */
interface IContextHelper {

    /**
     * LENGTH_SHORT Toast
     */
    fun Context.showToast(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Title + Message + OkPos
     */
    fun Context.showAlert(title: CharSequence, message: CharSequence) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * Title + Message + PosText + PosClick
     */
    fun Context.showAlert(title: CharSequence, message: CharSequence,
                          posText: CharSequence, posListener: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posText, posListener)
            .show()
    }

    /**
     * Title + Message + PosText + PosClick + NegTest + NegClick
     */
    fun Context.showAlert(title: CharSequence, message: CharSequence,
                          posText: CharSequence, posListener: DialogInterface.OnClickListener? = null,
                          negText: CharSequence, negListener: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .show()
    }

    /**
     * Title + View + PosText + PosClick + NegTest + NegClick
     */
    fun Context.showAlert(title: CharSequence, view: View,
                          posText: CharSequence, posListener: DialogInterface.OnClickListener? = null,
                          negText: CharSequence, negListener: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .show()
    }
}