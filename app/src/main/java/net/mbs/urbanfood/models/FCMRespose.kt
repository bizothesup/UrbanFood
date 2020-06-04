package net.mbs.urbanfood.models

class FCMRespose {
    var multicast_id:Long?=0.toLong()
    var success:Int?=0
    var failure:Int?=0
    var canocial_ids:Int?=0
  var results:List<FCMResult>?=null
   var message_id:Long?=0.toLong()

}
