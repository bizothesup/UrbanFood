package net.mbs.urbanfood.ui.view_order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mbs.urbanfood.models.OrderModel


class ViewOrderViewModel : ViewModel() {
      var orderMutableList: MutableLiveData<List<OrderModel>>? =null

    init {
        orderMutableList = MutableLiveData()
    }

    fun setOrderMutableList(orderModelList:List<OrderModel>){
        orderMutableList!!.value = orderModelList
    }
}
