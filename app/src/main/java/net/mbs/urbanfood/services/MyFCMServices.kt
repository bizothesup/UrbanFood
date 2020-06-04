package net.mbs.urbanfood.services


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import net.mbs.urbanfood.commons.Common
import java.util.*

class MyFCMServices : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val dataRecv = remoteMessage.data
        if(dataRecv != null){
            Common.showNotification(this, Random().nextInt(),
                dataRecv.get(Common.NOTI_TITLE),
                dataRecv.get(Common.NOTI_CONTENT),
                null)
        }
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    Common.updateToken(this,token)
    }
}