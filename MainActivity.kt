package com.raydej.nba

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val navy = Color.rgb(11, 18, 32)
    private val panel = Color.rgb(25, 39, 61)
    private val blue = Color.rgb(56, 189, 248)
    private val white = Color.WHITE
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private lateinit var refresh: Button
    private val refreshTask = object : Runnable {
        override fun run() { loadScores(); handler.postDelayed(this, 30_000) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy
        buildUi()
        loadScores()
        handler.postDelayed(refreshTask, 30_000)
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(navy) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(24))
        }
        val brand = TextView(this).apply {
            text = "RAYDEJ NBA"
            textSize = 30f
            setTextColor(blue)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        }
        page.addView(brand)
        page.addView(TextView(this).apply {
            text = "BASKETBALL CENTRAL  •  LIVE SCORES"
            textSize = 12f
            setTextColor(white)
            setPadding(0, dp(4), 0, dp(16))
            letterSpacing = .08f
        })
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        status = TextView(this).apply {
            text = "Loading NBA scores…"
            textSize = 14f
            setTextColor(blue)
        }
        refresh = Button(this).apply {
            text = "REFRESH"
            setTextColor(navy)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue))
            setOnClickListener { loadScores() }
        }
        top.addView(status, LinearLayout.LayoutParams(0, dp(48), 1f))
        top.addView(refresh)
        page.addView(top)
        page.addView(TextView(this).apply {
            text = "TODAY'S GAMES"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            setPadding(0, dp(12), 0, dp(12))
        })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(list)
        page.addView(TextView(this).apply {
            text = "Scores provided by ESPN's public scoreboard feed. Updates automatically every 30 seconds."
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(20), 0, 0)
        })
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun loadScores() {
        status.text = "Updating scores…"
        refresh.isEnabled = false
        executor.execute {
            try {
                val url = URL("https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", "RayDejNBA/1.0")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val events = JSONObject(body).optJSONArray("events")
                runOnUiThread {
                    list.removeAllViews()
                    if (events == null || events.length() == 0) {
                        status.text = "No games listed today"
                    } else {
                        for (i in 0 until events.length()) {
                            val event = events.getJSONObject(i)
                            val comp = event.optJSONArray("competitions")?.optJSONObject(0) ?: continue
                            val competitors = comp.optJSONArray("competitors") ?: continue
                            if (competitors.length() < 2) continue
                            val home = (0 until competitors.length()).map { competitors.getJSONObject(it) }
                                .firstOrNull { it.optString("homeAway") == "home" } ?: competitors.getJSONObject(0)
                            val away = (0 until competitors.length()).map { competitors.getJSONObject(it) }
                                .firstOrNull { it.optString("homeAway") == "away" } ?: competitors.getJSONObject(1)
                            val state = comp.optJSONObject("status")?.optJSONObject("type")
                            val stateText = state?.optString("shortDetail")
                                ?: state?.optString("description") ?: "Scheduled"
                            val live = state?.optString("state") == "in"
                            addGameCard(away, home, stateText, live)
                        }
                        status.text = "Updated ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())}"
                    }
                    refresh.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "Couldn't load scores. Check your internet and tap refresh."
                    refresh.isEnabled = true
                    if (list.childCount == 0) {
                        list.addView(TextView(this).apply {
                            text = "NBA scores need an internet connection. Pull up this screen and tap REFRESH to try again."
                            setTextColor(white)
                            textSize = 14f
                            setPadding(dp(14), dp(18), dp(14), dp(18))
                        })
                    }
                }
            }
        }
    }

    private fun addGameCard(away: JSONObject, home: JSONObject, detail: String, live: Boolean) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(panel)
        }
        val state = TextView(this).apply {
            text = if (live) "● LIVE  •  $detail" else detail.uppercase(Locale.getDefault())
            textSize = 11f
            setTextColor(if (live) Color.rgb(74, 222, 128) else blue)
            typeface = Typeface.DEFAULT_BOLD
        }
        card.addView(state)
        fun teamLine(team: JSONObject): TextView {
            val teamObj = team.optJSONObject("team")
            val name = teamObj?.optString("displayName", "Team") ?: "Team"
            val abbr = teamObj?.optString("abbreviation", "") ?: ""
            val score = team.optString("score", "—").ifBlank { "—" }
            return TextView(this).apply {
                text = "$abbr   $name                         $score"
                textSize = 16f
                setTextColor(white)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setPadding(0, dp(9), 0, dp(3))
            }
        }
        card.addView(teamLine(away))
        card.addView(teamLine(home))
        val params = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        list.addView(card, params)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        handler.removeCallbacks(refreshTask)
        executor.shutdownNow()
        super.onDestroy()
    }
}
