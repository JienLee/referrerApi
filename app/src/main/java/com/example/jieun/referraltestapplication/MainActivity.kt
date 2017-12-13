package com.example.jieun.referraltestapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import android.app.Activity
import android.app.AlertDialog
import android.support.v4.app.ActivityCompat
import android.text.Html
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse

class MainActivity : AppCompatActivity() {
    val referrerClient by lazy {
        InstallReferrerClient.newBuilder(this.applicationContext).build()
    }

    var referrerStatus : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkReferrer()
    }

    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        referrerStatus = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (referrerStatus != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(referrerStatus)) {
                googleApiAvailability.getErrorDialog(activity, referrerStatus, 2404).show()
            }
            return false
        }
        return true
    }

    fun checkReferrer() {
        if (isGooglePlayServicesAvailable(this)) {
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    referrerStatus = responseCode

                    when (responseCode) {
                        InstallReferrerResponse.OK -> {
                            // Connection established
                            val response = referrerClient.installReferrer
                            Log.d("DEBUG101", "REFERRER response OK ${response.installReferrer}" +
                                    "\n ${response.referrerClickTimestampSeconds}"+
                                    "\n${response.installBeginTimestampSeconds}")
                        }
                        InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            // API not available on the current Play Store app
                            Log.d("DEBUG101", "REFERRER FEATURE NOT SUPPORTED")

                        }
                        InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            // Connection could not be established
                            Log.d("DEBUG101", "REFERRER SERVICE UNAVAILABLE")

                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.d("DEBUG101", "referrer service disconnected")
                }
            })

        } else {
            Log.d("DEBUG101", "referrer not isGooglePlayServicesAvailable")
            AlertDialog.Builder(this@MainActivity)
                    .setTitle(Html.fromHtml(resources.getString(R.string.noticeTitle)))
                    .setMessage(Html.fromHtml(resources.getString(R.string.noticeMsg,"Google Play 서비스 stat :$referrerStatus")))
                    .setPositiveButton("확인",{dialog, position ->
                        killApp()
                    }).show()
        }

        Log.d("DEBUG101", "referrer play error stat : $referrerStatus")
    }

    override fun onPause() {
        super.onPause()
        if(referrerClient.isReady){
            referrerClient.endConnection()
        }
    }

    fun killApp() {
        ActivityCompat.finishAffinity(this@MainActivity)
        System.runFinalizersOnExit(true)
        System.exit(0)
    }
}