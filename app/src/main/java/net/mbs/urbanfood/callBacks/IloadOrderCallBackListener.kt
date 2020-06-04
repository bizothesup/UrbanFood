package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.CategoryModel
import net.mbs.urbanfood.models.OrderModel

interface IloadOrderCallBackListener {
    fun onOrderLoadSuccess(orderModel: List<OrderModel>)
    fun onOrderLoadFailed(message:String)
}