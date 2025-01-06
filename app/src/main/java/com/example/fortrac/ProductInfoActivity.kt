package com.example.fortrac

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.fortrac.contracts.Products
import com.example.fortrac.contracts.Products.Event
import com.example.fortrac.databinding.ActivityProductInfoBinding
import com.example.fortrac.utils.Constants
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.exceptions.TransactionException
import org.web3j.protocol.http.HttpService
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.gas.DefaultGasProvider
import java.net.ConnectException


class ProductInfoActivity : AppCompatActivity() {


    lateinit var writeTagFilters: Array<IntentFilter>
    private lateinit var binding: ActivityProductInfoBinding

    private fun getTag() = "ProductInfoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_product_info)
        val intent : Intent = intent
        val id = intent.getStringExtra("id")
        val content = intent.getStringExtra("content")
        populate(id.toString())
        binding.tagIDTxt.text = id.toString()
        binding.tagcontentTxt.text = content.toString()
        binding.updateBtn.setOnClickListener {
            if (id != null) {
                update(id)
            }
        }
    }

    /*
    Retrieve data from smart contract and use it to populate the activity
     */
    private fun populate(id: String) {
        Log.println(Log.INFO, getTag(), "Product Information for $id")
        try {
            val web3j = Web3j.build(HttpService(Constants.chainURL))
            val productsContract: Products = Products.load(
                Constants.contractAddressStr,
                web3j,
                Credentials.create(Constants.privkeyStr),
                DefaultGasProvider(),
            )
            // get the product data
            val events = productsContract.getTrace(id).sendAsync().get()
            val productData : Products.Product = productsContract.getProduct(id).sendAsync().get()
            Log.println(
                Log.INFO,
                getTag(),
                "Retrieved information for tag $id with tag id ${productData.tid}, owner ${productData.owner}, and data ${productData.data}"
            )
            val dataItems = ArrayList<String>()
            dataItems.add("Owner: ${productData.owner}")
            dataItems.add("Data: ${productData.data}")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataItems)
            binding.productInfoList.adapter = adapter
            adapter.notifyDataSetChanged()
            web3j.shutdown()
        }
        catch (e: ContractCallException) {
            Log.println(Log.ERROR, getTag(), "Contract call error: ${e.message}")
        }
        catch (e: ConnectException) {
            Log.println(Log.ERROR, getTag(), "Connection not estabilshed: ${e.message}")
        }
    }

    private fun update(tid: String) {
        Log.println(Log.INFO, getTag(), "Update product  $tid")
        try {
            val web3j = Web3j.build(HttpService(Constants.chainURL))
            val productsContract: Products = Products.load(
                Constants.contractAddressStr,
                web3j,
                Credentials.create(Constants.privkeyStr),
                DefaultGasProvider(),
            )
            productsContract.update(tid, binding.updateMessage.text.toString()).sendAsync().get()
            web3j.shutdown()
        }
        catch(e: TransactionException) {
            Log.println(Log.ERROR, getTag(), "Update went wrong ${e.message}")
        }
    }

    
}