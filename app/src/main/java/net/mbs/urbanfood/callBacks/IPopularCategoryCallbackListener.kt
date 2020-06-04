package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.PopularCategoryModel

interface IPopularCategoryCallbackListener {
    fun onPopularLoadSuccess(popularCategoryModels: List<PopularCategoryModel>)
    fun onPopularLoadFailed(message:String)
}