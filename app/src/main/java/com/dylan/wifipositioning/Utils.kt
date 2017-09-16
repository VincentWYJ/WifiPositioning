package com.dylan.wifipositioning

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast


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
}
