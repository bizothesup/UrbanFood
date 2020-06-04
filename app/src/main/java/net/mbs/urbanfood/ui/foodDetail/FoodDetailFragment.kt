package net.mbs.urbanfood.ui.foodDetail

import androidx.appcompat.app.AppCompatActivity
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.ui.foodDetail.FoodDetailViewModel
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast

import net.mbs.urbanfood.R
import net.mbs.urbanfood.database.CartDataSource
import net.mbs.urbanfood.database.CartDatabase
import net.mbs.urbanfood.database.CartItem
import net.mbs.urbanfood.database.LocalCartDataSource
import net.mbs.urbanfood.eventBus.CounterCartEvent
import net.mbs.urbanfood.eventBus.HideFabCart
import net.mbs.urbanfood.eventBus.MenuItemBack
import net.mbs.urbanfood.models.AddonModel
import net.mbs.urbanfood.models.CommentModel
import net.mbs.urbanfood.models.FoodModel
import net.mbs.urbanfood.ui.comment.CommentFragment
import org.greenrobot.eventbus.EventBus
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.text.StringBuilder

class FoodDetailFragment : Fragment(), TextWatcher {


    private lateinit var foodDetailViewModel: FoodDetailViewModel
    private var dialog: AlertDialog? = null
    private var waitingDialog: android.app.AlertDialog? = null
    private var addonbottomSheetDialog: BottomSheetDialog? = null
    private var chip_group_addon: ChipGroup? = null
    private var app_bar_layout: AppBarLayout? = null
    private var img_food: ImageView? = null
    private var btnCard: CounterFab? = null
    private var btn_rating: FloatingActionButton? = null
    private var nestedScrollView: NestedScrollView? = null
    private var food_name: TextView? = null
    private var food_price: TextView? = null
    private var number_button: ElegantNumberButton? = null
    private var rattingBar: RatingBar? = null
    private var food_description: TextView? = null
    private var rdi_group_size: RadioGroup? = null
    private var img_add_addon: ImageView? = null
    private var chip_group_user_selected_addon: ChipGroup? = null
    private var btnShowComment: Button? = null
    private var edt_search: EditText? = null

    private var cartDataSource:CartDataSource?=null
    private var compositeDisposable:CompositeDisposable = CompositeDisposable()

    override fun onStop() {
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFabCart(false))
        super.onStop()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().postSticky(HideFabCart(true))
        foodDetailViewModel = ViewModelProviders.of(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fooddetail_fragment, container, false)
        initView(root)

        foodDetailViewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            displayInfo(it)
        })

        foodDetailViewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
            submitRatingToFireBase(it);
        })
        return root
    }

    private fun submitRatingToFireBase(commentModel: CommentModel?) {
        waitingDialog?.show()
        FirebaseDatabase.getInstance()
            .getReference(Common.COMMENT_REFERENCE)
            .child(Common.FOOD_SELECTED?.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    addRatingFood(commentModel?.ratingValue);
                waitingDialog?.dismiss()
            }
    }

    private fun addRatingFood(ratingValue: Float?) {
        FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REFERENCE)
            .child(Common.CATEGORY_SELECTED?.menu_id!!)
            .child(Common.FOOD_REFERENCE)
            .child(Common.FOOD_SELECTED?.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val foodModel = p0.getValue(FoodModel::class.java)
                        foodModel?.key = Common.FOOD_SELECTED?.key
                        //Apply Rating

                        if (foodModel?.ratingValue == null)
                            foodModel?.ratingValue = 0.0
                        if (foodModel?.ratingCount == null)
                            foodModel?.ratingCount = 0L
                        val sumrating = foodModel?.ratingValue!!.toDouble() + (ratingValue!!)
                        val ratingCount = foodModel?.ratingCount!! + 1
                        val result = sumrating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        foodModel?.ratingValue = result
                        foodModel?.ratingCount = ratingCount

                        p0.ref.updateChildren(updateData)
                            .addOnCompleteListener {
                                waitingDialog?.dismiss()
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        context!!,
                                        "Merci d'avoir donné votre avis !!!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Common.FOOD_SELECTED = foodModel
                                    foodDetailViewModel.setFoodModel(foodModel!!)
                                }
                            }
                    } else
                        waitingDialog?.dismiss()
                }

            })
    }

    private fun displayInfo(foodModel: FoodModel?) {
        Glide.with(this).load(foodModel?.image).into(img_food!!);
        food_name?.text = foodModel?.name
        food_description?.text = foodModel?.description
        food_price?.text = Common.formatPrice(foodModel?.price!!.toDouble())
        rattingBar?.rating = foodModel?.ratingValue!!.toFloat()

        //taille Size
        for (sizeModel in foodModel!!.size!!) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compounButton, b ->
                if (b) {
                    Common.FOOD_SELECTED!!.userSelectedSize = sizeModel
                }

                calculateTotalPrice()
            }

            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price
            rdi_group_size?.addView(radioButton)
        }

        if (rdi_group_size!!.childCount > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }

        calculateTotalPrice()

    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.FOOD_SELECTED!!.price!!.toDouble()
        var displayPrice = 0.0

        //Addon
        if (Common.FOOD_SELECTED!!.userSelectedAddon != null && Common.FOOD_SELECTED!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.FOOD_SELECTED!!.userSelectedAddon!!) {
                totalPrice += addonModel.price!!.toDouble()
            }
        }


        //Size
        totalPrice += Common.FOOD_SELECTED!!.userSelectedSize!!.price!!.toDouble()

        displayPrice = totalPrice * number_button!!.number!!.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price?.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View?) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())

        (activity as AppCompatActivity).supportActionBar!!.title = Common.FOOD_SELECTED!!.name

        app_bar_layout = root?.findViewById(R.id.app_bar_layout) as AppBarLayout
        img_food = root.findViewById(R.id.img_food) as ImageView
        btnCard = root.findViewById(R.id.btnCard) as CounterFab
        btn_rating = root.findViewById(R.id.btn_rating) as FloatingActionButton
        nestedScrollView = root.findViewById(R.id.nestedScrollView) as NestedScrollView
        food_name = root.findViewById(R.id.food_name) as TextView
        food_price = root.findViewById(R.id.food_price) as TextView
        number_button = root.findViewById(R.id.number_button) as ElegantNumberButton
        rattingBar = root.findViewById(R.id.rattingBar) as RatingBar
        food_description = root.findViewById(R.id.food_description) as TextView
        rdi_group_size = root.findViewById(R.id.rdi_group_size) as RadioGroup
        img_add_addon = root.findViewById(R.id.img_add_addon) as ImageView
        chip_group_user_selected_addon =
            root.findViewById(R.id.chip_group_user_selected_addon) as ChipGroup
        btnShowComment = root.findViewById(R.id.btnShowComment) as Button

        addonbottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = layout_addon_display.findViewById(R.id.chip_group_addon) as ChipGroup
        edt_search = layout_addon_display.findViewById(R.id.edt_search) as EditText
        addonbottomSheetDialog?.setContentView(layout_addon_display)

        addonbottomSheetDialog?.setOnDismissListener { dialog ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

        //Event

        img_add_addon!!.setOnClickListener {
            if (Common.FOOD_SELECTED!!.addon != null) {
                displayAddonList();
                addonbottomSheetDialog!!.show();
            }
        }

        btn_rating!!.setOnClickListener {
            showDialogRating()
        }

        btnShowComment!!.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentFragment")
        }

        //Commande
        btnCard!!.setOnClickListener { v ->
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid
            cartItem.userPhone = Common.currentUser!!.phone

            cartItem.foodId = Common.FOOD_SELECTED!!.id
            cartItem.foodName = Common.FOOD_SELECTED!!.name
            cartItem.foodImage = Common.FOOD_SELECTED!!.image
            cartItem.foodPrice = Common.FOOD_SELECTED!!.price!!.toDouble()
            cartItem.foodQuantity = number_button!!.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(
                Common.FOOD_SELECTED!!.userSelectedAddon,
                Common.FOOD_SELECTED!!.userSelectedSize
            )

            if (Common.FOOD_SELECTED!!.userSelectedAddon != null)
                cartItem.foodAddon = Gson().toJson(Common.FOOD_SELECTED!!.userSelectedAddon)
            else
                cartItem.foodAddon = "Default"

            if (Common.FOOD_SELECTED!!.userSelectedSize != null)
                cartItem.foodSize = Gson().toJson(Common.FOOD_SELECTED!!.userSelectedSize)
            else
                cartItem.foodSize = "Default"

            //insert, update
            cartDataSource!!.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId!!, cartItem.foodAddon!!, cartItem.foodSize!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB == cartItem) {
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity =
                                cartItem.foodQuantity!! + cartItem.foodQuantity!!

                            cartDataSource!!.updateCartItems(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            "Mise a jour Panier Successe",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }

                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "Mise a jour Panier " + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })

                        } else {
                            compositeDisposable.add(cartDataSource!!.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(
                                        context,
                                        "Ajoute Panier Successe",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(CounterCartEvent(true))
                                }, { t: Throwable? ->
                                    Toast.makeText(
                                        context,
                                        "Panier ajoute Error" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            )
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(cartDataSource!!.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(
                                        context,
                                        "Ajoute Panier Successe",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(CounterCartEvent(true))
                                }, { t: Throwable? ->
                                    Toast.makeText(
                                        context,
                                        "Panier ajoute Error" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            )
                        } else Toast.makeText(context, "[Get Cart]"+e.message, Toast.LENGTH_SHORT).show()

                    }

                })
        }

    }

    private fun displayAddonList() {
        if (Common.FOOD_SELECTED!!.addon!!.size > 0) {
            chip_group_addon!!.clearCheck();
            chip_group_addon!!.removeAllViews();

            edt_search!!.addTextChangedListener(this)

            for (addonModel in Common.FOOD_SELECTED!!.addon!!) {

                val chip = layoutInflater.inflate(R.layout.layout_addon_item, null) as Chip
                chip.text =
                    StringBuilder(addonModel.name!!).append("(CFA ").append(addonModel.price!!)
                        .append(")")
                chip.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (Common.FOOD_SELECTED!!.userSelectedAddon == null) Common.FOOD_SELECTED!!.userSelectedAddon =
                            ArrayList()

                        Common.FOOD_SELECTED!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }

        }

    }

    private fun displayUserSelectedAddon() {
        if (Common.FOOD_SELECTED!!.userSelectedAddon != null &&
            Common.FOOD_SELECTED!!.userSelectedAddon!!.size > 0
        ) {

            chip_group_user_selected_addon!!.removeAllViews()

            for (addonModel in Common.FOOD_SELECTED!!.userSelectedAddon!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete_icon, null) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(").append(addonModel.price)
                    .append(" CFA)")
                chip.isClickable = false
                chip.setOnCloseIconClickListener { v ->
                    chip_group_user_selected_addon!!.removeView(v)
                    Common.FOOD_SELECTED!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon?.addView(chip)
            }
        } else if (Common.FOOD_SELECTED!!.userSelectedAddon!!.size == 0)
            chip_group_user_selected_addon!!.removeAllViews()

    }

    private fun showDialogRating() {
        val builder = AlertDialog.Builder(requireContext());
        builder.setTitle("Notez le  Plat");
        builder.setMessage("Veillez noter le Plat");
        val itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);
        val ratingBar = itemView.findViewById(R.id.ratting_bar) as RatingBar
        val edt_comment = itemView.findViewById(R.id.edt_comment) as EditText
        builder.setView(itemView)

        builder.setNegativeButton("Annuler") { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton("OK") { dialog, which ->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser?.name
            commentModel.uid = Common.currentUser?.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = serverTimeStamp

            foodDetailViewModel.setCommentModel(commentModel)

        }


        dialog = builder.create();
        dialog?.show()
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        chip_group_addon!!.clearCheck();
        chip_group_addon!!.removeAllViews();
        for (addonModel in Common.FOOD_SELECTED!!.addon!!) {
            if (addonModel.name!!.toLowerCase().contains(s.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_addon_item, null) as Chip
                chip.text =
                    StringBuilder(addonModel.name!!).append("(CFA ").append(addonModel.price!!)
                        .append(")")
                chip.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (Common.FOOD_SELECTED!!.userSelectedAddon == null) Common.FOOD_SELECTED!!.userSelectedAddon =
                            ArrayList()

                        Common.FOOD_SELECTED!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}
