package moe.reimu.booru2pixiv.resolver

import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.support.annotation.StringRes
import android.util.Log

import com.google.gson.Gson
import moe.reimu.booru2pixiv.*

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class Danbooru : BaseResolver {
    companion object {
        private val TAG = "Danbooru"
        val HOST = "danbooru.donmai.us"
    }

    override fun process(pref: SharedPreferences, origUri: Uri, handler: Handler) {
        val cb = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.sendFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body().string()
                var srcUri: Uri? = null
                @StringRes var srcType = 0

                try {
                    val post = Gson().fromJson(jsonData, Post::class.java)

                    if (post.pixiv_id != null) {
                        // Detect bad_id (deleted post)
                        if (!post.tag_string.contains("bad_id")) {
                            srcUri = Utils.pixivUri(post.pixiv_id)
                            srcType = R.string.pixiv
                        }
                    } else if (post.source != null && post.source.isNotBlank()) {
                        srcUri = Uri.parse(post.source)
                        srcType = R.string.source
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "JSON parsing failed", e)
                }

                if (srcUri == null) {
                    handler.sendFailed()
                } else {
                    handler.sendSuccess(Utils.Result(srcType, srcUri))
                }
            }
        }

        Utils.simpleRequest(cleanupUri(origUri).buildUpon()
                .appendQueryParameter("login",
                        pref.getString(SettingsActivity.KEY_PREF_LOGIN_NAME, ""))
                .appendQueryParameter("api_key",
                        pref.getString(SettingsActivity.KEY_PREF_API_KEY, ""))
                .build(), cb)
    }

    private fun cleanupUri(uri: Uri): Uri {
        return Uri.parse(uri.clearQuery().toString() + ".json")
    }

    private data class Post(val pixiv_id: Int?, val source: String?, val tag_string: String)
}
