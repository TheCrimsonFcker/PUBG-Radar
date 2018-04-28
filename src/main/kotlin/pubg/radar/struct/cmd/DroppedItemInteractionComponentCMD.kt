package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.struct.*
import pubg.radar.struct.cmd.CMD.propertyVector

object DroppedItemInteractionComponentCMD {
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        1 -> propertyBool()
        2 -> propertyBool()

        3 -> {
          val arraySize=readUInt16()
          var index=readIntPacked()
          while (index != 0) {
            val (netguid,obj)=readObject()
//                  println("$netguid,$obj")
            index=readIntPacked()
          }
        }

        4 -> propertyObject()
        5 -> propertyName()
        6 -> propertyBool()
        7 -> propertyBool()
        8 -> propertyBool()
        9 -> propertyBool()
        10 -> propertyBool()
        11 -> propertyBool()
        12 -> propertyVector()
        13 -> readRotationShort()
        14 -> propertyVector()

        15 -> {
          val (itemGUID, _) = readObject()
          val (loc, _) = droppedItemLocation[itemGUID] ?: return true
          droppedItemCompToItem[repObj!!.outerGUID] = itemGUID
//          print("item loc:$loc ->")
          loc.add(data["relativeLocation"] as Vector2)
//          println("item loc:$loc")
        }
        else -> return false
      }
    }
    return true
  }
}