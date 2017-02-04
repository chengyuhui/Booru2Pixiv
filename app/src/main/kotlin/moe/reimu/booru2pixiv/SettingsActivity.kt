package moe.reimu.booru2pixiv

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager


class SettingsActivity : PreferenceActivity() {

    companion object {
        var KEY_PREF_API_KEY = "api_key"
        var KEY_PREF_LOGIN_NAME = "login_name"

        private val mValidFragments = listOf(
                PreferenceFragment::class.java,
                GeneralPreferenceFragment::class.java
        )

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val SummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)

                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else {
                preference.summary = stringValue
            }
            true
        }


        private fun bindSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = SummaryToValueListener

            SummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return mValidFragments.any { it.name == fragmentName }
    }


    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            bindSummaryToValue(findPreference(KEY_PREF_API_KEY))
            bindSummaryToValue(findPreference(KEY_PREF_LOGIN_NAME))
        }
    }
}
