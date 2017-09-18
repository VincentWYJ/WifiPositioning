package com.dylan.wifipositioning

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.os.Build
import android.provider.Settings


object Utils {
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    fun transIntToIp(i: Int): String {
        return (i shr 24 and 0xFF).toString() + "." + (i shr 16 and 0xFF) +
                "." + (i shr 8 and 0xFF) + "." + (i and 0xFF)
    }

    fun transIpToInt(ip: String): Int {
        val ipArray = ip.split(".")
        return (ipArray[0].toInt() shl 24) + (ipArray[1].toInt() shl 16) +
                (ipArray[2].toInt() shl 8) + (ipArray[3].toInt())
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun showLog(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    private val SCHEME = "package"
    private val APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName"
    private val APP_PKG_NAME_22 = "pkg"
    private val APP_DETAILS_PACKAGE_NAME = "com.android.settings"
    private val APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails"
    fun showInstalledAppDetails(context: Context, packageName: String) {
        val intent = Intent()
        val apiLevel = Build.VERSION.SDK_INT
        if (apiLevel >= 9) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts(SCHEME, packageName, null)
            intent.data = uri
        } else {
            val appPkgName = if (apiLevel == 8) APP_PKG_NAME_22 else APP_PKG_NAME_21
            intent.action = Intent.ACTION_VIEW
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME)
            intent.putExtra(appPkgName, packageName)
        }
        context.applicationContext.startActivity(intent)
    }
}
