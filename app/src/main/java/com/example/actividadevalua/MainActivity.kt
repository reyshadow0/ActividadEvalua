package com.example.actividadevalua

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var listViewNoticias: ListView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listViewNoticias = findViewById(R.id.listViewNoticias)
        progressBar      = findViewById(R.id.progressBar)

        fetchNoticias()
    }

    private fun fetchNoticias() {
        progressBar.visibility      = View.VISIBLE
        listViewNoticias.visibility = View.GONE

        val request = object : StringRequest(
            Method.GET,
            ApiConstants.BASE_URL,
            { response ->
                Log.d("API_RESPONSE", response)
                try {
                    val trimmed = response.trim()
                    val jsonArray = if (trimmed.startsWith("[")) {
                        JSONArray(trimmed)
                    } else {
                        val obj = JSONObject(trimmed)
                        when {
                            obj.has("data") -> obj.getJSONArray("data")
                            else -> obj.getJSONArray(obj.keys().next())
                        }
                    }
                    parseAndDisplay(jsonArray)
                } catch (e: Exception) {
                    Log.e("API_PARSE", "Parse error", e)
                    showError(e.message)
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val body = error.networkResponse?.data?.let { String(it) } ?: ""
                val cause = error.cause?.toString() ?: "null"
                Log.e("API_ERROR", "Type: ${error.javaClass.simpleName} | HTTP $statusCode | cause: $cause | body: $body")
                showError("HTTP $statusCode (${error.javaClass.simpleName}): $cause")
            }
        ) {
            override fun getHeaders(): Map<String, String> =
                mapOf("Authorization" to "Bearer ${ApiConstants.TOKEN}")

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                val parsed = String(response.data, Charsets.UTF_8)
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun parseAndDisplay(jsonArray: JSONArray) {
        val inFmt  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val noticias = (0 until jsonArray.length()).mapNotNull { i ->
            try {
                val obj = jsonArray.getJSONObject(i)
                val rawFecha = obj.optString("ntFecha", "")
                val fechaDisplay = try {
                    outFmt.format(inFmt.parse(rawFecha)!!) + "|" + rawFecha
                } catch (e: Exception) { rawFecha }

                val cat = obj.optJSONObject("objCategoriaNotc")
                NoticiaModel(
                    ntTitulo          = obj.optString("ntTitular", ""),
                    ntUrlNoticia      = obj.optString("ntUrlNoticia", ""),
                    ntUrlPortada      = obj.optString("ntUrlPortada", ""),
                    ntFechaPublicacion = fechaDisplay,
                    ntCategoria       = cat?.optString("gtTitular", "") ?: "",
                    ntCategoriaColor  = cat?.optString("gtColorIdentf", "#2196F3") ?: "#2196F3"
                )
            } catch (e: Exception) { null }
        }

        val top10 = noticias
            .sortedByDescending { n ->
                if (n.ntFechaPublicacion.contains("|")) n.ntFechaPublicacion.split("|")[1]
                else n.ntFechaPublicacion
            }
            .take(10)

        listViewNoticias.adapter    = NoticiaAdapter(this, top10)
        progressBar.visibility      = View.GONE
        listViewNoticias.visibility = View.VISIBLE
    }

    private fun showError(message: String?) {
        progressBar.visibility      = View.GONE
        listViewNoticias.visibility = View.VISIBLE
        Toast.makeText(this, "${getString(R.string.error_loading)}: $message", Toast.LENGTH_LONG).show()
    }
}
