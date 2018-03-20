package pubg.radar.struct.cmd

import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemToItem
import pubg.radar.struct.*
import pubg.radar.struct.cmd.*

object AirDropComponentCMD {
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>):Boolean {
    with(bunch) {
      //println("${actor.netGUID} $waitingHandle")
      when (waitingHandle) {
        6 -> {
            repMovement(actor)
            airDropLocation[actor.netGUID]=actor.location
            //println("$airdropID")
        }
        16 -> {
          val (itemguid, item) = readObject()
          droppedItemToItem[actor.netGUID] = itemguid
        }
        else -> return ActorCMD.process(actor,bunch,repObj,waitingHandle,data)
      }
      return true
    }
  }
} 