package com.guardanis.netclient.sample.models

import org.json.JSONObject

class JsonPlaceholderPost(obj: JSONObject) {

    val id: String
    val title: String

    init {
        this.id = obj.getString("id")
        this.title = obj.getString("title")
    }
}