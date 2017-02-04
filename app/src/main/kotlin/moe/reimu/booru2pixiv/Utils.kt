package moe.reimu.booru2pixiv

import android.net.Uri
import android.os.Handler
import android.os.Message
import android.support.annotation.StringRes

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

fun Uri.clearQuery(): Uri {
    return this.buildUpon().clearQuery().build()
}

fun Handler.sendFailed() {
    val msg = Message.obtain()
    msg.what = ShareHandlerActivity.RESOLVE_FAIL
    this.sendMessage(msg)
}

fun Handler.sendSuccess(result: Utils.Result) {
    val msg = Message.obtain()
    msg.what = ShareHandlerActivity.RESOLVE_RESULT
    msg.obj = result
    this.sendMessage(msg)
}

object Utils {

    fun simpleRequest(uri: Uri, callback: Callback) {
        val client = OkHttpClient()
        val request = Request.Builder().url(uri.toString()).build()

        client.newCall(request).enqueue(callback)
    }


    fun pixivUri(id: Int): Uri {
        return Uri.parse("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + id.toString())
    }

    data class Result(@StringRes var sourceName: Int, var uri: Uri)
}
