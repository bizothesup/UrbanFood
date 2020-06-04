package net.mbs.urbanfood.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.FoodModel

class FoodViewModel : ViewModel() {
    private  var foodMutableList:MutableLiveData<List<FoodModel>>?=null

    val foodList:LiveData<List<FoodModel>>
        get() {
            if(foodMutableList == null){
                foodMutableList = MutableLiveData()
            }
            foodMutableList!!.value = Common.CATEGORY_SELECTED?.foods
            return foodMutableList!!
        }
}