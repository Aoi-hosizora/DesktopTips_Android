package com.aoihosizora.desktoptips.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.lang.Exception

data class Tab (

    @JsonProperty(value = "Title", index = 1)
    var title: String,

    @JsonProperty(value = "Tips", index = 2)
    var tips: MutableList<TipItem> = mutableListOf()

) {
    companion object {

        /**
         * 判斷新標題是否在 Global中重複
         */
        fun isDuplicate(checkTitle: String): Boolean {
            for (tab in Global.tabs) {
                if (checkTitle == tab.title)
                    return true
            }
            return false
        }

        /**
         * json -> list
         */
        fun fromJson(json: String): MutableList<Tab>? {
            val obj: MutableList<Tab>
            try {
                obj = jacksonObjectMapper().readValue(json)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            }
            return obj
        }

        /**
         * list -> json
         */
        fun toJson(obj: MutableList<Tab>): String {
            val str: String
            try {
                str = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return ""
            }
            if (str.isEmpty())
                return ""
            return str
        }
    }
}