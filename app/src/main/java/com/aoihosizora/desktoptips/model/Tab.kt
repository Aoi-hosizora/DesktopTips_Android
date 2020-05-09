package com.aoihosizora.desktoptips.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Tab(
    @JsonProperty(value = "title", index = 1)
    var title: String,

    @JsonProperty(value = "tips", index = 2)
    var tips: MutableList<TipItem> = mutableListOf()
) {
    override fun toString() = title
}
