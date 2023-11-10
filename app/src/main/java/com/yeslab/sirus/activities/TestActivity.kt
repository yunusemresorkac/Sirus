package com.yeslab.sirus.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import com.yeslab.sirus.R
import com.yeslab.sirus.controller.DummyMethods
import com.yeslab.sirus.databinding.ActivityTestBinding
import com.yeslab.sirus.model.Question
import com.yeslab.sirus.util.Constants
import com.yeslab.sirus.util.NetworkChangeListener
import com.yeslab.sirus.viewmodel.TestViewModel
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var questionList: MutableList<Question>
    private var currentQuestionIndex = 0
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis = Constants.QUESTION_TIME
    private var userAnswered = false
    private lateinit var pd : ProgressDialog
    private lateinit var firebaseUser: FirebaseUser
    private val testViewModel by viewModel<TestViewModel>()

    private var isTestCompleted = true
    private val networkChangeListener = NetworkChangeListener()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        questionList = mutableListOf()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        pd = ProgressDialog(this,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()

        loadQuestions()


        val answerButtons = listOf(
            binding.answerOne,
            binding.answerTwo,
            binding.answerThree,
            binding.answerFour
        )

        for (button in answerButtons) {
            button.setOnClickListener {
                if (!userAnswered) { // Kullanıcı daha önce cevap vermediyse
                    val selectedAnswerIndex = answerButtons.indexOf(it as Button)
                    checkAnswer(selectedAnswerIndex)
                }
            }
        }

        getIconForSound()

        binding.reportBtn.setOnClickListener { reportDialog() }

        binding.soundsBtn.setOnClickListener { soundControl() }
    }

    private fun getIconForSound(){
        val prefs = FastPrefs(this)
        val isSoundActive = prefs.getBoolean("isSoundActive",true)

        if (isSoundActive){
            binding.soundsBtn.setImageResource(R.drawable.baseline_music_note_24)
        }else{
            binding.soundsBtn.setImageResource(R.drawable.baseline_music_off_24)
        }
    }

    private fun soundControl(){
        val prefs = FastPrefs(this)
        val isSoundActive = prefs.getBoolean("isSoundActive",true)

        if (isSoundActive){
            prefs.setBoolean("isSoundActive",false)
            binding.soundsBtn.setImageResource(R.drawable.baseline_music_off_24)
            DummyMethods.showMotionToast(this,"Sesler Kapatıldı","",MotionToastStyle.INFO)
        }else{
            prefs.setBoolean("isSoundActive",true)
            binding.soundsBtn.setImageResource(R.drawable.baseline_music_note_24)
            DummyMethods.showMotionToast(this,"Sesler Açıldı","",MotionToastStyle.INFO)
        }
    }

    private fun finishTestActivity(){
        val intent = Intent(this,StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun resetCountdown() {
        timeLeftInMillis = Constants.QUESTION_TIME
    }


    private fun loadQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Kullanıcının çözdüğü soruların ID'lerini Firestore'dan al
                val solvedQuestionIds = FirebaseFirestore.getInstance().collection("MyQuestions")
                    .document(firebaseUser.uid)
                    .collection("MyQuestions")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.getString("answeredQuestionId") }


                val querySnapshot = FirebaseFirestore.getInstance().collection("Questions")
                    .orderBy("questionId")
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val list = querySnapshot.documents

                    val remainingQuestions = mutableListOf<Question>()

                    for (d in list) {
                        val question: Question? = d.toObject(Question::class.java)
                        if (question?.questionId != null && question.status == Constants.ACTIVE_QUESTION && !solvedQuestionIds.contains(question.questionId)) {
                            remainingQuestions.add(question)
                        }
                    }

                    if (remainingQuestions.size > 6) {
                        val randomQuestions = remainingQuestions.shuffled().take(7)

                        questionList.addAll(randomQuestions)
                        binding.progressBar.max = questionList.size
                        pd.dismiss()

                        runOnUiThread {

                            testViewModel.lostQuizRight(firebaseUser.uid,this@TestActivity)
                            loadNextQuestion()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isTestCompleted = false
                            DummyMethods.showMotionToast(
                                this@TestActivity,
                                "Çözebileceğiniz soru kalmadı.",
                                "Daha sonra tekrar deneyiniz.",
                                MotionToastStyle.INFO
                            )
                            pd.dismiss()
                            finishTestActivity()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("hata", e.message.toString())
            }
        }
    }


    private fun loadNextQuestion() {
        userAnswered = false

        if (currentQuestionIndex < questionList.size) {
            resetCountdown()
            startCountdown()

            val currentQuestion = questionList[currentQuestionIndex]
            binding.questionText.text = currentQuestion.question
            val shuffledAnswers = currentQuestion.shuffleAnswers()

            val answerButtons = listOf(
                binding.answerOne,
                binding.answerTwo,
                binding.answerThree,
                binding.answerFour
            )

            for (i in answerButtons.indices) {
                answerButtons[i].text = shuffledAnswers[i]
                answerButtons[i].isEnabled = true
                answerButtons[i].setBackgroundResource(R.drawable.normal_button)
            }

            testViewModel.addAnsweredQuestions(currentQuestion.questionId,firebaseUser.uid)

        } else {
            if (currentQuestionIndex == questionList.size){
                testViewModel.addBalanceForWinners(firebaseUser.uid, this)
                DummyMethods.showMotionToast(this, "Tebrikler, Kazandınız.", "1 Bakiye Hesabınıza Eklendi.", MotionToastStyle.SUCCESS)
                Handler().postDelayed({
                    finishTestActivity()

                }, 1000)
            }
        }
    }


    private fun checkAnswer(selectedAnswerIndex: Int) {
        if (!userAnswered) {
            userAnswered = true
            val currentQuestion = questionList[currentQuestionIndex]
            val correctAnswer = currentQuestion.correctAnswer

            if (getAnswerButton(selectedAnswerIndex).text == correctAnswer) {
                countDownTimer?.cancel()
                // Cevap doğru
                val correctButton = getAnswerButton(selectedAnswerIndex)
                correctButton.setBackgroundResource(R.drawable.correct_button)
                val prefs = FastPrefs(this)
                val isSoundActive = prefs.getBoolean("isSoundActive",true)
                if (isSoundActive){
                    val mediaPlayer = MediaPlayer.create(this,R.raw.correct_sound)
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer.start()
                    }


                }


                binding.progressBar.progress++
                testViewModel.addCorrectAnswer(firebaseUser.uid,this)
                Handler().postDelayed({
                    currentQuestionIndex++
                    loadNextQuestion()
                }, 1000)
            } else {

                val prefs = FastPrefs(this)
                val isSoundActive = prefs.getBoolean("isSoundActive",true)
                if (isSoundActive){
                    DummyMethods.vibratePhone(this)
                    val mediaPlayer = MediaPlayer.create(this,R.raw.incorrect_sound)
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer.start()
                    }

                }

                val selectedButton = getAnswerButton(selectedAnswerIndex)
                selectedButton.setBackgroundResource(R.drawable.incorrect_button)
                DummyMethods.showMotionToast(
                    this,
                    "Yanlış cevap!",
                    "Test sona erdi.",
                    MotionToastStyle.ERROR
                )
                testViewModel.addWrongAnswer(firebaseUser.uid,this)

                Handler().postDelayed({
                    finishTestActivity()
                }, 1000)
            }
        }
    }

    private fun startCountdown() {


        countDownTimer = object : CountDownTimer(timeLeftInMillis.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished.toInt()
                val secondsLeft = (timeLeftInMillis / 1000).toString()
                binding.counterText.text = secondsLeft

                val maxTime = Constants.QUESTION_TIME
                val progress = ((maxTime - timeLeftInMillis) / 1000).toFloat()
                binding.timeProgressBar.setProgress(progress,false , 1000)
            }

            override fun onFinish() {
                if (!userAnswered) {
                    disableAnswerButtons()
                    DummyMethods.showMotionToast(this@TestActivity,"Zaman doldu!","Test sona erdi.",MotionToastStyle.ERROR)
                    userAnswered = true

                    Handler().postDelayed({
                        finishTestActivity()
                    }, 1000)
                }
            }
        }.start()
    }
    private fun disableAnswerButtons() {
        val answerButtons = listOf(
            binding.answerOne,
            binding.answerTwo,
            binding.answerThree,
            binding.answerFour
        )

        for (button in answerButtons) {
            button.isEnabled = false
        }
    }


    private fun getAnswerButton(answerIndex: Int): Button {
        val answerButtons = listOf(
            binding.answerOne,
            binding.answerTwo,
            binding.answerThree,
            binding.answerFour
        )
        return answerButtons[answerIndex]
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()


    }

    override fun onBackPressed() {
        areYouSureDialog()
    }




    private fun areYouSureDialog(){
        val mDialog = MaterialDialog.Builder(this)
            .setTitle("Testi Kapatmak İstediğinize Emin Misiniz?")
            .setMessage("Testi kapatmanız halinde 1 bilet hakkınızı kaybetmiş olacaksınız.")
            .setCancelable(true)
            .setPositiveButton("Testi Kapat") { dialogInterface, which ->
                dialogInterface.dismiss()
                Handler().postDelayed({
                    finishTestActivity()

                }, 500)
            }
            .setNegativeButton("İptal") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()

    }



    private fun reportDialog(){
        val mDialog = MaterialDialog.Builder(this)
            .setTitle("Bu Soru Hatalı Mı?")
            .setMessage("Bu sorunun hatalı olduğunu düşünüyorsanız bize bildirebilirsiniz.")
            .setCancelable(true)
            .setPositiveButton("Bildir") { dialogInterface, which ->
                testViewModel.reportQuestion(questionList[currentQuestionIndex],this, firebaseUser.uid)
                dialogInterface.dismiss()
            }
            .setNegativeButton("İptal") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()

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
