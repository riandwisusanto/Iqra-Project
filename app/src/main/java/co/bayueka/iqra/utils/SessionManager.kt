package co.bayueka.iqra.utils

import android.content.Context
import co.bayueka.iqra.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(context: Context) {
    private val TAG = "SessionManager"

    private val rootPackage: String = BuildConfig.APPLICATION_ID
    private val sharedPreferences =
        context.getSharedPreferences("${rootPackage}_setSession", Context.MODE_PRIVATE)

    var spotlightMain: Boolean
        get() = sharedPreferences.getBoolean(rootPackage + "_spotlightMain", true)
        set(data) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(rootPackage + "_spotlightMain", data)
            editor.apply()
        }

    var spotlightHijaiyah: Boolean
        get() = sharedPreferences.getBoolean(rootPackage + "_spotlightHijaiyah", true)
        set(data) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(rootPackage + "_spotlightHijaiyah", data)
            editor.apply()
        }

    var spotLightTest: Boolean
        get() = sharedPreferences.getBoolean(rootPackage + "_spotLightTest", true)
        set(data) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(rootPackage + "_spotLightTest", data)
            editor.apply()
        }

    var spotlightListening: Boolean
        get() = sharedPreferences.getBoolean(rootPackage + "_spotlightListening", true)
        set(data) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(rootPackage + "_spotlightListening", data)
            editor.apply()
        }

    var spotlightSpeaking: Boolean
        get() = sharedPreferences.getBoolean(rootPackage + "_spotlightSpeaking", true)
        set(data) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(rootPackage + "_spotlightSpeaking", data)
            editor.apply()
        }

}