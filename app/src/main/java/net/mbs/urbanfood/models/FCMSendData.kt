package net.mbs.urbanfood.models

class FCMSendData {
    var to:String?=null
    var data:Map<String,String>?=null

    constructor(to: String?, data: Map<String, String>) {
        this.to = to
        this.data = data
    }
}
