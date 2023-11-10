package com.yeslab.sirus.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeslab.sirus.R
import com.yeslab.sirus.adapter.LeaderAdapter
import com.yeslab.sirus.adapter.OrderAdapter
import com.yeslab.sirus.databinding.ActivityOrdersBinding
import com.yeslab.sirus.model.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOrdersBinding
    private lateinit var orderList : ArrayList<Order>
    private lateinit var orderAdapter : OrderAdapter
    private lateinit var pd : ProgressDialog
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this,StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }



        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        pd = ProgressDialog(this,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()


        initRecycler()
        getOrders()

    }
    private fun initRecycler(){
        orderList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        orderAdapter = OrderAdapter(orderList,this)
        binding.recyclerView.adapter = orderAdapter

    }


    private fun getOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Orders")
                    .whereEqualTo("userId",firebaseUser.uid)
                    .orderBy("id",Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    orderList.clear()
                    val documents = querySnapshot.documents
                    for (d in documents) {
                        val order: Order? = d.toObject(Order::class.java)
                        if (order != null) {
                            orderList.add(order)
                            pd.dismiss()

                        }
                    }


                    withContext(Dispatchers.Main){
                        orderAdapter.notifyDataSetChanged()
                        pd.dismiss()

                    }
                }else{
                    pd.dismiss()

                }
            } catch (e: Exception) {
                pd.dismiss()
                println("hata ${e.message}")
            }
        }
    }


    override fun onBackPressed() {
        val intent = Intent(this,StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}