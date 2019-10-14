package com.aoihosizora.desktoptips.model

import android.content.Context
import android.support.annotation.WorkerThread
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.lang.Exception

object Global {

    private const val FILE_NAME = "data.json"

    val tabTitles: MutableList<String> = mutableListOf("Test1", "Test2", "Test3", "Test4")

    var tabs: MutableList<Tab> = mutableListOf(
        Tab("默认")
    )

    @WorkerThread
    fun loadData(context: Context) {
        val inf = File("${context.filesDir.absolutePath}/$FILE_NAME")
        if (!inf.exists()) {
            saveData(context)
            return
        }

        val fis = context.openFileInput(FILE_NAME)
        val buf = ByteArray(fis.available())
        fis.read(buf)
        val json = String(buf, Charsets.UTF_8)
        try {
            tabs = jacksonObjectMapper().readValue(json)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    @WorkerThread
    fun saveData(context: Context) {
        val json = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(tabs)
        val buf = json.toByteArray(Charsets.UTF_8)
        val fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
        fos.write(buf)
    }

}