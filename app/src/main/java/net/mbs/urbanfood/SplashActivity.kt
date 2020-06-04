package net.mbs.urbanfood

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import  org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import net.mbs.urbanfood.Remote.ICloudFunctions
import net.mbs.urbanfood.Remote.RetrofitCloudClient
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.BraintreeToken
import net.mbs.urbanfood.models.UserModel
import java.util.*

class SplashActivity : AppCompatActivity() {
    lateinit var userReference: DatabaseReference
    val SPLASH_SCREEN: Long = 5000
    lateinit var handler: Handler

    private  var compositeDisposable: CompositeDisposable?= CompositeDisposable()
    lateinit var cloudFunctions: ICloudFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        cloudFunctions= RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        userReference = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)

        //Animation
        var logo_animation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        var txt_animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        logo_splash.setAnimation(logo_animation)
        urbanfood.setAnimation(txt_animation)

        handler = Handler();
        checkInternet();
    }

    private fun checkInternet() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object :MultiplePermissionsListener{
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (isOnline(this@SplashActivity)) {
                        // showHome()
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            checkUserFromFirebase(currentUser);
                        } else {
                            longToast("Veiller vous connecter")
                            showLogin();
                        }

                    } else {
                        longToast("Veillez vous connecter sur Internet!!!!")
                        handler.postDelayed({
                            finish()
                        }, SPLASH_SCREEN)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    AlertDialog.Builder(this@SplashActivity)
                        .setTitle(R.string.internet_permission_title)
                        .setMessage(R.string.internet_permission_message)
                        .setNegativeButton(
                            android.R.string.cancel,
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss();
                                p1?.cancelPermissionRequest()
                            })
                        .setPositiveButton(
                            android.R.string.ok,
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                p1?.continuePermissionRequest()
                            }).show()
                }

            } ).check()

    }

    private fun checkUserFromFirebase(currentUser: FirebaseUser) {
        userReference.child(currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onCancelled(p0: DatabaseError) {
                    showRegisterDialog(currentUser)
                    finish()
                }

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val userModel = p0.getValue(UserModel::class.java)
                        if (userModel != null) {
                            FirebaseAuth.getInstance().currentUser!!
                                .getIdToken(true)
                                .addOnFailureListener{
                                        exception ->  Toast.makeText(this@SplashActivity,""+exception.message,Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener{
                                    Common.authorizeToken=it.result!!.token
                                    val headers = HashMap<String,String>()
                                    headers["Authorization"] = Common.buildToken(Common.authorizeToken)
                                    compositeDisposable!!.add(cloudFunctions.getToken(headers)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({braintreeToken: BraintreeToken? ->
                                            goToHomeActivity(userModel,braintreeToken!!.token)
                                        },{throwable: Throwable? ->
                                            Toast.makeText(this@SplashActivity,""+throwable!!.message,Toast.LENGTH_SHORT).show()
                                        }))
                                }


                        }
                    } else {
                        showRegisterDialog(currentUser)
                    }
                }

            })

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    private fun showLogin() {
        handler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()


        }, SPLASH_SCREEN)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showRegisterDialog(currentUser: FirebaseUser?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mis à Jour Profile")
        builder.setMessage("Veillez renseigner vos information")

        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.layout_register, null)

        val edt_nom = itemView.findViewById<TextInputEditText>(R.id.edt_nom)
        val date_naiss = itemView.findViewById<TextInputEditText>(R.id.date_naiss)
        val edt_adresse = itemView.findViewById<TextInputEditText>(R.id.edt_adresse)
        val edt_phone = itemView.findViewById<TextInputEditText>(R.id.edt_phone)
        //setPhone
        edt_phone.setText(currentUser!!.phoneNumber)

        //Date
        date_naiss.setOnClickListener(View.OnClickListener {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        date_naiss.setText("" + dayOfMonth + "/" + month + "/" + year)
                    },
                    mYear,
                    mMonth,
                    mDay
                )

            datePickerDialog.show()
        })

        builder.setView(itemView)
        builder.setNegativeButton("ANNULER") { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton("ENREGISTRER") { dialog, which ->
            if (TextUtils.isDigitsOnly(edt_nom.text.toString())) {
                toast("Saisir votre nom")
                return@setPositiveButton
            } else if (TextUtils.isDigitsOnly(edt_adresse.text.toString())) {
                toast("Saisir votre adress")
                return@setPositiveButton
            }

            val userModel = UserModel()
            userModel.uid = currentUser?.uid
            userModel.name = edt_nom.text.toString()
            userModel.address = edt_adresse.text.toString()
            userModel.aniversaire = date_naiss.text.toString()

            userReference.child(currentUser.uid)
                .setValue(userModel)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser!!
                            .getIdToken(true)
                            .addOnFailureListener{
                                exception ->  Toast.makeText(this,""+exception.message,Toast.LENGTH_SHORT).show()
                            }
                            .addOnCompleteListener{
                                Common.authorizeToken=it.result!!.token
                                val headers = HashMap<String,String>()
                                headers.put("Authorization",Common.buildToken(Common.authorizeToken))
                                compositeDisposable!!.add(cloudFunctions.getToken(headers)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({braintreeToken: BraintreeToken? ->
                                        dialog.dismiss()
                                        goToHomeActivity(userModel, braintreeToken!!.token)
                                    },
                                        {throwable: Throwable? ->
                                            dialog.dismiss()
                                            Toast.makeText(this,""+throwable!!.message,Toast.LENGTH_SHORT).show()
                                        }))
                            }
                    }
                }
        }

        //Important
        val dialog = builder.create()
        dialog.show()

    }

    private fun goToHomeActivity(
        userModel: UserModel?,
        token: String
    ) {
        FirebaseInstanceId.getInstance()
            .instanceId
            .addOnFailureListener { exception ->
                Toast.makeText(this,""+exception.message,Toast.LENGTH_SHORT).show()

                Common.currentUser= userModel
                Common.currentToken=token
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
            }
            .addOnCompleteListener { task->
                if(task.isSuccessful){

                    Common.currentUser= userModel
                    Common.currentToken=token
                    Common.updateToken(this,task.result!!.token)
                    startActivity(Intent(this,HomeActivity::class.java))
                    finish()
                }
            }


    }

}
