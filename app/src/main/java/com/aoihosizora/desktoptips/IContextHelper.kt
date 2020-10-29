package com.aoihosizora.desktoptips

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

interface IContextHelper {

    fun Context.showToast(
        message: String
    ) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.showAlert(
        title: String, message: String, cancelable: Boolean = true
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton("OK", null)
            .show()
    }

    fun Context.showAlert(
        title: String, message: String, cancelable: Boolean = true,
        posText: String, posListener: ((DialogInterface, Int) -> Unit)? = null
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(posText, posListener)
            .show()
    }

    fun Context.showAlert(
        title: String, message: String, cancelable: Boolean = true,
        posText: String, posListener: ((DialogInterface, Int) -> Unit)? = null,
        negText: String, negListener: ((DialogInterface, Int) -> Unit)? = null
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .show()
    }

    fun Context.showAlert(
        title: String, message: String, cancelable: Boolean = true,
        posText: String, posListener: ((DialogInterface, Int) -> Unit)? = null,
        negText: String, negListener: ((DialogInterface, Int) -> Unit)? = null,
        neuText: String, neuListener: ((DialogInterface, Int) -> Unit)? = null
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(posText, posListener)
            .setNegativeButton(negText, negListener)
            .setNeutralButton(neuText, neuListener)
            .show()
    }

    fun Context.showAlert(
        title: String, cancelable: Boolean = true,
        list: Array<String>,
        listener: ((DialogInterface, Int) -> Unit)? = null
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setCancelable(cancelable)
            .setItems(list, listener)
            .show()
    }
}
