package com.example.actividadevalua

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class VolleySingleton private constructor(context: Context) {

    val requestQueue: RequestQueue = Volley.newRequestQueue(
        context.applicationContext,
        HurlStack(null, buildTrustAllSslSocketFactory())
    )

    companion object {
        @Volatile private var instance: VolleySingleton? = null

        fun getInstance(context: Context): VolleySingleton =
            instance ?: synchronized(this) {
                instance ?: VolleySingleton(context).also { instance = it }
            }

        private fun buildTrustAllSslSocketFactory(): javax.net.ssl.SSLSocketFactory {
            val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            })
            return SSLContext.getInstance("TLS").apply {
                init(null, trustAll, SecureRandom())
            }.socketFactory
        }
    }

    fun <T> addToRequestQueue(req: Request<T>) = requestQueue.add(req)
}
