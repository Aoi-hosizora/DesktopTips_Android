package com.aoihosizora.desktoptips.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FileModel(
    @JsonProperty(value = "tabs", index = 1)
    var tabs: MutableList<Tab>,

    @JsonProperty(value = "colors", index = 1)
    var colors: MutableList<TipColor>
)
