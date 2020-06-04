package net.mbs.urbanfood.ui.cart

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

import net.mbs.urbanfood.R
import net.mbs.urbanfood.Remote.ICloudFunctions
import net.mbs.urbanfood.Remote.IFCMService
import net.mbs.urbanfood.Remote.RetrofitCloudClient
import net.mbs.urbanfood.Remote.RetrofitFCMClient
import net.mbs.urbanfood.adapter.MyCartAdapter
import net.mbs.urbanfood.callBacks.ILoadTimeFromFireBaseListener
import net.mbs.urbanfood.callBacks.MyButtonCallBack
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.commons.MySwiperHelper
import net.mbs.urbanfood.database.CartDataSource
import net.mbs.urbanfood.database.CartDatabase
import net.mbs.urbanfood.database.CartItem
import net.mbs.urbanfood.database.LocalCartDataSource
import net.mbs.urbanfood.eventBus.CounterCartEvent
import net.mbs.urbanfood.eventBus.HideFabCart
import net.mbs.urbanfood.eventBus.MenuItemBack
import net.mbs.urbanfood.eventBus.UpdateItemInCart
import net.mbs.urbanfood.models.BraintreeTransaction
import net.mbs.urbanfood.models.FCMRespose
import net.mbs.urbanfood.models.FCMSendData
import net.mbs.urbanfood.models.OrderModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CartFragment : Fragment(), ILoadTimeFromFireBaseListener {

    private val REQUEST_BRAINTREE_CODE: Int=8888
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private lateinit var viewModel: CartViewModel
    private var cartDataSource: CartDataSource? = null
    private var recyclerViewState: Parcelable? = null
    private var recycler_cart: RecyclerView? = null
    private var txt_empty_cart: TextView? = null
    private var txt_total_price: TextView? = null
    private var btn_place_order: Button? = null
    private var group_place_holder: CardView? = null
    private var adapterMy: MyCartAdapter? = null

    //Gmap
    private var locationRequest: LocationRequest? = null
    private var locationCallBack: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLocation: Location? = null
    private var listener: ILoadTimeFromFireBaseListener? = null
    internal var address: String = ""
    internal var comment: String = ""

    lateinit var cloudFunctions: ICloudFunctions

    //Notification
    var fcmServices:IFCMService?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().postSticky(HideFabCart(true))
        listener = this
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        fcmServices = RetrofitFCMClient.getInstance().create(IFCMService::class.java)

        viewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        val root = inflater.inflate(R.layout.cart_fragment, container, false)
        initView(root)
        initLocation()


        viewModel.initCartDataSource(requireContext())

        viewModel.getMutableLiveDataCartItems().observe(viewLifecycleOwner, Observer {
            val cartItems = it
            if (cartItems == null || cartItems.isEmpty()) {
                recycler_cart!!.setVisibility(View.GONE)
                group_place_holder!!.setVisibility(View.GONE)
                txt_empty_cart!!.setVisibility(View.VISIBLE)
            } else {
                recycler_cart!!.setVisibility(View.VISIBLE)
                group_place_holder!!.setVisibility(View.VISIBLE);
                txt_empty_cart!!.setVisibility(View.GONE);

                adapterMy = MyCartAdapter(requireContext(), cartItems);
                recycler_cart!!.adapter = adapterMy
            }
        })

        return root
    }

    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationCallBack() {
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                currentLocation = p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 3000
        locationRequest!!.smallestDisplacement = 10f
    }

    private fun initView(root: View?) {
        setHasOptionsMenu(true)
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())
        recycler_cart = root!!.findViewById(R.id.recycler_cart) as RecyclerView
        txt_empty_cart = root.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_price = root.findViewById(R.id.txt_total_price) as TextView
        btn_place_order = root.findViewById(R.id.btn_place_order) as Button
        group_place_holder = root.findViewById(R.id.group_place_holder) as CardView

        //recycler view
        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.setHasFixedSize(true)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        btn_place_order!!.setOnClickListener { _ ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Etape Suivante")
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_place_order, null)

            val edt_address = view.findViewById(R.id.edt_address) as EditText
            val edt_comment = view.findViewById(R.id.edt_comment) as EditText
            val txt_adress = view.findViewById(R.id.txt_address_detail) as TextView

            val rdi_home_address = view.findViewById(R.id.rdi_home_address) as RadioButton
            val rdi_other_address = view.findViewById(R.id.rdi_other_address) as RadioButton
            val rdi_ship_this_address = view.findViewById(R.id.rdi_ship_this_address) as RadioButton
            val rdi_cod = view.findViewById(R.id.rdi_cod) as RadioButton
            val rdi_braintree = view.findViewById(R.id.rdi_braintree) as RadioButton

            //Edt data
            edt_address.setText(Common.currentUser!!.address)

            //Event
            rdi_home_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    edt_address.setText(Common.currentUser!!.address)
                    txt_adress.setVisibility(View.GONE)
                }
            }

            rdi_other_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    edt_address.setText("")
                    txt_adress.visibility = View.GONE
                }
            }

            rdi_ship_this_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    fusedLocationProviderClient!!.lastLocation
                        .addOnFailureListener { exception: Exception ->
                            Toast.makeText(
                                requireContext(),
                                " " + exception.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            txt_adress.visibility = View.GONE
                        }
                        .addOnCompleteListener { task ->
                            val coordonne = StringBuilder().append(task.result!!.latitude)
                                .append(" / ").append(task.result!!.longitude)

                            val singleAddress = Single.just(
                                getAddressFromLatLng(
                                    task.result!!.latitude,
                                    task.result!!.longitude
                                )
                            )
                            val diaposable = singleAddress.subscribeWith(object :
                                DisposableSingleObserver<String>() {
                                override fun onSuccess(t: String) {
                                    edt_address.setText(coordonne)
                                    txt_adress.text = t;
                                    txt_adress.visibility = View.VISIBLE
                                }

                                override fun onError(e: Throwable) {
                                    edt_address.setText(coordonne)
                                    txt_adress.text = e.message;
                                    txt_adress.visibility = View.VISIBLE
                                }

                            })
                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NON") { dialog, which ->
                dialog.dismiss()
            }.setPositiveButton("OUI") { dialog, which ->
                //Tooast
                if (rdi_cod.isChecked) {
                    payementCOD(edt_address.text.toString(), edt_comment.text.toString())
                } else
                    if (rdi_braintree.isChecked) {
                    address = edt_address.text.toString()
                    comment = edt_comment.text.toString()

                    if(!TextUtils.isEmpty(Common.currentToken))
                    {
                         val dropInRequest = DropInRequest().clientToken(Common.currentToken)
                        startActivityForResult(dropInRequest.getIntent(requireContext()),REQUEST_BRAINTREE_CODE)

                    }
                }
            }
            val dialog = builder.create()
            dialog.show()

        }


        //Delete plate
        val swip = object : MySwiperHelper(requireContext(), recycler_cart!!, 200) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(context!!, "Supprimer", 30, 0, Color.parseColor("#E13F3F"),
                        object : MyButtonCallBack {
                            override fun onClick(pos: Int) {
                                val deleteItem = adapterMy!!.getItemAtPosition(pos)

                                cartDataSource!!.deleteCartItem(deleteItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSuccess(t: Int) {
                                            adapterMy!!.notifyItemRemoved(pos)
                                            sumAllItemInCart()
                                            EventBus.getDefault().postSticky(CounterCartEvent(true))
                                            Toast.makeText(
                                                context,
                                                "Plat Supprimer dans la Commande",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onError(e: Throwable) {
                                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                                                .show()
                                        }

                                    })
                            }

                        })
                )

            }


        }

        sumAllItemInCart()
    }

    private fun payementCOD(address: String, comment: String) {
        compositeDisposable!!.add(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val cartItems = it

                cartDataSource!!.sumPriceInCart(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Double> {
                        override fun onSuccess(totalPrice: Double) {
                            val finalPrice = totalPrice
                            val orderModel = OrderModel()
                            orderModel.userId = Common.currentUser!!.uid
                            orderModel.userName = Common.currentUser!!.name
                            orderModel.userPhone = Common.currentUser!!.phone
                            orderModel.shippingAdress = address
                            orderModel.comment = comment

                            if (currentLocation != null) {
                                orderModel.lat = currentLocation!!.latitude
                                orderModel.lng = currentLocation!!.longitude
                            }

                            orderModel.cartItemList = cartItems
                            orderModel.totalPayement = totalPrice
                            orderModel.discount = 0.toDouble()
                            orderModel.finalPayement = finalPrice
                            orderModel.isCod = true
                            orderModel.transactionId = "Payement a la Livraison"

                            syncLocationTimeWithCGlobalTime(orderModel)
                        }

                        override fun onSubscribe(totalPrice: Disposable) {


                        }

                        override fun onError(e: Throwable) {
                            if (!e.message!!.contains("Query returned empty result set"))
                                Toast.makeText(
                                    getContext(),
                                    "[SUM CART] " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                        }

                    })

            }, { t: Throwable? ->
                Toast.makeText(getContext(), "" + t!!.message, Toast.LENGTH_SHORT).show()
            })
        )

    }

    private fun syncLocationTimeWithCGlobalTime(orderModel: OrderModel) {
        val offsetDatabaseReference =
            FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                listener!!.onLoadTimeFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {

                val offset = p0.getValue(Long::class.java)
                val estimatTimedServer = System.currentTimeMillis() + offset!!
                val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
                val resultDate = Date(estimatTimedServer)
                listener!!.onLoadTimeSucess(
                    orderModel = orderModel,
                    estimateTimeInMs = estimatTimedServer
                )
            }

        })
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var result: String? = ""
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))
                result = sb.toString()
            } else
                result = "Adresse introuvable";
        } catch (e: IOException) {
            e.printStackTrace()
            result = e.message
        }
        return result!!
    }

    private fun sumAllItemInCart() {
        cartDataSource!!.sumPriceInCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSuccess(price: Double) {
                    txt_total_price!!.text = Common.formatPrice(price)
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty result set"))
                        Toast.makeText(getContext(), "[SUM CART] " + e.message, Toast.LENGTH_SHORT)
                            .show()
                }

            })
    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        viewModel.onStop()
        compositeDisposable!!.clear()
        EventBus.getDefault().postSticky(HideFabCart(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallBack)
        super.onStop()

    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
            )
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCartEvent(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()

            cartDataSource!!.updateCartItems(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        calculateTotalPrice();
                        recycler_cart!!.getLayoutManager()!!
                            .onRestoreInstanceState(recyclerViewState);
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(
                            getContext(),
                            "[UPDATE CART] " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        }


    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPriceInCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSuccess(price: Double) {
                    txt_total_price!!.text = Common.formatPrice(price)
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty result set"))
                        Toast.makeText(getContext(), "[SUM CART] " + e.message, Toast.LENGTH_SHORT)
                            .show()
                }

            })

    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear_cart) {
            cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                        Toast.makeText(context, "Panier Vider", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoadTimeSucess(orderModel: OrderModel, estimateTimeInMs: Long) {
        orderModel.createDate = estimateTimeInMs
        orderModel.status = 0
        wirteOrderToFireBase(orderModel)
    }

    override fun onLoadTimeFailed(message: String) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show()
    }

    private fun wirteOrderToFireBase(orderModel: OrderModel) {
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REFERENCE)
            .child(Common.createOrderNumer())
            .setValue(orderModel)
            .addOnFailureListener { e: Exception ->
                Toast.makeText(getContext(), "" + e.message, Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener { task: Task<Void> ->
                cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Int> {
                        override fun onSuccess(t: Int) {
                                //FCM apres Commande
                                val notiData= HashMap<String,String>()
                            notiData.put(Common.NOTI_TITLE!!,"Nouvelle Commande")
                            notiData.put(Common.NOTI_CONTENT!!,"Nouvelle Commande de "+Common.currentUser!!.phone)
                            val sendData =  FCMSendData(Common.createTopicOrder(),notiData)

                            compositeDisposable!!.add(fcmServices!!.sendNotification(sendData)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({fCMRespose: FCMRespose? ->
                                    Toast.makeText(getContext(), "Votre Commande est Passée avec Success", Toast.LENGTH_SHORT).show();
                                    EventBus.getDefault().postSticky( CounterCartEvent(true))
                                },{
                                    t: Throwable? ->
                                    Toast.makeText(context,""+t!!.message,Toast.LENGTH_SHORT).show()
                                }))


                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(requireContext(), "" + e.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                    })
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==REQUEST_BRAINTREE_CODE){
            if(resultCode == Activity.RESULT_OK){
                val result = data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result!!.paymentMethodNonce

                //calcule de la somme
                cartDataSource!!.sumPriceInCart(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object :SingleObserver<Double>{
                        override fun onSuccess(totalPrice: Double) {
                            //get all item to create
                            cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({cartItems: List<CartItem>? ->
                                    val headers = HashMap<String,String>()
                                    headers.put("Authorization",Common.buildToken(Common.authorizeToken))
                                    //After have all cart item, we will submit payment
                                    compositeDisposable!!.add(cloudFunctions.submitPayement(headers,totalPrice
                                        ,nonce!!.nonce)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({braintreeTransaction: BraintreeTransaction? ->
                                            if(braintreeTransaction!!.success){
                                                //create Order
                                                val finalPrice = totalPrice
                                                val orderModel = OrderModel()
                                                orderModel.userId = Common.currentUser!!.uid
                                                orderModel.userName = Common.currentUser!!.name
                                                orderModel.userPhone = Common.currentUser!!.phone
                                                orderModel.shippingAdress = address
                                                orderModel.comment = comment

                                                if (currentLocation != null) {
                                                    orderModel.lat = currentLocation!!.latitude
                                                    orderModel.lng = currentLocation!!.longitude
                                                }

                                                orderModel.cartItemList = cartItems
                                                orderModel.totalPayement = totalPrice
                                                orderModel.discount = 0.toDouble()
                                                orderModel.finalPayement = finalPrice
                                                orderModel.isCod = false
                                                orderModel.transactionId = braintreeTransaction.transaction!!.id

                                                syncLocationTimeWithCGlobalTime(orderModel)
                                            }
                                        },
                                            {t: Throwable? ->
                                                Log.d("MBSDEV",t!!.message)
                                                Toast.makeText(requireContext(),""+t!!.message,Toast.LENGTH_SHORT).show()
                                            }))
                                },{t: Throwable? ->
                                    Toast.makeText(requireContext(),""+t!!.message,Toast.LENGTH_SHORT).show()
                                })

                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(requireContext(),""+e.message,Toast.LENGTH_SHORT).show()
                        }

                    })
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}
