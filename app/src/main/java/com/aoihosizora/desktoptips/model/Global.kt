package com.aoihosizora.desktoptips.model

import android.content.Context
import android.support.annotation.WorkerThread
import java.io.File

object Global {

    // private const val TAG = "Global"
    private const val FILE_NAME = "data.json"

    var tabs: MutableList<Tab> = mutableListOf(Tab("默认"))

    @WorkerThread
    fun loadData(context: Context): Boolean {

        val filePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$FILE_NAME"
        val file = File(filePath)

        // 文件不存在 -> 生成默认文件
        if (!file.exists()) {
            file.createNewFile()
            saveData(context)
            return true
        }

        // 文件存在 -> 读取 (UTF-8 Without BOM)
        val buf = file.readBytes()
        val json = String(buf, Charsets.UTF_8).replace("\uFEFF", "")

        val obj = Tab.fromJson(json)
        obj?.let {
            tabs = it
            return@loadData true
        }
        return false
    }

    @WorkerThread
    fun saveData(context: Context): Boolean {
        // 列表 Json
        val json = Tab.toJson(tabs)
        if (json.isEmpty()) return false
        val buf = json.toByteArray(Charsets.UTF_8)

        // 保存进文件
        val filePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$FILE_NAME"
        val file = File(filePath)
        file.writeBytes(buf)
        return true
    }
}
