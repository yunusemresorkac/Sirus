package com.yeslab.sirus.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.UnityBanners
import com.unity3d.services.banners.view.BannerPosition
import com.yeslab.fastprefs.FastPrefs
import com.yeslab.sirus.R
import com.yeslab.sirus.controller.DummyMethods
import com.yeslab.sirus.databinding.ActivityStartBinding
import com.yeslab.sirus.model.User
import com.yeslab.sirus.model.WeeklyReward
import com.yeslab.sirus.util.Constants
import com.yeslab.sirus.util.NetworkChangeListener
import com.yeslab.sirus.viewmodel.InfoViewModel
import com.yeslab.sirus.viewmodel.TimeViewModel
import com.yeslab.sirus.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import www.sanju.motiontoast.MotionToastStyle


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private lateinit var fastPrefs : FastPrefs
    private var user: User? = null

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth: FirebaseAuth
    private val infoViewModel by viewModel<InfoViewModel>()
    private val timeViewModel by viewModel<TimeViewModel>()
    private val userViewModel by viewModel<UserViewModel>()

    private val networkChangeListener = NetworkChangeListener()
    private lateinit var pd : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pd = ProgressDialog(this,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseAuth = FirebaseAuth.getInstance()


        fastPrefs = FastPrefs(this)



        binding.openTestBtn.setOnClickListener {
            showPlayDialog()
        }


        binding.openShopBtn.setOnClickListener {
            showBuyDialog()
        }

        binding.statsBtn.setOnClickListener {
            showStatsDialog()
        }


        binding.giftsBtn.setOnClickListener {
            startActivity(Intent(this,GiftActivity::class.java))
        }

        binding.leadersBtn.setOnClickListener {
            startActivity(Intent(this,LeadersActivity::class.java))
        }

        binding.signOutBtn.setOnClickListener { signOut() }


        binding.weeklyRewardBtn.setOnClickListener {
            showRewardedAds()
        }


        binding.ordersBtn.setOnClickListener { startActivity(Intent(this, OrdersActivity::class.java)) }


        binding.shareApp.setOnClickListener {
            val shareText ="Ödüllü bilgi yarışması Sirüs'e sen de gel ve kazanmaya başla. Birbirinden keyifli sorular burada!" +
                    "https://play.google.com/store/apps/details?id=${packageName}"


            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(intent, "Share Via"))
        }

        binding.supportBtn.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:yeslabapps@gmail.com")
            }
            startActivity(Intent.createChooser(emailIntent, "Mail"))
        }

        setup()


        showGuides("Mevcut Biletleriniz Burada!","Biletler Teste Katılmak İçin Gereklidir.",binding.availableTicketsCard){
            showGuides("Bakiyeniz Burada Gözükecektir","Her Tamamladığınız Testte Bir Artar",binding.balanceCard){
                showGuides("Haftalık Ödülünüzü Buradan Alabilirsiniz","Ödülü Aldığınızda Biletiniz 1 Artacaktır.",binding.weeklyRewardBtn){
                    showGuides("Mağazadan Aldığınız Ödüller Burada Gözükecektir.","Ödüller Bu Ekranda Ulaşılabilir Olacaktır.",binding.ordersBtn){
                        showGuides("Yarışmaya Buradan Katılabilirsiniz","Teste Katılmak İçin 1 Bilet Gereklidir",binding.openTestBtn){
                            showGuides("Buradan Ödüllere Ulaşabilirsiniz.","Bakiyenizin Yettiği Her Ödülü Alabilirsiniz",binding.giftsBtn){
                                showGuides("Test Çözmek İçin Bilet Alabilirsiniz","",binding.openShopBtn){
                                    showGuides("Burada Liderler Sıralanır.","En Yükseğe Ulaşmak İçin Yarışmaya Başlayın!",binding.leadersBtn){
                                        showGuides("Yarışma İstatistikleriniz Burada Görünür.","",binding.statsBtn){
                                            val prefs = FastPrefs(this)
                                            prefs.setBoolean("guide",true)

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun showGuides(title : String, message : String, cardView: CardView, onDismiss: () ->Unit){
        val prefs = FastPrefs(this)
        val guide = prefs.getBoolean("guide",false)
        if (!guide){
            GuideView.Builder(this)
                .setTitle(title)
                .setContentText(message)
                .setGravity(Gravity.auto)
                .setDismissType(DismissType.anywhere)
                .setTargetView(cardView)
                .setGuideListener {
                    onDismiss()
                }
                .build()
                .show()
        }

    }


    private fun setup(){
        getUserData()
        checkWeeklyReward()
        UnityAds.initialize(this,Constants.UNITY_APP_ID,false)
        loadRewardedAds()

        userViewModel.getAccountType(this,firebaseUser.uid){ type ->
            if (type == Constants.BANNED_USER){
                firebaseAuth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                DummyMethods.showMotionToast(this,"Hesabınız devre dışı bırakıldı","",MotionToastStyle.ERROR)
            }
        }

    }

    private fun loadRewardedAds() {
        if (UnityAds.isInitialized()) {
            UnityAds.load(Constants.REWARDED_ID,rewardedLoadListener)
        } else {
            Handler().postDelayed({ UnityAds.load(Constants.REWARDED_ID,rewardedLoadListener) }, 1000)
        }
    }

    private fun showRewardedAds() {
        if (UnityAds.isInitialized()) {
            UnityAds.show(this, Constants.REWARDED_ID, rewardedShowListener)
        } else {
            loadRewardedAds()
            // Reklam yüklenmedi veya gösterilmeye hazır değil
            // Gerekirse burada ek işlemler yapabilirsiniz.
        }
    }

    private val rewardedLoadListener = object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(adUnitId: String) {
            // Reklam başarıyla yüklendiğinde burası çalışır
            // Reklam şimdi gösterilebilir durumda
        }

        override fun onUnityAdsFailedToLoad(adUnitId: String, error: UnityAds.UnityAdsLoadError, message: String) {

            // Reklam yüklenmesi başarısız olduğunda burası çalışır
            // Hata kodu ve mesajı alabilirsiniz
        }
    }


    private val rewardedShowListener = object : IUnityAdsShowListener {
        override fun onUnityAdsShowStart(adUnitId: String) {
            // Reklam gösterilmeye başlamadan önce burası çalışır
        }

        override fun onUnityAdsShowClick(adUnitId: String) {
            // Reklama tıklandığında burası çalışır
        }

        override fun onUnityAdsShowComplete(adUnitId: String, result: UnityAds.UnityAdsShowCompletionState) {
            // Reklam tamamlandığında burası çalışır
            updateReward()

        }

        override fun onUnityAdsShowFailure(adUnitId: String, error: UnityAds.UnityAdsShowError, message: String) {
            // Reklam gösteriminde hata oluştuğunda burası çalışır
        }
    }


    private fun updateReward(){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!=null){
                            val hashMap : HashMap<String,Any> = HashMap()
                            hashMap["ticket"] = user.ticket + 1
                            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
                                .update(hashMap).addOnSuccessListener {
                                    val map : HashMap<String,Any> = HashMap()
                                    map["userId"] = firebaseUser.uid
                                    map["weeklyTime"] = timeViewModel.getNowFromApi()

                                    FirebaseFirestore.getInstance().collection("WeeklyReward").document(firebaseUser.uid)
                                        .set(map).addOnCompleteListener {
                                            createNewUserObject()
                                            val intent = Intent(this@StartActivity,StartActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            DummyMethods.showMotionToast(this@StartActivity,"Ödül Alındı.","1 Bilet Hesabınıza Eklendi",MotionToastStyle.SUCCESS)
                                        }
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
                        val prefs = FastPrefs(this@StartActivity)
                        prefs.set("user", user)
                    }
                }
            }

    }





    private fun checkWeeklyReward(){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("WeeklyReward").document(firebaseUser.uid)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val weekly = documentSnapshot.toObject(WeeklyReward::class.java)

                        val weekLater = 7 * 24 * 60 * 60 * 1000
                        if (timeViewModel.getNowFromApi() >= weekly!!.weeklyTime + weekLater){
                            binding.weeklyRewardBtn.isEnabled = true
                            binding.weeklyRewardStatusText.text = "Haftalık Ödül"
                        }else{
                            binding.weeklyRewardBtn.isEnabled = false
                            val nextReward = weekly.weeklyTime + weekLater
                            binding.weeklyRewardStatusText.text = "${DummyMethods.convertTime(nextReward)}"
                        }

                        pd.dismiss()

                    } else {
                        binding.weeklyRewardBtn.isEnabled = true
                        binding.weeklyRewardStatusText.text = "Haftalık Ödül"
                        pd.dismiss()


                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }




    private fun showStatsDialog(){
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.stats_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(android.view.Gravity.BOTTOM)
        dialog.show()

        val correctText = dialog.findViewById<TextView>(R.id.correctAnswers)
        val wrongText = dialog.findViewById<TextView>(R.id.wrongAnswers)
        val percentageText = dialog.findViewById<TextView>(R.id.percentage)

        getStats(correctText,wrongText,percentageText)


    }






    @SuppressLint("SetTextI18n")
    private fun getStats(correctText : TextView, wrongText : TextView, percentageText: TextView){
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)
            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)

                        if (user!=null ){
                            val corrects = user.correctAnswer
                            val wrongs = user.wrongAnswer
                            val totals = corrects + wrongs

                            if (corrects != 0 && totals != 0 && wrongs != 0){
                                val correctPercent = ((100 * corrects) / (totals))
                                correctText.text = "Doğru Cevap Sayısı: $corrects"
                                wrongText.text = "Yanlış Cevap Sayısı: $wrongs"
                                percentageText.text = "Doğru Oranı: %${correctPercent}"
                            }



                        }



                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }


        }
    }



    private fun showBuyDialog(){
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.buy_ticket_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(android.view.Gravity.BOTTOM)
        dialog.show()

        val buyOneTicket = dialog.findViewById<CardView>(R.id.buyOneTicket)
        val buyFiveTicket = dialog.findViewById<CardView>(R.id.buyFiveTicket)
        val buyTenTicket = dialog.findViewById<CardView>(R.id.buyTenTicket)

        buyOneTicket.setOnClickListener {
            startActivity(Intent(this,BuyTicketActivity::class.java)
                .putExtra(Constants.PACKET_KEY,Constants.PACKET_ONE_ID)
            )
        }
        buyFiveTicket.setOnClickListener {
            startActivity(Intent(this,BuyTicketActivity::class.java)
                .putExtra(Constants.PACKET_KEY,Constants.PACKET_TWO_ID)
            )
        }
        buyTenTicket.setOnClickListener {
            startActivity(Intent(this,BuyTicketActivity::class.java)
                .putExtra(Constants.PACKET_KEY,Constants.PACKET_THREE_ID)
            )
        }

    }



    private fun getUserData(){
        user = fastPrefs.get("user",null)

        binding.username.text = "Merhaba, ${user?.username}"
        binding.rights.text = "${user?.ticket}"
        binding.balance.text = user?.balance.toString()

    }

    private fun signOut(){
        firebaseAuth.signOut()
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }



    private fun showRulesDialog(){
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.rules_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity (android.view.Gravity.BOTTOM)
        dialog.show()
    }


    private fun showPlayDialog(){
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.start_quiz_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(android.view.Gravity.BOTTOM)
        dialog.show()

        val playBtn = dialog.findViewById<Button>(R.id.playBtn)
        val rules = dialog.findViewById<TextView>(R.id.rulesBtn)

        rules.setOnClickListener { showRulesDialog() }

        infoViewModel.getTestStatus(this){ active ->
            if (active){

                if (user!!.ticket > 0){
                    playBtn.isEnabled = true
                    playBtn.text = "Oyna -> Kalan Biletiniz: ${user!!.ticket} "
                }else{
                    playBtn.isEnabled = false
                    playBtn.text = "Yarışmak için biletiniz kalmadı."
                }


                playBtn.setOnClickListener {
                    dialog.dismiss()
                    startActivity(Intent(this,TestActivity::class.java))
                    val mediaPlayer = MediaPlayer.create(this,R.raw.play_click)
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer.start()
                    }
                }
            }else{
                playBtn.isEnabled = false
                playBtn.text = "Yarışma şu an devre dışı. Birazdan tekrar deneyin."
            }
        }




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