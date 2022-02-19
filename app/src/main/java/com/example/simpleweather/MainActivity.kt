package com.example.simpleweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import io.github.cdimascio.dotenv.dotenv

class MainActivity() : AppCompatActivity() {
    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }
    private val apiKey = dotenv["APIKEY"]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listenForInput()
    }

     private fun callAPI(): String? {
        var res: String?
        val location = findViewById<EditText>(R.id.location).text.toString()
        try {
            res =
                URL("https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$apiKey").readText(
                    Charsets.UTF_8
                )
            println("sent")
        } catch (e: Exception) {
            res = "Invalid URL"
            println(e)
        }
        return res
    }

     private fun handleWeatherData(){
        GlobalScope.launch {
            //println(callAPI())
            val apiResult = async { callAPI() }
            withContext(Dispatchers.Main) {
                val currentWeatherView = findViewById<TextView>(R.id.currentWeather)
                val currentTempView = findViewById<TextView>(R.id.currentTemp)
                val maxTempView = findViewById<TextView>(R.id.maxTemp)
                val minTempView = findViewById<TextView>(R.id.minTemp)
                try {
                    val jsonObj = JSONObject(apiResult.await())
                    val main = jsonObj.getJSONObject("main")
                    val weatherArray = jsonObj.getJSONArray("weather").getJSONObject(0)
                    val weatherType = weatherArray.getString("main")
                    val currentTemp = main.getString("temp") + "°C"
                    val minTemp = "Min Temp: " + main.getString("temp_min") + "°C"
                    val maxTemp = "Max Temp: " + main.getString("temp_max") + "°C"
                    currentWeatherView.visibility = View.VISIBLE
                    minTempView.visibility = View.VISIBLE
                    maxTempView.visibility = View.VISIBLE
                    currentWeatherView.text = weatherType
                    currentTempView.text = currentTemp
                    maxTempView.text = maxTemp
                    minTempView.text = minTemp
                }catch(err: Exception) {
                    currentWeatherView.visibility = View.INVISIBLE
                    currentTempView.text = "ERROR"
                    minTempView.visibility = View.INVISIBLE
                    maxTempView.visibility = View.INVISIBLE
                 }
            }
            //findViewById<TextView>(R.id.currentWeather).text = weatherType.toString()

        }
    }

    private fun listenForInput() {
        findViewById<EditText>(R.id.location).setOnEditorActionListener { textView, i, keyEvent ->
            return@setOnEditorActionListener when (i) {
                EditorInfo.IME_ACTION_SEND -> {
                    handleWeatherData()
                    true
                }
                else -> false
            }
        }
    }


}



