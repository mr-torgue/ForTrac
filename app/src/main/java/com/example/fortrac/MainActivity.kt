package com.example.fortrac

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fortrac.databinding.ActivityMainBinding
import com.example.fortrac.fragments.ProductListActivity
import com.example.fortrac.utils.NfcUtils

/**
 * This activity has three actions:
 * 1. click button to create a new product
 * 2. click button to explore existing products
 * 3. scan NFC tag and go to product information
 * MainActivity listens for NFC intents
 */
class MainActivity : AppCompatActivity() {


    private lateinit var btnCreate: Button
    private lateinit var btnList: Button
    private lateinit var binding: ActivityMainBinding
    private var adapter: NfcAdapter? = null
    // tag and tag ID
    private var tag: WritableTag? = null
    private var tagId: String? = null

    private fun getTag() = "MainActivity"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        // initialize create product button and add onclick listener
        btnCreate = binding.createProductBtn
        btnCreate.setOnClickListener {
            Log.println(Log.INFO, getTag(), "Opening new activity")
            val i = Intent(this, ProductCreateActivity::class.java)
            startActivity(i)
        }

        // initialize list product button and add onclick listener
        btnList = binding.productsExploreBtn
        btnList.setOnClickListener {
            Log.println(Log.INFO, getTag(), "Opening product list")
            val i = Intent(this, ProductListActivity::class.java)
            startActivity(i)
        }
        // initialize a nfcmanager to read tags
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        adapter = nfcManager.defaultAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_settings) {
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows a Toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * For reading the NFC when the app is already launched
     */
    override fun onNewIntent(intent: Intent) {
        Log.println(Log.INFO, getTag(), "Main Activity got a new intent $intent")
        super.onNewIntent(intent)
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        try {
            tag = tagFromIntent?.let { WritableTag(it) }
            if(intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
                val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                val message = rawMsgs?.let { NfcUtils.getData(it) }
                val i = Intent(this, ProductInfoActivity::class.java)
                i.putExtra("id", tag!!.tagId)
                i.putExtra("content", message)
                startActivity(i)
            }
        } catch (e: FormatException) {
            Log.e(getTag(), "Unsupported tag tapped", e)
            return
        }
        tagId = tag!!.tagId
        showToast("Tag tapped: $tagId with content ${tag.toString()}")
    }

    /**
     * Enable/disable  foreground dispatching of NFC
     */
    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        disableNfcForegroundDispatch()
        super.onPause()
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error disabling NFC foreground dispatch", ex)
        }
    }

}