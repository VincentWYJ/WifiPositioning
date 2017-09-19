package com.dylan.wifipositioning

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "WifiPositioning"
    private val WifiEnabledI = 111
    private val WifiEnabledS = "\nwifi is enabled"
    private val WifiNotEnabledI = 112
    private val WifiNotEnabledS = "\nwifi is not enabled"
    private val WifiConnectedI = 113
    private val WifiConnectedS = "\nwifi is connected"
    private val WifiNotConnectedI = 114
    private val WifiNotConnectedS = "\nwifi is not connected"
    private val REQUEST_CODE_ASK_PERMISSIONS = 115
     private val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

    private val mWifiManager by lazy {
        getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val mHandler = object: Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WifiEnabledI ->  wifi_info.text = msg.obj as String
                WifiNotEnabledI -> wifi_info.text = msg.obj as String
                WifiConnectedI -> wifi_info.text = msg.obj as String
                WifiNotConnectedI ->  wifi_info.text = msg.obj as String
            }
        }
    }

    private fun sendMessage(obj: Any, what: Int) {
        val msg = mHandler.obtainMessage()
        msg.what = what
        msg.obj = obj
        mHandler.sendMessage(msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.showLog(TAG, "on create")

        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        get_connected_wifi.setOnClickListener {
            _ -> getConnectedWifiInfo()
        }

        get_all_wifi.setOnClickListener {
            _ -> getAllWifiInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.showLog(TAG, "on resume")

        checkFineLocationPermission()
    }

    private fun getWifiState() {
        Utils.showLog(TAG, "get wifi state")

        when (mWifiManager.isWifiEnabled) {
            true -> when (Utils.isWifiConnected(this)) {
                true -> sendMessage(WifiConnectedS, WifiConnectedI)
                false -> sendMessage(WifiNotConnectedS, WifiNotConnectedI)
            }
            false -> sendMessage(WifiNotEnabledS, WifiNotEnabledI)
        }
    }

    private fun getConnectedWifiInfo() {
        Utils.showLog(TAG, "get connected wifi info")

        if (!Utils.isWifiConnected(this)) {
            sendMessage(WifiNotConnectedS, WifiNotConnectedI)
            return
        }

        val wifiInfo = mWifiManager.connectionInfo
        val wifiString = StringBuilder()
        if (wifiInfo.ssid != null) {
            wifiString.append("\n" + "ssid: " + wifiInfo.ssid.replace("\"", "") + "\n")
            wifiString.append("macAddress: " + wifiInfo.bssid + "\n")
            wifiString.append("ipAddress: " + Utils.transIntToIp(wifiInfo.ipAddress) + "\n")
            wifiString.append("rssi: " + wifiInfo.rssi + "\n")
            wifiString.append("strength: " + WifiManager.calculateSignalLevel(
                    wifiInfo.rssi, 5) + "\n")
            wifiString.append("linkSpeed: " + wifiInfo.linkSpeed + "\n")
            wifiString.append("linkSpeedUnits: " + WifiInfo.LINK_SPEED_UNITS + "\n")

            sendMessage(wifiString.toString(), WifiConnectedI)
        }
    }

    private fun getAllWifiInfo() {
        Utils.showLog(TAG, "get all wifi info")
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (!mWifiManager.isWifiEnabled) {
            sendMessage(WifiNotEnabledS, WifiNotEnabledI)
            return
        }

        val scanResultList = mWifiManager.scanResults as ArrayList<ScanResult>
        val wifiString = StringBuilder()
        if (scanResultList.size > 0) {
            sortByLevel(scanResultList)
            scanResultList.map {
                if (it.SSID != null) {
                    val wifiStringTemp = StringBuilder()
                    wifiStringTemp.append("ssid: " + it.SSID + "\n")
                    wifiStringTemp.append("macAddress: " + it.BSSID + "\n")
                    wifiStringTemp.append("rssi: " + it.level + "\n")

                    wifiString.append("\n" + wifiStringTemp.toString())
                }
            }
        }
        sendMessage(wifiString.toString(), WifiEnabledI)
    }

    private fun sortByLevel(scanResultList: ArrayList<ScanResult>) {
        Collections.sort(scanResultList, { t1, t2 -> t2.level - t1.level})
    }

    private fun checkFineLocationPermission() {
        val hasFineLocationPermission = checkSelfPermission(ACCESS_FINE_LOCATION)
        Utils.showLog(TAG, "" + hasFineLocationPermission)
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            Utils.showLog(TAG, "has not $ACCESS_FINE_LOCATION")

            val sp = getSharedPreferences(ACCESS_FINE_LOCATION, Context.MODE_PRIVATE)
            val access = sp.getBoolean(ACCESS_FINE_LOCATION, false)

            if (access && !shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                showPermissionDialog("You need to allow $ACCESS_FINE_LOCATION",
                        DialogInterface.OnClickListener { dialog, which ->
                            Utils.showInstalledAppDetails(this, packageName)
                        })
                return
            }

            if (!access) {
                val edit = sp.edit()
                edit.putBoolean(ACCESS_FINE_LOCATION, true)
                edit.commit()
            }
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_CODE_ASK_PERMISSIONS)
            return
        } else {
            getWifiState()
        }
    }

    private fun showPermissionDialog(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                getWifiState()
            } else {
                // Permission Denied
                Utils.showLog(TAG, "$ACCESS_FINE_LOCATION access failed.")
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}