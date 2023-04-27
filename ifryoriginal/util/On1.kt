package com.example.ifryoriginal.util

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class On1(private val ipAddress: String) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String? {
        return try {
            val url = URL("http://$ipAddress/led1on")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            val inputStream: InputStream = httpURLConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response: String? = bufferedReader.readLine()
            response
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Do something with the response, if needed
    }
}
