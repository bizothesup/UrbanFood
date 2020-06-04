package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.OrderModel

interface ILoadTimeFromFireBaseListener {
    fun onLoadTimeSucess(orderModel: OrderModel, estimateTimeInMs:Long )
    fun onLoadTimeFailed(message:String)
}