package com.prizma.app.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.prizma.app.databinding.ActivityBuyTicketBinding
import com.prizma.app.util.Constants
import com.prizma.app.util.NetworkChangeListener

import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.google.common.collect.ImmutableList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import com.prizma.app.R
import com.prizma.app.controller.DummyMethods
import com.prizma.app.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Date


class BuyTicketActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBuyTicketBinding

    private var productId : String? = null

    private val networkChangeListener = NetworkChangeListener()

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var purchase: Purchase? = null
    private val TAG_IAP = "InAppPurchaseTag"

    private lateinit var firebaseUser : FirebaseUser
    private lateinit var pd : ProgressDialog
    private var ticket : Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {finish()}

        pd = ProgressDialog(this,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()

        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        productId = intent.getStringExtra(Constants.PACKET_KEY)

        if (productId.equals(Constants.PACKET_ONE_ID)){
            binding.productType.text = "1 Bilet"
            ticket = 1
        }else if (productId.equals(Constants.PACKET_TWO_ID)){
            binding.productType.text = "5 Bilet"
            ticket = 5
        }else{
            binding.productType.text = "10 Bilet"
            ticket = 10
        }

        billingSetup()


        binding.makePurchaseBtn.setOnClickListener {
            makePurchase()
        }


    }



    private fun billingSetup() {
        billingClient = BillingClient.newBuilder(this@BuyTicketActivity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(
                billingResult: BillingResult
            ) {
                if (billingResult.responseCode ==
                    OK
                ) {
                    Log.i(TAG_IAP, "OnBillingSetupFinish connected")
                    pd.dismiss()
                    queryProduct()
                } else {
                    Log.i(TAG_IAP, "OnBillingSetupFinish failed")
                    pd.dismiss()

                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i(TAG_IAP, "OnBillingSetupFinish connection lost")
                pd.dismiss()

            }
        })
    }

    private fun queryProduct() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId!!)
                        .setProductType(
                            BillingClient.ProductType.INAPP
                        )
                        .build()
                )
            )
            .build()
        billingClient!!.queryProductDetailsAsync(
            queryProductDetailsParams
        ) { _: BillingResult?, productDetailsList: List<ProductDetails> ->
            if (productDetailsList.isNotEmpty()) {
                productDetails = productDetailsList[0]
                runOnUiThread {
                    binding.makePurchaseBtn.isEnabled = true
                    val skuList: MutableList<String> = ArrayList()
                    skuList.add(productId!!)
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient!!.querySkuDetailsAsync(
                        params.build()
                    ) { billingResult1: BillingResult, list: List<SkuDetails>? ->
                        if (billingResult1.responseCode == OK && list != null) {
                            for (skuDetails in list) {
                                val price = skuDetails.price
                                runOnUiThread {
                                    binding.priceText.text = price

                                }
                            }
                        }
                    }
                    pd.dismiss()
                }
            } else {
                Log.i(TAG_IAP, "onProductDetailsResponse: No products")
                pd.dismiss()
            }
        }
    }

    private fun makePurchase() {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                ImmutableList.of(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .build()
                )
            )
            .build()
        billingClient!!.launchBillingFlow(this, billingFlowParams)
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase>? ->
            if (billingResult.responseCode ==
                OK
                && purchases != null
            ) {
                for (purchase in purchases) {
                    completePurchase(purchase)
                }
            } else if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.USER_CANCELED
            ) {
                Log.i(TAG_IAP, "onPurchasesUpdated: Purchase Canceled")
                //StyleableToast.makeText(CompleteBuyActivity.this, "Bir hata oldu. Yeniden deneyin.", R.style.customToast).show();
            } else {
                Log.i(TAG_IAP, "onPurchasesUpdated: Error")

            }
        }

    private fun updateInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!=null){
                            val hashMap : HashMap<String,Any> = HashMap()
                            hashMap["ticket"] = user.ticket + ticket
                            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
                                .update(hashMap).addOnSuccessListener {
                                    createNewUserObject()

                                    val map: HashMap<String, Any> = HashMap()
                                    val id = System.currentTimeMillis().toString()
                                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                                    val currentDate = sdf.format(Date())

                                    map["time"] = currentDate
                                    map["id"] = id
                                    map["userId"] = firebaseUser.uid
                                    map["token"] = purchase!!.purchaseToken
                                    map["orderNumber"] = purchase!!.orderId.toString()
                                    map["email"] = firebaseUser.email.toString()
                                    map["ticket"] = ticket

                                    FirebaseFirestore.getInstance().collection("Buys").document(id)
                                        .set(map).addOnSuccessListener {
                                            DummyMethods.showMotionToast(this@BuyTicketActivity,"İşlem Başarılı.","Biletler Hesabınıza Eklendi.",MotionToastStyle.SUCCESS)
                                            val intent = Intent(this@BuyTicketActivity,
                                                com.prizma.app.activities.StartActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                        }.addOnFailureListener {

                                        }

                                }.addOnFailureListener{
                                    DummyMethods.showMotionToast(this@BuyTicketActivity,"Bir hata oldu.","Lütfen daha sonra tekrar deneyin.",MotionToastStyle.ERROR)

                                }
                        }


                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }


    }


    private fun completePurchase(item: Purchase) {

        purchase = item
        if (purchase!!.purchaseState == Purchase.PurchaseState.PURCHASED)
            runOnUiThread {
                consumePurchase()
                if (purchase!!.orderId.toString().length == 24){
                    updateInfo()
                }


            }
    }

    private fun consumePurchase() {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase!!.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult, purchaseToken ->
            if (billingResult.responseCode == OK) {
                // Acknowledge the purchase
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseListener)

                runOnUiThread {
                    // Diğer işlemleri burada yapabilirsiniz.
                }
            }
        }
        billingClient!!.consumeAsync(consumeParams, listener)
    }




    private val acknowledgePurchaseListener = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Acknowledgement successful
            Log.d(TAG_IAP, "Purchase acknowledged successfully")
            // Burada gerekli işlemleri yapabilirsiniz.
        } else {
            Log.w(TAG_IAP, "Purchase acknowledgement failed with response code: ${billingResult.responseCode}")
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
                        val prefs = FastPrefs(this@BuyTicketActivity)
                        prefs.set("user", user)
                    }
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient?.endConnection()
    }


    override fun onStart() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeListener, intentFilter)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(networkChangeListener)
        super.onStop()
    }



}