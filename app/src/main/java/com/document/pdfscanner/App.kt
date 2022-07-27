package com.document.pdfscanner

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.onesignal.*
import kotlin.properties.Delegates


class App : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        context = this
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        logger = AppEventsLogger.Companion.newLogger(applicationContext)

        val handler: OSInAppMessageLifecycleHandler = object : OSInAppMessageLifecycleHandler() {
            override fun onWillDisplayInAppMessage(message: OSInAppMessage) {
                OneSignal.onesignalLog(
                    OneSignal.LOG_LEVEL.VERBOSE,
                    "MainApplication onWillDisplayInAppMessage"
                )
            }

            override fun onDidDisplayInAppMessage(message: OSInAppMessage) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "MainApplication onDidDisplayInAppMessage")
            }

            override fun onWillDismissInAppMessage(message: OSInAppMessage) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "MainApplication onWillDismissInAppMessage")
            }

            override fun onDidDismissInAppMessage(message: OSInAppMessage) {
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "MainApplication onDidDismissInAppMessage")
            }
        }

        OneSignal.setInAppMessageLifecycleHandler(handler)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.setAppId(applicationContext.getString(R.string.ONESIGNAL_APP_ID))
        OneSignal.initWithContext(this)

        OneSignal.setNotificationOpenedHandler { result: OSNotificationOpenedResult ->
            OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSNotificationOpenedResult result: $result")
        }

        OneSignal.setNotificationWillShowInForegroundHandler { notificationReceivedEvent: OSNotificationReceivedEvent ->
            OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "NotificationWillShowInForegroundHandler fired! with notification event: $notificationReceivedEvent")
            val notification = notificationReceivedEvent.notification
            notification.additionalData
            notificationReceivedEvent.complete(notification)
        }

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true)
        OneSignal.setLocationShared(false)

        val oneSignalUserID = OneSignal.getDeviceState()!!.userId
        Log.e("oneSignalUserID", "oneSignalUserID --------------- $oneSignalUserID")
    }

    companion object {
        private lateinit var mFirebaseAnalytics: FirebaseAnalytics
        private lateinit var logger: AppEventsLogger
        var context by Delegates.notNull<App>()

    }
}