package net.mbs.urbanfood.ui.foodDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mbs.urbanfood.commons.Common
import net.mbs.urbanfood.models.CommentModel
import net.mbs.urbanfood.models.FoodModel

class FoodDetailViewModel : ViewModel() {


    private  var foodMutable: MutableLiveData<FoodModel>?=null
    private  var commentMutable: MutableLiveData<CommentModel>?=null

    init {
        commentMutable = MutableLiveData()
    }


    fun getMutableLiveDataFood():MutableLiveData<FoodModel>{
        if(foodMutable==null)
            foodMutable= MutableLiveData()
        foodMutable?.value=Common.FOOD_SELECTED
        return foodMutable!!
    }

    fun setFoodModel(foodModel: FoodModel){
        if(foodMutable != null)
            foodMutable?.value = foodModel
    }

    fun setCommentModel(commentModel: CommentModel) {
        if(commentMutable !=null )
            commentMutable?.value =commentModel
    }

    fun getMutableLiveDataComment():MutableLiveData<CommentModel>{
        if(commentMutable==null)
            commentMutable = MutableLiveData()
        return commentMutable!!
    }


}
