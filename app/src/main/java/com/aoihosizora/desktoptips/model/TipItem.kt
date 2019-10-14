package com.aoihosizora.desktoptips.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TipItem (

    @JsonProperty(value = "Content", index = 1)
    var content: String,

    @JsonProperty(value = "IsHighLight", index = 1)
    var isHighLight: Boolean = false
)