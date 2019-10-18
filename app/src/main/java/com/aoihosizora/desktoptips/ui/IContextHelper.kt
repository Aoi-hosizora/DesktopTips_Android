package com.aoihosizora.desktoptips.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import java.lang.Exception

/**
 * 显示 toast / alert 辅助类
 */
interface IContextHelper {

    companion object {
        // const val TAG: String = "IContextHelper"
    }

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

    /**
     * Title + Text + Hint + MaxLines + PosText + PosClick + NegText + NegClick
     */
    fun Context.showInputDlg(title: CharSequence,
                             text: CharSequence = "", hint: CharSequence = "", maxLines: Int = 5,
                             posText: CharSequence, posClick: (DialogInterface, Int, String) -> Unit,
                             negText: CharSequence, negListener: DialogInterface.OnClickListener? = null) {

        val edt = EditText(this)
        edt.setSingleLine(true)
        edt.maxLines = maxLines
        edt.setHorizontallyScrolling(false)

        edt.hint = hint
        edt.setText(text)

        showAlert(
            title = title,
            view = edt,
            posText= posText,
            posListener = DialogInterface.OnClickListener { dialogInterface, i -> run {
                posClick(dialogInterface, i, edt.text.toString())
            }},
            negText = negText,
            negListener = negListener
        )
    }

    /**
     * Title + Message + List + Listener
     */
    fun Context.showAlert(title: CharSequence,
                          list: Array<out CharSequence>, listener: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(list, listener)
            .show()
    }

    /**
     * Context + Message + Flag + Listener -> ProgressDialog
     */
    fun Context.showProgress(context: Context, message: CharSequence,
                             cancelable: Boolean = true, onCancelListener: DialogInterface.OnCancelListener? = null): ProgressDialog {
        val progressDlg = ProgressDialog(context)
        progressDlg.setMessage(message)
        progressDlg.setCancelable(cancelable)
        progressDlg.setOnCancelListener(onCancelListener)
        progressDlg.show()
        return progressDlg
    }

    /**
     * Message + View
     */
    fun Context.showSnackBar(message: CharSequence, view: View) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Message + View + Action + Listener
     */
    fun Context.showSnackBar(message: CharSequence, view: View,
                             action: CharSequence, listener: View.OnClickListener? = null) {
        Snackbar
            .make(view, message, Snackbar.LENGTH_SHORT)
            .setAction(action, listener)
            .show()
    }

    /**
     * Links
     */
    fun Context.showBrowser(links: Collection<String>) {
        for (link in links) {
            try {
                val uri = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
                continue
            }
        }
    }
}