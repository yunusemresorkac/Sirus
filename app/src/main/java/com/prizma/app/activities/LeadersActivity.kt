package com.prizma.app.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.prizma.app.R
import com.prizma.app.adapter.LeaderAdapter
import com.prizma.app.databinding.ActivityLeadersBinding
import com.prizma.app.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeadersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeadersBinding
    private lateinit var userList : ArrayList<User>
    private lateinit var leaderAdapter: LeaderAdapter
    private lateinit var pd : ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeadersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {finish()}

        pd = ProgressDialog(this,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()

        initRecycler()
        getLeaders()


    }

    private fun initRecycler(){
        userList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        leaderAdapter = LeaderAdapter(userList,this)
        binding.recyclerView.adapter = leaderAdapter

    }

    private fun getLeaders() {
        CoroutineScope(Dispatchers.IO).launch {
            val query = FirebaseFirestore.getInstance()
                .collection("Users")
                .orderBy("correctAnswer", Query.Direction.DESCENDING)
                .limit(20)

            query.get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        userList.clear()
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val user: User? = d.toObject(User::class.java)
                            if (user != null && user.correctAnswer > 10) {
                                userList.add(user)
                            }
                        }
                        if (userList.size > 0){
                            binding.leadersInfoText.visibility = View.GONE
                        }else{
                            binding.leadersInfoText.visibility = View.VISIBLE
                        }

                        leaderAdapter?.notifyDataSetChanged()
                    }
                    pd.dismiss()
                }
                .addOnFailureListener {
                    // Hata işleme burada yapılabilir.
                }
        }
    }


}