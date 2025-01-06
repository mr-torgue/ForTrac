package com.example.fortrac

import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.FormatException
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fortrac.contracts.Products
import com.example.fortrac.databinding.ActivityProductCreateBinding
import com.example.fortrac.utils.Constants
import com.example.fortrac.utils.Constants.path
import com.example.fortrac.utils.NfcUtils
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider


class ProductCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductCreateBinding
    private lateinit var btnCreate: Button
    private lateinit var btnAdd: Button
    private lateinit var btnRemove: Button
    private lateinit var selectView: RecyclerView
    private lateinit var selectedView: RecyclerView
    private lateinit var txtMessage: EditText

    private var nfcAdapter: NfcAdapter? = null
    private var tag: WritableTag? = null
    private var tagId: String? = null

    private fun getTag() = "ProductCreate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // get NFC manager
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        nfcAdapter = nfcManager.defaultAdapter
        txtMessage = binding.messagetagTxt
        btnCreate = binding.createBtn
        // when clicked initialize the tag and create a product
        btnCreate.setOnClickListener {
            writeNDefMessage()
            createProduct()
        }
        // intialize the recycler view
        recyclerView = binding.selectRecycler
        selectAdapter = SelectAdapter()
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(manager)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.setAdapter(selectAdapter)
        StudentDataPrepare()
    }

    /**
     * createProduct() does two things:
     * 1. initializes a tag with a signed message
     * 2. initializes a product smart contract
     */
    private fun createProduct() {
        Log.println(Log.INFO, getTag(), "Creating new product for tag $tagId")
        // check if tag exists
        if(tag != null) {
            // connect to ganache
            val web3j = Web3j.build(HttpService(Constants.chainURL))
            // connect to the products
            val productsContract = Products.load(
                Constants.contractAddressStr,
                web3j,
                Credentials.create(Constants.privkeyStr),
                DefaultGasProvider(),
            )
            val message: String = txtMessage.text.toString()
            productsContract.addProduct(tag!!.tagId, message, path).sendAsync()
            Log.println(Log.INFO, getTag(), "Inserted tag $tagId in smart contract ${productsContract.contractAddress} with message $message")
            web3j.shutdown()
        }
    }
    /**
     * For reading the NFC when the app is already launched
     */
    override fun onNewIntent(intent: Intent) {
        Log.println(Log.INFO, getTag(), "New Intent $intent")
        super.onNewIntent(intent)
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        try {
            tag = tagFromIntent?.let { WritableTag(it) }
        } catch (e: FormatException) {
            Log.e(getTag(), "Unsupported tag tapped", e)
            return
        }
        tagId = tag!!.tagId
        showToast("Tag tapped: $tagId with content ${tag.toString()}")
    }

    /**
     * Writing and displaying data
     */
    private fun writeNDefMessage() {
        val message = NfcUtils.prepareMessageToWrite(txtMessage.text.toString(), this)
        val writeResult = tag!!.writeData(tagId!!, message)
        if (writeResult) {
            showToast("Write successful!")
        } else {
            showToast("Write failed!")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error disabling NFC foreground dispatch", ex)
        }
    }

}