package moe.reimu.booru2pixiv

import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler

interface BaseResolver {
    fun process(pref: SharedPreferences, origUri: Uri, handler: Handler)
}
