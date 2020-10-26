package com.aoihosizora.desktoptips.model

import com.aoihosizora.desktoptips.global.Global
import com.fasterxml.jackson.annotation.JsonProperty

data class TipItem(
    @JsonProperty(value = "content", index = 1)
    var content: String,

    @JsonProperty(value = "color", index = 2)
    var colorId: Int = -1
) {
    val color: TipColor?
        get() = Global.colors.getOrNull(colorId)

    val highLight: Boolean
        get() = colorId == -1 || color == null

    override fun toString() = content
}
