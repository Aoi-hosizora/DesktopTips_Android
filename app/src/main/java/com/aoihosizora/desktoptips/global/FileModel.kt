package com.aoihosizora.desktoptips.global

import android.graphics.Color
import com.aoihosizora.desktoptips.model.Tab
import com.aoihosizora.desktoptips.model.TipColor
import com.aoihosizora.desktoptips.model.TipItem
import com.fasterxml.jackson.annotation.JsonProperty

data class FileModel(
    @JsonProperty(value = "colors", index = 1)
    var colors: MutableList<TipColor> = mutableListOf(),

    @JsonProperty(value = "tabs", index = 2)
    var tabs: MutableList<Tab> = mutableListOf()
) {
    companion object {
        fun getDefaultModel(): FileModel {
            val colorList = mutableListOf(
                TipColor(0, "红色", Color.RED),
                TipColor(1, "蓝色", Color.BLUE)
            )
            val itemList = mutableListOf(
                TipItem("实例普通标签"),
                TipItem("实例红色标签", 0),
                TipItem("实例蓝色标签", 1)
            )
            val tabList = mutableListOf(Tab("默认", itemList))
            return FileModel(colors = colorList, tabs = tabList)
        }
    }
}
