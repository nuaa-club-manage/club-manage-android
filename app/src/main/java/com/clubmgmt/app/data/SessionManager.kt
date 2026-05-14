package com.clubmgmt.app.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "club_mgmt_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"

    private var prefs: SharedPreferences? = null




    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs?.getString(KEY_TOKEN, null)
        set(value) {
            prefs?.edit()?.putString(KEY_TOKEN, value)?.apply()
        }

    var userId: String?
        get() = prefs?.getString(KEY_USER_ID, null)
        set(value) {
            prefs?.edit()?.putString(KEY_USER_ID, value)?.apply()
        }

    var userRole: String?
        get() = prefs?.getString(KEY_USER_ROLE, null)
        set(value) {
            prefs?.edit()?.putString(KEY_USER_ROLE, value)?.apply()
        }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    fun logout() {
        prefs?.edit()?.clear()?.apply()
    }
}
