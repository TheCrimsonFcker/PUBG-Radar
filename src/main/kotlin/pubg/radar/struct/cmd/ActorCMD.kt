package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.*
import pubg.radar.deserializer.*
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.sniffer.Sniffer.Companion.selfCoordsSniffer
import pubg.radar.struct.*
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.NetGUIDCache.Companion.guidCache
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyVector
import pubg.radar.struct.cmd.CMD.propertyVector10
import pubg.radar.struct.cmd.CMD.propertyVector100
import pubg.radar.struct.cmd.CMD.propertyVectorNormal
import pubg.radar.struct.cmd.CMD.propertyVectorQ
import pubg.radar.struct.cmd.CMD.repMovement
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import java.util.concurrent.ConcurrentHashMap

var selfDirection = 0f
val selfCoords = Vector2()
var selfAttachTo: Actor? = null

object ActorCMD: GameListener {
  init {
    register(this)
  }
  
  override fun onGameOver() {
    actorWithPlayerState.clear()
    playerStateToActor.clear()
    actorHealth.clear()
    actorGroggyHealth.clear()
    spectatedCount.clear()
    //reviveCastingTime.clear()
    isGroggying.clear()
    isReviving.clear()
  }
  
  val actorWithPlayerState = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  val playerStateToActor = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  val actorHealth = ConcurrentHashMap<NetworkGUID, Float>()
  val actorGroggyHealth = ConcurrentHashMap<NetworkGUID, Float>()
  val spectatedCount = ConcurrentHashMap<NetworkGUID, Int>()

  //val reviveCastingTime = ConcurrentHashMap<NetworkGUID, Float>()
  val isGroggying = ConcurrentHashMap<NetworkGUID, Boolean>()
  val isReviving = ConcurrentHashMap<NetworkGUID, Boolean>()
  
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        1 -> propertyObject()
        2 -> propertyObject()
        3 -> propertyName()
        4 -> propertyVector100()
        5 -> propertyVector100()
        6 -> readRotationShort()
        7 -> {
          val bCanBeDamaged = readBit()
          //println("bCanBeDamaged=$bCanBeDamaged")
        }
        8 -> if (readBit()) {//bHidden
          visualActors.remove(actor.netGUID)
          bugln { ",bHidden id$actor" }
        }
        9 -> if (!readBit()) {// bReplicateMovement
          if (!actor.isVehicle) {
            visualActors.remove(actor.netGUID)
          }
          bugln { ",!bReplicateMovement id$actor " }
        }
        10 -> if (readBit()) {//bTearOff
          visualActors.remove(actor.netGUID)
          bugln { ",bTearOff id$actor" }
        }
        11 -> {
          val (instigatorGUID, instigator) = propertyObject()
        }
        12 -> {
          val (netGUID, obj) = readObject()
          actor.owner = if (netGUID.isValid()) netGUID else null
          bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
        }
        13 -> readInt(ROLE_MAX)
        14 -> {
          repMovement(actor)
          with(actor) {
            when (Type) {
              AirDrop -> airDropLocation[netGUID] = location
              Other -> {
              }
              else -> visualActors[netGUID] = this
            }
          }
        }
        15 -> {
          val role = readInt(ROLE_MAX)
          val b = role
        }

        //APawn
        16 -> {
          val (controllerGUID, controller) = propertyObject()
        }
        17 -> {
          val (playerStateGUID, playerState) = propertyObject()
          if (playerStateGUID.isValid()) {
            actorWithPlayerState[actor.netGUID] = playerStateGUID
            playerStateToActor[playerStateGUID] = actor.netGUID
          }
        }
        18 -> readUInt16() * shortRotationScale//RemoteViewPitch

      //ACharacter
        19 -> propertyFloat()
        20 -> propertyBool()
        21 -> propertyInt()
        22 -> propertyFloat()

        //Struct FBasedMovementInfo ReplicatedBasedMovement
        23 -> propertyName()
        24 -> propertyBool()
        25 -> propertyBool()
        26 -> propertyBool()
        27 -> propertyVector100()
        28 -> propertyObject()
        29 -> readRotationShort()

        30 -> readUInt8()
        31 -> propertyFloat()

        //struct FRepRootMotionMontage RepRootMotion;
        32 -> propertyVector10()
        33 -> propertyObject()
        34 -> {//player
          val bHasAdditiveSources = propertyBool();
          val bHasOverridesources = propertyBool();
          val LastPreAdditiveVelocity = propertyVector10(); //FVector_NetQuantize10
          val IsAdditiveVelocityApplied = propertyBool();
          val LastAccumulatedSettingsFlags = readUInt8(); //struct ENGINE_API FRootMotionSourceSettings
        }
        35 -> propertyBool()
        36 -> propertyBool()
        37 -> propertyBool()
        38 -> propertyVector10()
        39 -> propertyVector100()
        40 -> propertyObject()
        41 -> propertyName()
        42 -> propertyFloat()
        43 -> readRotationShort()

      //AMutableCharacter
        44 -> {
          val arrayNum = readUInt16()
          var index = readIntPacked()
          while (index != 0) {
            val value = readUInt8()
            index = readIntPacked()
          }
        }

      //ATslCharacter
        45 -> propertyVectorNormal()
        46 -> propertyBool()
        47 -> propertyBool()
        48 -> propertyBool()
        49 -> propertyBool()
        50 -> propertyBool()
        51 -> propertyBool()
        52 -> {
          val result = propertyBool()
          //println("82: ${actor.netGUID} $result")
          isGroggying[actor.netGUID] = result
        }
        53 -> propertyBool()
        54 -> propertyBool()
        55 -> propertyBool()
        56 -> propertyBool()
        57 -> {
          isReviving[actor.netGUID] = propertyBool()
          //println("84: ${actor.netGUID} $bIsThirdPerson")
        }
        58 -> propertyBool()
        59 -> propertyBool()
        60 -> propertyBool()
        61 -> propertyBool()
        62 -> propertyBool()
        63 -> propertyFloat()
        64 -> propertyFloat()
        65 -> propertyBool()
        66 -> propertyFloat()
        67 -> propertyBool()
        68 -> propertyBool()
        69 -> propertyBool()
        70 -> propertyBool()
        71 -> propertyBool()
        72 -> propertyBool()
        73 -> propertyBool()
        74 -> readUInt8()
        75 -> readUInt8()
        76 -> {
          val GroggyHealth = propertyFloat()
          actorGroggyHealth[actor.netGUID] = GroggyHealth
        }
        77 -> propertyFloat()
        78 -> readRotationShort()
        79 -> {
          val health = propertyFloat()
          actorHealth[actor.netGUID] = health
        }
        80 -> propertyFloat()
        81 -> propertyBool()
        82 -> propertyObject()
        83 -> propertyFloat()
        84 -> propertyVector()
        85 -> propertyName()
        86 -> propertyBool()
        87 -> propertyName()
        88 -> propertyBool()
        89 -> propertyBool()
        90 -> propertyFloat()
        91 -> propertyVectorQ()
        92 -> propertyObject()
        93 -> readUInt8()
        94 -> propertyObject()
        95 -> propertyVectorQ()
        96 -> readUInt8()
        97 -> readUInt8()
        98 -> propertyObject()
        99 -> readInt(4)
        100 -> readInt(8)
        101 -> propertyFloat()
        102 -> readInt(8)
        103 -> {
          val result = propertyInt()
          spectatedCount[actor.netGUID] = result
        }
        104 -> readInt(4)
        105 -> propertyObject()
        106 -> propertyObject()
        107 -> propertyObject()
        else -> return false
      }
      return true
    }
  }
}