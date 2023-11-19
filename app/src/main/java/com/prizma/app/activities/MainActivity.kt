package com.prizma.app.activities

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.yeslab.fastprefs.FastPrefs
import com.prizma.app.R
import com.prizma.app.controller.DummyMethods
import com.prizma.app.databinding.ActivityMainBinding
import com.prizma.app.model.User
import com.prizma.app.util.NetworkChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.aviran.cookiebar2.CookieBar
import www.sanju.motiontoast.MotionToastStyle

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var firestore : FirebaseFirestore

    private val networkChangeListener = NetworkChangeListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        firestore  = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser!=null){
            val intent  = Intent(this@MainActivity, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.showLogin.setOnClickListener {
            binding.loginLayout.visibility = View.VISIBLE
            binding.registerLayout.visibility = View.GONE
        }
        binding.showRegister.setOnClickListener {
            binding.loginLayout.visibility = View.GONE
            binding.registerLayout.visibility = View.VISIBLE
        }


        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.privacy.setOnClickListener {
            showPrivacy()
        }

        changeEditTextTintColorWhenFocus()
    }

    private fun showPrivacy(){
        val dialog = Dialog(this@MainActivity,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.rules_dialog)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val titles: TextView = dialog.findViewById(R.id.titles)
        val desc: TextView = dialog.findViewById(R.id.desc)

        titles.text = "Gizlilik Sözleşmesi"
        desc.text = getString(R.string.privacy)

    }


    private fun changeEditTextTintColorWhenFocus(){

        binding.emailLogin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.emailLogin.backgroundTintList = getColorStateList(R.color.appOrange)
            } else {
                binding.emailLogin.backgroundTintList = getColorStateList(R.color.black)
            }
        }
        binding.passwordLogin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.passwordLogin.backgroundTintList = getColorStateList(R.color.appOrange)
            } else {
                binding.passwordLogin.backgroundTintList = getColorStateList(R.color.black)
            }
        }

        binding.emailRegister.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.emailRegister.backgroundTintList = getColorStateList(R.color.appOrange)
            } else {
                binding.emailRegister.backgroundTintList = getColorStateList(R.color.black)
            }
        }

        binding.passwordRegister.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.passwordRegister.backgroundTintList = getColorStateList(R.color.appOrange)
            } else {
                binding.passwordRegister.backgroundTintList = getColorStateList(R.color.black)
            }
        }
        binding.usernameRegister.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.usernameRegister.backgroundTintList = getColorStateList(R.color.appOrange)
            } else {
                binding.usernameRegister.backgroundTintList = getColorStateList(R.color.black)
            }
        }

        binding.forgotPassword.setOnClickListener {
            forgotPassword()
        }

    }

    private fun forgotPassword(){
        val dialog = Dialog(this@MainActivity,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_forgot_password)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val editText: EditText = dialog.findViewById(R.id.forgotPasswordEt)
        val button: Button = dialog.findViewById(R.id.sendPasswordLink)

        button.setOnClickListener {
            if (editText.text.toString().trim().isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(editText.text.toString().trim())
                    .addOnSuccessListener {
                        dialog.dismiss()
                        CookieBar.build(this@MainActivity)
                            .setTitle("Gelen kutunuzu ve spam klasörünü kontrol edin.")
                            .setCookiePosition(CookieBar.TOP)
                            .show()
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }
            }
        }

    }

    private fun login() {
        if (binding.emailLogin.text.isNotEmpty() && binding.passwordLogin.text.isNotEmpty()) {
            val pd = ProgressDialog(this, R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()

            coroutineScope.launch {
                try {
                    val authResult = withContext(Dispatchers.IO) {
                        firebaseAuth.signInWithEmailAndPassword(
                            binding.emailLogin.text.toString().trim(),
                            binding.passwordLogin.text.toString().trim()
                        ).await()
                    }

                    val user = authResult.user
                    user?.reload()

                    if (user?.isEmailVerified == true) {



                        val myUser = withContext(Dispatchers.IO) {
                            firestore.collection("Users").document(user.uid)
                                .get().await().toObject(User::class.java)
                        }

                        if (myUser != null) {
                            val prefs = FastPrefs(this@MainActivity)
                            prefs.set("user", myUser)


                            pd.dismiss()

                            val intent  = Intent(this@MainActivity, StartActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)


                        }
                    } else {
                        firebaseAuth.signOut()
                        pd.dismiss()
                        DummyMethods.showCookie(this@MainActivity, "Lütfen Mail Adresinizi Doğrulayın.", "")
                    }
                } catch (e: Exception) {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun register() {
        if (binding.emailRegister.text.isNotEmpty() && binding.passwordRegister.text.isNotEmpty() && binding.usernameRegister.text.isNotEmpty()) {
            val pd = ProgressDialog(this@MainActivity, R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            coroutineScope.launch {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(
                        binding.emailRegister.text.toString().trim(),
                        binding.passwordRegister.text.toString().trim()
                    ).await()

                    firestore.collection("Users").whereEqualTo("deviceId",deviceId)
                        .get().addOnCompleteListener { task->
                            if (task.isSuccessful){
                                val querySnapshot: QuerySnapshot? = task.result
                                if (!querySnapshot!!.isEmpty) {
                                    val currentUser = firebaseAuth.currentUser
                                    currentUser?.delete()
                                    pd.dismiss()
                                    DummyMethods.showMotionToast(this@MainActivity,"Bu Cihazda Zaten Bir Hesap Açılmış"
                                    ,"Aynı Cihazdan Sadece 1 Hesap Açabilirsiniz",MotionToastStyle.INFO)
                                } else {
                                    val prefs = FastPrefs(this@MainActivity)
                                    val userId = authResult.user?.uid ?: ""
                                    val user = User(binding.usernameRegister.text.toString().trim(),binding.emailRegister.text.toString().trim()
                                    ,0,0,deviceId,userId,System.currentTimeMillis(),0,0,0)

                                    firestore.collection("Users").document(userId)
                                        .set(user).addOnCompleteListener {
                                            prefs.set("user", user)

                                            pd.dismiss()
                                            sendEmailVerification()
                                        }


                                }
                            }
                        }

                } catch (e: Exception) {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.loginLayout.visibility = View.VISIBLE
                binding.registerLayout.visibility = View.GONE
                val email = binding.emailRegister.text.toString().trim()
                val pass  = binding.passwordRegister.text.toString().trim()
                binding.emailLogin.setText(email)
                binding.passwordLogin.setText(pass)
                DummyMethods.showCookie(this@MainActivity,"Şu Mail Adresine Onay Maili Gönderildi. " + user.email,"Lütfen Hesabınızı Doğrulayın.")


            } else {
                Toast.makeText(this@MainActivity, "İşlem sırasında bir hata oldu.", Toast.LENGTH_SHORT).show()

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