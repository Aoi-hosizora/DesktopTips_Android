package com.aoihosizora.desktoptips.model

import android.graphics.Color
import com.fasterxml.jackson.annotation.JsonProperty

data class TipColor(
    @JsonProperty(value = "id", index = 1)
    var id: Int,

    @JsonProperty(value = "name", index = 2)
    var name: String,

    var color: Int = Color.RED
) {
    constructor(color: TipColor) : this(color.id, color.name, color.color)

    @JsonProperty(value = "color", index = 3)
    var hexColor: String = "#FF0000"
        get() = String.format("#%06X", Integer.valueOf(16777215 and color))
        set(value) {
            field = value
            color = Color.parseColor(value)
        }

    val rdbColor: String
        get() = "${Color.red(color)}, ${Color.green(color)}, ${Color.blue(color)}"

    override fun toString() = name
}
