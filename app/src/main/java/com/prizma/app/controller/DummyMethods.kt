package com.prizma.app.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Vibrator
import androidx.core.content.res.ResourcesCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.prizma.app.R
import org.aviran.cookiebar2.CookieBar
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DummyMethods {

    companion object{

        private const val SECOND = 1
        private const val MINUTE = 60 * SECOND
        private const val HOUR = 60 * MINUTE
        private const val DAY = 24 * HOUR
        private const val MONTH = 30 * DAY
        private const val YEAR = 12 * MONTH


        fun generateRandomString(length: Int): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }



        fun validatePermission(context: Context ): Boolean {
            var checkPermission = false
            Dexter.withActivity(context as Activity?)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        checkPermission = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        checkPermission = false
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
            return checkPermission
        }


        fun showCookie(context: Context, title : String, message : String){
            CookieBar.build(context as Activity?)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(R.color.dot_dark_screen3)
                .show()
        }


        fun getTimeAgo(time : Long): String {
            val now = System.currentTimeMillis()

            val diff = (now - time) / 1000

            return when {
                diff < MINUTE -> "Just now"
                diff < 2 * MINUTE -> "a minute ago"
                diff < 60 * MINUTE -> "${diff / MINUTE} minutes ago"
                diff < 2 * HOUR -> "an hour ago"
                diff < 24 * HOUR -> "${diff / HOUR} hours ago"
                diff < 2 * DAY -> "yesterday"
                diff < 30 * DAY -> "${diff / DAY} days ago"
                diff < 2 * MONTH -> "a month ago"
                diff < 12 * MONTH -> "${diff / MONTH} months ago"
                diff < 2 * YEAR -> "a year ago"
                else -> "${diff / YEAR} years ago"
            }
        }

        fun formatNumber(value: Int): String {
            if (value < 1000) {
                return value.toString()
            } else if (value < 1_000_000) {
                val dividedValue = value / 1000.0
                return String.format("%.1fk", dividedValue)
            } else if (value < 1_000_000_000) {
                val dividedValue = value / 1_000_000.0
                return String.format("%.1fM", dividedValue)
            } else if (value < 1_000_000_000_000) {
                val dividedValue = value / 1_000_000_000.0
                return String.format("%.1fB", dividedValue)
            }
            return value.toString()
        }

        fun convertTime(time: Long): String? {
            val formatter = SimpleDateFormat("dd MMMM k:mm")
            return formatter.format(Date(time.toString().toLong()))
        }


        @SuppressLint("SimpleDateFormat")
        fun convertToMillis(dateString: String): Long {
            val format = SimpleDateFormat("dd/M/yyyy HH:mm:ss",Locale.ENGLISH)
            val date: Date = format.parse(dateString)
            return date.time
        }

        fun isEmulator(): Boolean {
            return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.HARDWARE.contains("goldfish")
                    || Build.HARDWARE.contains("ranchu")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion") || Build.PRODUCT.startsWith("sdk") && Build.PRODUCT.endsWith(
                "google"
            ))
        }


        fun showMotionToast(context: Context, title :String, message: String, style: MotionToastStyle ){

            MotionToast.createToast(
                context as Activity,
                title,
                message,
                style,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, dev.shreyaspatil.MaterialDialog.R.font.montserrat))
        }


        fun vibratePhone(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            val vibrationDuration = 500

            if (vibrator.hasVibrator()) {
                vibrator.vibrate(vibrationDuration.toLong())
            }
        }




    }




}