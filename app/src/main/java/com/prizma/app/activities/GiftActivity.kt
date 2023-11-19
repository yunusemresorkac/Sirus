package com.prizma.app.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import com.prizma.app.R
import com.prizma.app.adapter.GiftAdapter
import com.prizma.app.controller.DummyMethods
import com.prizma.app.databinding.ActivityGiftBinding
import com.prizma.app.interfaces.GiftClick
import com.prizma.app.model.Gift
import com.prizma.app.model.Order
import com.prizma.app.model.User
import com.prizma.app.util.Constants
import com.prizma.app.viewmodel.GiftViewModel
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class GiftActivity : AppCompatActivity(),GiftClick {

    private lateinit var binding: ActivityGiftBinding
    private val giftViewModel by viewModel<GiftViewModel>()
    private lateinit var giftList : ArrayList<Gift>
    private lateinit var giftAdapter : GiftAdapter
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {finish()}

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initRecycler()
        getGifts()

    }

    private fun initRecycler(){
        giftList = ArrayList()
        binding.recyclerView.layoutManager = GridLayoutManager(this,2)
        binding.recyclerView.setHasFixedSize(true)
        giftAdapter = GiftAdapter(giftList,this,this)
        binding.recyclerView.adapter = giftAdapter
    }

    private fun getGifts(){
        giftViewModel.getGifts()
        giftViewModel.getAllGifts().observe(this){ gifts ->
            giftList.addAll(gifts!!)
            giftAdapter.notifyDataSetChanged()
            if (giftList.size > 0){
                binding.giftInfoText.visibility = View.GONE
            }else{
                binding.giftInfoText.visibility = View.VISIBLE
            }
        }


    }

    override fun clickGift(gift: Gift) {
        showGiftBottom(gift)



    }

    private fun showGiftBottom(gift: Gift){
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_gift)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val giftImage = dialog.findViewById<ImageView>(R.id.giftImage)
        val giftTitle = dialog.findViewById<TextView>(R.id.giftTitle)
        val giftDesc = dialog.findViewById<TextView>(R.id.giftDesc)
        val buyGift = dialog.findViewById<TextView>(R.id.buyGift)

        Glide.with(this).load(gift.imageUrl).into(giftImage)
        giftTitle.text = gift.title
        giftDesc.text = gift.description

        buyGift.setOnClickListener {
            val fastPrefs = FastPrefs(this)
            val user : User? = fastPrefs.get("user",null)
            if (user!!.balance >= gift.price){
                showBuyGift(gift)
            }else{
                val diff = gift.price - user.balance
                DummyMethods.showMotionToast(this,"Yetersiz Bakiye!","$diff bakiye eksik.",
                    MotionToastStyle.INFO)
            }
        }

    }



    private fun showBuyGift(gift: Gift){
        val mDialog = MaterialDialog.Builder(this)
            .setTitle("Bu ödülü almak istediğinize emin misiniz?")
            .setMessage("${gift.title} - ${gift.price} Bakiye")
            .setCancelable(true)
            .setPositiveButton("Satın Al") { dialogInterface, which ->
                lostBalance(gift)
            }
            .setNegativeButton("İptal") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()

    }

    private fun addOrder(gift: Gift){
        val id = System.currentTimeMillis().toString()
        val order = Order(firebaseUser.uid,
            firebaseUser.email!!,id,gift.title,gift.price,Constants.PENDING_ORDER,"", gift.id,gift.imageUrl)

        FirebaseFirestore.getInstance().collection("Orders").document(id)
            .set(order)
            .addOnCompleteListener {
                DummyMethods.showMotionToast(this,"Siparişiniz Alındı!","Ödülünüz Kısa Süre İçinde Bu Ekranda Görünecektir.",MotionToastStyle.SUCCESS)
                startActivity(Intent(this,OrdersActivity::class.java))
            }

    }


    private fun lostBalance(gift: Gift){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!!.ticket > 0 ){
                            val map : HashMap<String,Any> = HashMap()
                            map["balance"] = user.balance - gift.price
                            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
                                .update(map).addOnSuccessListener {
                                    addOrder(gift)
                                    createNewUserObject()

                                }
                        }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }


    private fun createNewUserObject()= CoroutineScope(Dispatchers.IO).launch {
        FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        val prefs = FastPrefs(this@GiftActivity)
                        prefs.set("user", user)
                    }
                }
            }

    }

}