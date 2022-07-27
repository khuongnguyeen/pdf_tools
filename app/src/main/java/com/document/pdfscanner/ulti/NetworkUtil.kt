package com.document.pdfscanner.ulti

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


class NetworkUtil {


    interface Consumer {
        fun accept(internet: Boolean?)
    }
     class InternetCheck(private val mConsumer: Consumer) :
        AsyncTask<Void, Void, Boolean>() {

        init {
            execute()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            try {
                var a = System.currentTimeMillis()

                if (InetAddress.getByName("www.google.com").isReachable(5000))
                {
                    Log.d("CheckTimeOut :", (System.currentTimeMillis()-a).toString())
                    return true }
                else
                {   return false }

//                val sock = Socket()
//                sock.connect(InetSocketAddress("https://www.google.com/", 53), 1500)
//                sock.close()
//                NetworkUtil().isInternetAvailable()
            } catch (e: IOException) {
                return false
            }

        }

        override fun onPostExecute(internet: Boolean?) {
            mConsumer.accept(internet)
        }
    }


//    class CheckNetwork(var context: Context): AsyncTask<Void, Void, Boolean>() {
//        override fun doInBackground(vararg params: Void?): Boolean {
//            return NetworkUtil().isNetworkConnectionAvailable(context)
//        }
//
//        override fun onPostExecute(result: Boolean?) {
//            super.onPostExecute(result)
//        }
//    }
    public fun isNetworkConnectionAvailable(context: Context): Boolean {
        if (isConnectionOn(context) && isInternetAvailable()) {
            return true
        } else {
            return false
        }
    }

    private fun isConnectionOn(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val connection = connectivityManager.getNetworkCapabilities(network)
            return connection != null && (
                    connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            connection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            if (activeNetwork != null) {
                return (activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
                        activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
            }
            return false
        }
    }

     private fun isInternetAvailable(): Boolean {
        return try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockaddr = InetSocketAddress("8.8.8.8", 53)

            sock.connect(sockaddr, timeoutMs)
            sock.close()

            true
        } catch (e: IOException) {
            false
        }

    }

    class NoConnectivityException : IOException() {
        override val message: String
            get() = "No network available, please check your WiFi or Data connection"
    }

    class NoInternetException() : IOException() {
        override val message: String
            get() = "No internet available, please check your connected WIFi or Data"
    }

//    class NoConnectionInterceptor @Inject constructor(private val context: Context) : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            return if (!isConnectionOn()) {
//                throw NoConnectivityException()
//            } else if(!isInternetAvailable()) {
//                throw NoInternetException()
//            } else {
//                chain.proceed(chain.request())
//            }
//        }
}