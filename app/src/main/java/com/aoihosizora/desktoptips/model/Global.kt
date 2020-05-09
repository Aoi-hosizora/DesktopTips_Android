package com.aoihosizora.desktoptips.model

import android.content.Context
import android.support.annotation.WorkerThread
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

object Global {

    var tabs: MutableList<Tab> = mutableListOf(Tab("默认"))

    var colors: MutableList<TipColor> = mutableListOf(TipColor(0, "默认"))

    private var currentIndex: Int = 0

    var currentTab: Tab?
        get() = tabs.getOrNull(currentIndex)
        set(value) {
            currentIndex = tabs.indexOf(value)
        }

    private const val FILE_NAME = "model.json"

    @WorkerThread
    fun loadData(context: Context): Boolean {
        val filePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$FILE_NAME"
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
            saveData(context)
            return true
        }

        val buf = file.readBytes()
        val json = String(buf, Charsets.UTF_8).replace("\uFEFF", "")

        val obj: FileModel
        try {
            obj = jacksonObjectMapper().readValue(json)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        tabs = obj.tabs
        colors = obj.colors

        for (t in tabs) {
            if (checkDuplicateTab(t.title, tabs)) {
                return false
            }
        }
        handleWithColorOrder(colors, tabs)

        return true
    }

    @WorkerThread
    fun saveData(context: Context): Boolean {
        val obj = FileModel(tabs, colors)
        val json: String
        try {
            json = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        if (json.isEmpty()) {
            return false
        }

        val buf = json.toByteArray(Charsets.UTF_8)
        val filePath = "${context.getExternalFilesDir(null)!!.absolutePath}/$FILE_NAME"
        val file = File(filePath)
        file.writeBytes(buf)
        return true
    }

    @WorkerThread
    fun checkDuplicateTab(newTitle: String, tabs: List<Tab>, currTab: Tab? = null) =
        tabs.any { it.title == newTitle.trim() && (currTab == null || it.title != currTab.title) }

    @WorkerThread
    fun handleWithColorOrder(colors: List<TipColor>, tabs: List<Tab>) {
        val colorList = colors.sortedBy { it.id }
        for (i in colorList.indices) {
            val color = colorList[i]
            if (color.id == i) {
                continue
            }

            tabs.flatMap { it.tips }.takeWhile { it.colorId == color.id }.forEach { it.colorId = i }
            color.id = i
        }
    }
}
