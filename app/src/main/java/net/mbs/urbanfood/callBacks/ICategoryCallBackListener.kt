package net.mbs.urbanfood.callBacks

import net.mbs.urbanfood.models.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoryModels: List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}