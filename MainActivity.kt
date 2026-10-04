package com.example.voiceassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val speechCode = 1001
    private val callCode = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        root.addView(TextView(this).apply { text = "KYRO"; textSize = 34f })
        root.addView(TextView(this).apply { text = "Your personal voice assistant"; textSize = 18f })
        status = TextView(this).apply { text = "Ready. Tap Talk to Kyro."; textSize = 16f }
        root.addView(status)
        root.addView(Button(this).apply { text = "Talk to Kyro"; setOnClickListener { listen() } })
        root.addView(Button(this).apply { text = "Android Settings"; setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) } })
        setContentView(root)
    }

    private fun listen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command to Kyro")
        }
        startActivityForResult(intent, speechCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != speechCode || resultCode != RESULT_OK) return
        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim() ?: return
        handle(spoken)
    }

    private fun handle(raw: String) {
        val cmd = raw.lowercase(Locale.getDefault()).trim()
        status.text = "Kyro heard: $raw"
        when {
            cmd.startsWith("open ") -> openApp(cmd.removePrefix("open ").trim())
            cmd == "settings" || cmd == "open settings" -> startActivity(Intent(Settings.ACTION_SETTINGS))
            cmd.startsWith("call ") -> call(cmd.removePrefix("call ").trim())
            cmd == "stop" || cmd == "cancel" -> status.text = "Kyro: Okay."
            else -> status.text = "Kyro: I heard \"$raw\". That action isn't implemented yet."
        }
    }

    private fun openApp(target: String) {
        val pm = packageManager
        val app = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .firstOrNull { pm.getApplicationLabel(it).toString().lowercase().contains(target) }
        val launch = app?.let { pm.getLaunchIntentForPackage(it.packageName) }
        if (launch != null) startActivity(launch) else status.text = "Kyro: I couldn't find $target."
    }

    private fun call(target: String) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), callCode)
            return
        }
        startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$target")))
    }
}
