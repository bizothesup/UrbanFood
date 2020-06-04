package net.mbs.urbanfood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.mbs.urbanfood.R
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.OrderModel
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter(internal var context: Context,
                     internal var orderModels:MutableList<OrderModel>
):
    RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {

    var calendar: Calendar
    var simpleFormat:SimpleDateFormat

    init {
        calendar = Calendar.getInstance()
        simpleFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
       var  img_order : ImageView
        var txt_order_date :TextView
        var txt_order_number :TextView
        var txt_order_comment :TextView
        var txt_order_status :TextView
        init {
              img_order = itemView.findViewById(R.id.img_order) as ImageView
             txt_order_date =itemView.findViewById<TextView>(R.id.txt_order_date)
            txt_order_number =itemView.findViewById<TextView>(R.id.txt_order_number)
            txt_order_comment =itemView.findViewById<TextView>(R.id.txt_order_comment)
            txt_order_status =itemView.findViewById<TextView>(R.id.txt_order_status)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layou_order_item,parent,false))
    }

    override fun getItemCount(): Int {
        return orderModels.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(orderModels.get(position).cartItemList!!.get(0).foodImage)
                .into(holder.img_order)
        calendar.timeInMillis = orderModels.get(position).createDate!!
        val date =  Date(orderModels.get(position).createDate!!);
        holder.txt_order_date.text =StringBuilder(Common.getDaystoWeek(calendar.get(Calendar.DAY_OF_WEEK))).append(" ")
            .append(simpleFormat.format(date))
        holder.txt_order_number.setText(StringBuilder("Numéro Commande: ").append(orderModels.get(position).key));
        holder.txt_order_comment.setText( StringBuilder("Info complémentaire: ").append(orderModels.get(position).comment));
        holder.txt_order_status.setText(StringBuilder("Status: ").append(Common.convertStatusToText(orderModels.get(position).status)))
    }

    fun getItemAtPosition(pos: Int): OrderModel {
        return orderModels[pos]
    }

    fun setItemAtPosition(pos: Int, orderModel: OrderModel) {
        orderModels[pos] = orderModel
    }
}

