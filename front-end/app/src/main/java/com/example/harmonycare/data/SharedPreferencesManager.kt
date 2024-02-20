package com.example.harmonycare.data

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {

    private const val PREFS_NAME = "MyPrefs"
    private const val KEY_AUCCESSTOKEN = "auccesstoken"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(accessToken: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_AUCCESSTOKEN, accessToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_AUCCESSTOKEN, null)
    }
}
