package com.example.fortrac.fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.example.fortrac.contracts.Products
import com.example.fortrac.databinding.ActivityProductListBinding
import com.example.fortrac.utils.Constants
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.gas.DefaultGasProvider
import java.net.ConnectException
import java.util.concurrent.ExecutionException

private lateinit var binding: ActivityProductListBinding
private fun getTag() = "ProductListActivity"

class ProductListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        populate()
    }

    private fun populate() {
        Log.println(Log.INFO, getTag(), "Listing all products")
        try {
            val web3j = Web3j.build(HttpService(Constants.chainURL))
            val productsContract: Products = Products.load(
                Constants.contractAddressStr,
                web3j,
                Credentials.create(Constants.privkeyStr),
                DefaultGasProvider(),
            )
            val data = productsContract.products.sendAsync().get()
            Log.println(Log.INFO, getTag(), "Data: $data")
            //val dataItems = ArrayList<String>()
            //dataItems.add("Owner: ${data.owner}\nData: ${data.data}")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
            binding.productsList.adapter = adapter
            adapter.notifyDataSetChanged()
            web3j.shutdown()
        }
        catch (e: ContractCallException) {
            Log.println(Log.ERROR, getTag(), "Contract call error: ${e.message}")
        }
        catch (e: ExecutionException) {
            Log.println(Log.ERROR, getTag(), "Execution error: ${e.message}")
        }
        catch (e: ConnectException) {
            Log.println(Log.ERROR, getTag(), "Connection not estabilshed: ${e.message}")
        }
    }
}