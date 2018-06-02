package com.guardanis.netclient.sample

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.guardanis.netclient.ApiRequest
import com.guardanis.netclient.WebRequest
import com.guardanis.netclient.sample.models.JsonPlaceholderPost
import org.json.JSONArray
import org.json.JSONObject

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
    }

    fun testGetClicked(view: View?) {
        ApiRequest<List<JsonPlaceholderPost>>(this, WebRequest.ConnectionType.GET, "posts")
                .setCacheDurationMs(1)
                .setResponseParser({
                    Log.d("NetClient", "GET Response: ${it.unparsedResponse}")

                    val unparsed = JSONArray(it.unparsedResponse)

                    return@setResponseParser 0.until(unparsed.length())
                            .map({ unparsed.getJSONObject(it) })
                            .map({ JsonPlaceholderPost(it) })
                })
                .onSuccess({
                    AlertDialog.Builder(this)
                            .setMessage("First title: ${it.first().title}")
                            .show()
                })
                .onFail({
                    AlertDialog.Builder(this)
                            .setMessage(it.errors.joinToString("\n"))
                            .show()
                })
                .execute()
    }

    fun testPostClicked(view: View?) {
        val data = JSONObject()
                .put("user_id", "1")
                .put("title", "This is a new title!")
                .put("body", "This is a new body....")

        ApiRequest<JsonPlaceholderPost>(this, WebRequest.ConnectionType.POST, "posts")
                .setData(data)
                .setResponseParser({
                    Log.d("NetClient", "POST Response: ${it.unparsedResponse}")

                    return@setResponseParser JsonPlaceholderPost(it.responseJson)
                })
                .onSuccess({
                    AlertDialog.Builder(this)
                            .setMessage("Created: ${it.title}")
                            .show()
                })
                .onFail({
                    AlertDialog.Builder(this)
                            .setMessage(it.errors.joinToString("\n"))
                            .show()
                })
                .execute()
    }
}
