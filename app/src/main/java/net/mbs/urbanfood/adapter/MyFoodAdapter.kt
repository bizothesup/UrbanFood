package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.Scheduler
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.mbs.urbanfood.R
import net.mbs.urbanfood.callBacks.IRecyclerClickListener
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.database.CartDataSource
import net.mbs.urbanfood.database.CartDatabase
import net.mbs.urbanfood.database.CartItem
import net.mbs.urbanfood.database.LocalCartDataSource
import net.mbs.urbanfood.eventBus.CounterCartEvent
import net.mbs.urbanfood.eventBus.FoodClick
import net.mbs.urbanfood.models.FoodModel
import org.greenrobot.eventbus.EventBus

class MyFoodAdapter(
    internal var context: Context,
    internal var foodModels: List<FoodModel>
) :
    RecyclerView.Adapter<MyFoodAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }

    fun onStop(){
        if(compositeDisposable!=null)
            compositeDisposable.clear()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var txt_food_name: TextView? = null
        var txt_food_price: TextView? = null
        var img_food_image: ImageView? = null
        var img_fav: ImageView? = null
        var img_img_quick_cart: ImageView? = null
        internal var listener: IRecyclerClickListener? = null

        fun setListener(listener: IRecyclerClickListener) {
            this.listener = listener
        }


        init {
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            img_food_image = itemView.findViewById(R.id.img_food_image) as ImageView
            img_fav = itemView.findViewById(R.id.img_fav) as ImageView
            img_img_quick_cart = itemView.findViewById(R.id.img_quick_cart)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener?.onItemClickListener(v!!, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_food_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return foodModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodModels.get(position).image).into(holder.img_food_image!!);
        holder.txt_food_price!!.text = foodModels.get(position).price!!.toString()
        holder.txt_food_name!!.text = foodModels.get(position).name!!

        //EventBus
        holder.setListener(object : IRecyclerClickListener {
            override fun onItemClickListener(view: View, position: Int) {
                Common.FOOD_SELECTED = foodModels.get(position)
                Common.FOOD_SELECTED!!.key = position.toString()
                EventBus.getDefault().postSticky(FoodClick(true, foodModels.get(position)))
            }

        })

        holder.img_img_quick_cart!!.setOnClickListener {
            val cartItem = CartItem()

            cartItem.uid = Common.currentUser!!.uid
            cartItem.userPhone = Common.currentUser!!.phone

            cartItem.foodId = foodModels.get(position).id
            cartItem.foodName = foodModels.get(position).name
            cartItem.foodImage = foodModels.get(position).image
            cartItem.foodPrice = foodModels.get(position).price!!.toDouble()
            cartItem.foodQuantity = 1
            cartItem.foodExtraPrice = 0.toDouble()
            cartItem.foodAddon = "Default"
            cartItem.foodSize = "Default"

            //Inserete database

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId!!, cartItem.foodAddon!!, cartItem.foodSize!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity =
                                cartItem.foodQuantity!! + cartItem.foodQuantity!!

                            cartDataSource.updateCartItems(cartItemFromDB)
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
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
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
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
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
}