package net.mbs.urbanfood

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.form_layout
import net.mbs.urbanfood.Remote.ICloudFunctions
import net.mbs.urbanfood.Remote.RetrofitCloudClient
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.BraintreeToken
import net.mbs.urbanfood.models.UserModel
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val TAG = "MAIN ACTIVITY"
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private lateinit var userRef: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mverificationId: String
    private lateinit var timer: CountDownTimer
    private  var compositeDisposable:CompositeDisposable?=CompositeDisposable()
    lateinit var cloudFunctions:ICloudFunctions


    companion object {
        private val APP_REQUEST_CODE = 7171 // Any number
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cloudFunctions= RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        init()
        firebaseAuth = FirebaseAuth.getInstance()
        phoneNumberAuthCallbackListener()
    }

    private fun phoneNumberAuthCallbackListener() {
        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential.smsCode)
                edt_otp.setText(phoneAuthCredential.smsCode)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    longToast(e.message.toString())
                } else if (e is FirebaseTooManyRequestsException) {
                    longToast(e.message.toString())
                }
                longToast(e.message.toString())
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                mverificationId = verificationId
                form_layout.visibility = View.GONE
                edt_otp.visibility = View.VISIBLE
                otp_layout.visibility = View.VISIBLE
            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success");
                    edt_otp.setText("")
                    checkUserfromFirebase(currentUser = firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithCredential:echoué", task.getException());
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        longToast("Échec de la vérification! Veuillez saisir le bon code")
                    }
                }
            }
    }
    private fun startPhoneNumberVerification(phoneNumber:String){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mCallBack) // OnVerificationStateChangedCallbacks
    }
private fun verifyPhoneNumberWithCode(verificationId:String,code:String){
    val credential =PhoneAuthProvider.getCredential(verificationId,code)
    signInWithPhoneAuthCredential(credential)
}
    private fun checkUserfromFirebase(currentUser: FirebaseUser?) {
        if (currentUser != null) {
             userRef.child(currentUser.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onCancelled(error: DatabaseError) {
                    showRegisterDialog(currentUser)
                    finish()
                }

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userModel = dataSnapshot.getValue(UserModel::class.java)
                  if(dataSnapshot.exists()){
                      FirebaseAuth.getInstance().currentUser!!
                          .getIdToken(true)
                          .addOnFailureListener{
                                  exception ->  Toast.makeText(this@MainActivity,""+exception.message,Toast.LENGTH_SHORT).show()
                          }
                          .addOnCompleteListener{
                              Common.authorizeToken=it.result!!.token
                              val headers = HashMap<String,String>()
                              headers.put("Authorization",Common.buildToken(Common.authorizeToken))
                              compositeDisposable!!.add(cloudFunctions.getToken(headers)
                                  .subscribeOn(Schedulers.io())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe({braintreeToken: BraintreeToken? ->
                                      goToHomeActivity(userModel,braintreeToken!!.token)
                                  },{throwable: Throwable? ->
                                      Toast.makeText(this@MainActivity,""+throwable!!.message,Toast.LENGTH_SHORT).show()
                                  }))
                          }
                  }else{
                    showRegisterDialog(currentUser)
                  }
                }

            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showRegisterDialog(currentUser: FirebaseUser?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mise à Jour Profile")
        builder.setMessage("Veillez renseigner vos information")

        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.layout_register, null)

       val  edt_nom = itemView.findViewById<EditText>(R.id.edt_nom)
       val  date_naiss = itemView.findViewById<EditText>(R.id.date_naiss)
       val  edt_adresse = itemView.findViewById<EditText>(R.id.edt_adresse)
       val  edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)
        //setPhone
           edt_phone.setText(currentUser!!.phoneNumber)

        //Date
        date_naiss.setOnClickListener(OnClickListener {
            val c = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                date_naiss.setText(""+dayOfMonth+"/"+month+"/"+year)
            },mYear,mMonth,mDay)

            datePickerDialog.show()
        })

        builder.setView(itemView)
        builder.setNegativeButton("ANNULER") { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton("ENREGISTRER")  { dialog, which ->
            if(TextUtils.isDigitsOnly(edt_nom.text.toString())){
                toast("Saisir votre nom")
                return@setPositiveButton
            }
            else if(TextUtils.isDigitsOnly(edt_adresse.text.toString())){
                toast("Saisir votre adress")
                return@setPositiveButton
            }

            val userModel= UserModel()
            userModel.uid= currentUser.uid
            userModel.name = edt_nom.text.toString()
            userModel.address = edt_adresse.text.toString()
            userModel.aniversaire = date_naiss.text.toString()

            userRef.child(currentUser.uid)
                .setValue(userModel)
                .addOnCompleteListener{
                    task ->
                    if (task.isSuccessful)
                    {
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
                    Common.updateToken(this,task.result!!.token)
                    Common.currentUser= userModel
                    Common.currentToken=token
                    startActivity(Intent(this,HomeActivity::class.java))
                    finish()
                }
            }


    }

    private fun init() {
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        resendOtp.visibility = View.GONE
        otp_layout.visibility = View.GONE

        send_otp.setOnClickListener(OnClickListener {

            val nowithCode :String = "+"+ccp.selectedCountryCode+edt_phoneNo.text.toString()

            startPhoneNumberVerification(nowithCode)
            starTimer()
        })

        btn_valide.setOnClickListener(OnClickListener {
            verifyPhoneNumberWithCode(mverificationId,edt_otp.text.toString().trim())
        })
    }

    private fun starTimer() {
        timer= object :CountDownTimer(6000,1000){
            override fun onFinish() {
            skip.visibility=View.GONE
            resendOtp.visibility=View.VISIBLE
            }

            override fun onTick(millisUntilFinished: Long) {
            skip.visibility=View.VISIBLE
            skip.text = "Temps restant"+millisUntilFinished/1000
            }

        }.start()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable!!.clear()
    }
}
