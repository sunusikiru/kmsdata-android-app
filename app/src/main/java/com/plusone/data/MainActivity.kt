package com.plusone.data

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.webkit.WebView
import android.webkit.WebViewClient
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if fingerprint token exists
        val token = getSharedPreferences("app", MODE_PRIVATE)
            .getString("fingerprint_token", "")
        
        if (token.isNullOrEmpty()) {
            showLoginScreen()
        } else {
            showFingerprintLogin()
        }
    }
    
    private fun showLoginScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }
        
        val title = TextView(this).apply {
            text = "Plusone Data"
            textSize = 28f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 50)
        }
        
        val etUsername = EditText(this).apply {
            hint = "Email / Phone / Username"
            setPadding(20, 15, 20, 15)
        }
        
        val etPassword = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(20, 15, 20, 15)
        }
        
        val btnLogin = Button(this).apply {
            text = "Login"
            setBackgroundColor(0xFF68C2E7.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(20, 15, 20, 15)
        }
        
        val btnFingerprint = Button(this).apply {
            text = "🔐 Login with Fingerprint"
            setBackgroundColor(0xFFDDDDDD.toInt())
            setTextColor(0xFF000000.toInt())
            setPadding(20, 15, 20, 15)
        }
        
        val token = getSharedPreferences("app", MODE_PRIVATE)
            .getString("fingerprint_token", "")
        btnFingerprint.visibility = if (token.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            login(username, password)
        }
        
        btnFingerprint.setOnClickListener {
            showFingerprintLogin()
        }
        
        layout.addView(title)
        layout.addView(etUsername)
        layout.addView(etPassword)
        layout.addView(btnLogin)
        layout.addView(btnFingerprint)
        
        setContentView(layout)
    }
    
    private fun showFingerprintLogin() {
        val executor = ContextCompat.getMainExecutor(this)
        
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val token = getSharedPreferences("app", MODE_PRIVATE)
                        .getString("fingerprint_token", "")
                    
                    if (!token.isNullOrEmpty()) {
                        fingerprintLogin(token)
                    }
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(this@MainActivity, "Error: $errString", Toast.LENGTH_LONG).show()
                    showLoginScreen()
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Plusone Data")
            .setSubtitle("Login with fingerprint")
            .setNegativeButtonText("Use password")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    private fun login(username: String, password: String) {
        val url = "https://kmsdata.com.ng/api/login/index.php"
        
        try {
            val jsonBody = JSONObject()
            jsonBody.put("identifier", username)
            jsonBody.put("password", password)
            
            val request = object : StringRequest(Request.Method.POST, url,
                { response ->
                    try {
                        val json = JSONObject(response)
                        if (json.getString("status") == "success") {
                            val token = json.getString("fingerprint_token")
                            
                            getSharedPreferences("app", MODE_PRIVATE).edit()
                                .putString("fingerprint_token", token)
                                .putString("username", username)
                                .apply()
                            
                            runOnUiThread {
                                showWebView()
                            }
                        } else {
                            Toast.makeText(this, json.getString("msg"), Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
                override fun getBodyContentType(): String = "application/json"
            }
            
            Volley.newRequestQueue(this).add(request)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun fingerprintLogin(token: String) {
        val url = "https://kmsdata.com.ng/api/login/index.php"
        
        try {
            val jsonBody = JSONObject()
            jsonBody.put("fingerprint_token", token)
            
            val request = object : StringRequest(Request.Method.POST, url,
                { response ->
                    try {
                        val json = JSONObject(response)
                        if (json.getString("status") == "success") {
                            val newToken = json.getString("fingerprint_token")
                            getSharedPreferences("app", MODE_PRIVATE).edit()
                                .putString("fingerprint_token", newToken)
                                .apply()
                            
                            runOnUiThread {
                                showWebView()
                            }
                        } else {
                            getSharedPreferences("app", MODE_PRIVATE).edit().clear().apply()
                            runOnUiThread {
                                showLoginScreen()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
                override fun getBodyContentType(): String = "application/json"
            }
            
            Volley.newRequestQueue(this).add(request)
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showWebView() {
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        
        // Add JavaScript bridge
        webView.addJavascriptInterface(AndroidBridge(), "AndroidApp")
        
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://kmsdata.com.ng/dashboard/")
        
        setContentView(webView)
    }
    
    inner class AndroidBridge {
        @android.webkit.JavascriptInterface
        fun saveLogin(username: String, password: String) {
            runOnUiThread {
                login(username, password)
            }
        }
    }
}
