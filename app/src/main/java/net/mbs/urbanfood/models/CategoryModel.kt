package net.mbs.urbanfood.models

class CategoryModel {
    var menu_id:String?=null
    var name:String?=null
    var image:String?=null
    var foods:List<FoodModel>?=null

    constructor()
    constructor(menu_id: String?, name: String?, image: String?, foods: List<FoodModel>?) {
        this.menu_id = menu_id
        this.name = name
        this.image = image
        this.foods = foods
    }


}