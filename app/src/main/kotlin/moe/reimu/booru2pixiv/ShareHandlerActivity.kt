package moe.reimu.booru2pixiv

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog

import moe.reimu.booru2pixiv.resolver.*


class ShareHandlerActivity : Activity() {
    companion object {
        private val TAG = "ShareHandlerActivity"

        var RESOLVE_RESULT = 1
        var RESOLVE_FAIL = 2
    }

    var progress: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val origUri = intent.data
        val ctx = this
        showProgress()

        val handler = Handler(Handler.Callback { msg ->
            dismissProgress()

            if (msg.what == RESOLVE_FAIL) {
                showToast(getString(R.string.failed))
                openLinkBrowser(origUri)
                finish()
                return@Callback false
            }

            val result = msg.obj as Utils.Result

            val items = arrayOf(
                    "${ctx.getString(R.string.orig_uri)}\n($origUri)",
                    "${ctx.getString(result.sourceName)}\n(${result.uri})"
            )

            val dl = MaterialDialog.Builder(ctx)
                    .title(R.string.app_name)
                    .items(*items)
                    .itemsCallback { dialog, itemView, which, text ->
                        when(which) {
                            0 -> openLinkBrowser(origUri)
                            1 -> openLinkApp(result.uri)
                        }
                    }
                    .dismissListener { finish() }
                    .build()

            try {
                dl.show()
            } catch (e: Exception) {
                Log.d(TAG, "Activity closed?")
            }

            false
        })

        when (origUri.host) {
            Danbooru.HOST -> Danbooru().process(sharedPref, origUri, handler)
        }
    }


    private fun openLinkBrowser(url: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.setDataAndType(url, "text/html")
        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE)
        startActivity(browserIntent)
    }

    private fun openLinkApp(url: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url)
        startActivity(browserIntent)
    }

    private fun showToast(text: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show() }
    }

    private fun showProgress() {
        progress = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.resolving)
                .progress(true, 0)
                .cancelListener { finish() }
                .show()
    }

    private fun dismissProgress() {
        this.runOnUiThread { progress?.dismiss() }
    }
}
