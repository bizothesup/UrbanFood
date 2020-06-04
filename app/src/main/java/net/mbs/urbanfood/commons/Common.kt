package net.mbs.urbanfood.commons

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import net.mbs.urbanfood.R
import net.mbs.urbanfood.models.*
import net.mbs.urbanfood.services.MyFCMServices
import java.lang.Exception
import java.text.NumberFormat
import java.util.*

object Common {
    val NOTI_CONTENT: String? = "content"
    var NOTI_TITLE: String? = "title"
    var TOKEN_REFERENCE: String? = "Tokens"
    var authorizeToken: String? = null
    var currentToken: String = ""
    val ORDER_REFERENCE: String = "Order"
    val FOOD_REFERENCE: String = "foods"
    val COMMENT_REFERENCE: String = "Comments"
    var FOOD_SELECTED: FoodModel? = null
    var CATEGORY_SELECTED: CategoryModel? = null
    val FULL_WIDHT_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val CATEGORY_REFERENCE: String = "Category"
    val BEST_DEAL_REFERENCE: String = "BestDeals"
    val POPULAR_CATEGORY_REFERENCE: String = "MostPopular"
    val USER_REFERENCE: String = "Users"
    var currentUser: UserModel? = null

    fun formatPrice(displayPrice: Double): String {
        if (displayPrice != 0.toDouble()) {
            val currency = Currency.getInstance("XOF")
            val numberFormat = NumberFormat.getCurrencyInstance()
            numberFormat.currency = currency

            return numberFormat.format(displayPrice)
        } else
            return "0 CFA"
    }

    fun reverformat(price: String): Double? {
        val currency = Currency.getInstance("XOF")
        val numberFormat = NumberFormat.getCurrencyInstance()

        numberFormat.currency = currency
        var priceForm = 0.toDouble()

        priceForm = numberFormat.parse(price).toDouble()

        return priceForm
    }

    fun calculateExtraPrice(
        userSelectedAddon: MutableList<AddonModel>?,
        userSelectedSize: SizesModel?
    ): Double {
        var result = 0.toDouble()

        if (userSelectedSize == null && userSelectedAddon == null)
            return 0.toDouble()
        else if (userSelectedSize == null) {
            for (addon in userSelectedAddon!!)
                result += addon.price!!.toDouble()
            return result
        } else if (userSelectedAddon == null) {
            return userSelectedSize.price!!.toDouble()
        } else {
            result = userSelectedSize.price!!.toDouble()
            for (addon in userSelectedAddon)
                result += addon.price!!.toDouble()
            return result
        }

    }

    fun setSpanString(txt: String, name: String?, txtView: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(txt)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtView!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun createOrderNumer(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(Math.abs(Random().nextInt()))
            .toString()
    }

    fun buildToken(authorizeToken: String?): String {
        return StringBuilder("Bearer").append(" ").append(authorizeToken).toString()
    }

    fun updateToken(context: Context, newToken: String) {
        if (Common.currentToken != null) {
            FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE!!)
                .child(Common.currentUser!!.uid!!)
                .setValue(TokenModel(currentUser!!.phone, newToken))
                .addOnFailureListener { exception: Exception ->
                    Toast.makeText(
                        context,
                        "" + exception.message,
                        Toast.LENGTH_SHORT
                    )
                }
        }
    }

    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        content: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null

        if (intent != null) {
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val NOTIFICATION_CHANEL_ID = "UrbanFood"
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANEL_ID,
                    "UrbanFood",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.description = "Urban Food"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)
            builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.ic_restaurant_menu_black_24dp
                    )
                )

            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent)

                val notification = builder.build()
                notificationManager.notify(id, notification)

            }
        }
    }

    fun createTopicOrder(): String? {
        return StringBuilder("/topics/new_order").toString()
    }

    fun getDaystoWeek(i: Int): String {
        when(i){
           1->return  "Lundi"
           2->return  "Mardi"
           3->return  "Mercredi"
           4->return  "Jeudi"
           5->return  "Vendredi"
           6->return  "Samedi"
           7->return  "Dimanche"
        }
        return "UnKnowDay"
    }

    fun convertStatusToText(status: Int?): String{
        when(status){
            0->return  "Commande Passée"
            1->return  "Commande en cour Livraison"
            2->return  "Commande Livrée"
            3->return  "Commande annulée"
        }
        return "UnKnowStatus"
    }

}