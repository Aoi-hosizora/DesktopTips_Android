package com.aoihosizora.desktoptips.global

import android.content.Context
import android.support.annotation.WorkerThread
import com.aoihosizora.desktoptips.model.Tab
import com.aoihosizora.desktoptips.model.TipColor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

object Global {

    /**
     * Global tabs
     */
    var tabs: MutableList<Tab> = mutableListOf(Tab("默认"))

    /**
     * Global colors
     */
    var colors: MutableList<TipColor> = mutableListOf(TipColor(0, "默认"))

    /**
     * filename
     */
    private const val FILE_NAME = "model.json"

    /**
     * Load data
     */
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

        val obj: FileModel = try {
            jacksonObjectMapper().readValue(json)
        } catch (ex: Exception) {
            ex.printStackTrace()
            FileModel.getDefaultModel()
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

    /**
     * save data
     */
    @WorkerThread
    fun saveData(context: Context): Boolean {
        val obj = FileModel(tabs = tabs, colors = colors)
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

    /**
     * check duplicate tab
     */
    fun checkDuplicateTab(newTitle: String, tabs: List<Tab>, currTab: Tab? = null): Boolean {
        return tabs.any {
            it.title == newTitle.trim() && (currTab == null || it.title != currTab.title)
        }
    }

    /**
     * check tipColor order
     */
    private fun handleWithColorOrder(colors: List<TipColor>, tabs: List<Tab>) {
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
