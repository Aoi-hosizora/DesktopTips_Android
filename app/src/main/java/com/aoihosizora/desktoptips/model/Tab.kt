package com.aoihosizora.desktoptips.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Tab (

    @JsonProperty(value = "Title", index = 1)
    var title: String,

    @JsonProperty(value = "Tips", index = 2)
    var tips: MutableList<TipItem> = mutableListOf()
)