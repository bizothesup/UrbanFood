package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import io.reactivex.disposables.CompositeDisposable
import net.mbs.urbanfood.R
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.database.CartDataSource
import net.mbs.urbanfood.database.CartDatabase
import net.mbs.urbanfood.database.CartItem
import net.mbs.urbanfood.database.LocalCartDataSource
import net.mbs.urbanfood.eventBus.UpdateItemInCart
import org.greenrobot.eventbus.EventBus

class MyCartAdapter(internal var context: Context,
                    internal var cartItems:List<CartItem> ):RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    internal var compositeDisposable:CompositeDisposable
    internal var cartDataSource:CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img_cart : ImageView?=null
        var txt_food_price:TextView?=null
        var txt_food_name:TextView?=null
        var numberButton:ElegantNumberButton?=null

        init {
            img_cart = itemView.findViewById(R.id.img_cart) as ImageView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            numberButton = itemView.findViewById(R.id.number_button) as ElegantNumberButton
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun getItemCount(): Int {
       return cartItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItems[position].foodImage)
            .into(holder.img_cart!!)
        holder.txt_food_name!!.text =cartItems[position].foodName
        holder.txt_food_price!!.text=Common.formatPrice(cartItems[position].foodPrice!! +cartItems[position].foodExtraPrice!!)
        holder.numberButton!!.number=cartItems[position].foodQuantity!!.toString()

        holder.numberButton!!.setOnValueChangeListener{view, oldValue, newValue ->
            cartItems[position].foodQuantity = newValue

            EventBus.getDefault().postSticky(UpdateItemInCart(cartItems[position]))
        }

    }

    fun getItemAtPosition(pos: Int): CartItem {
        return cartItems[pos]
    }
}