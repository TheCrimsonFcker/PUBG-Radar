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
    isGroggying.clear()
    isReviving.clear()
  }
  
  val actorWithPlayerState = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  val playerStateToActor = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
  val actorHealth = ConcurrentHashMap<NetworkGUID, Float>()
  val actorGroggyHealth = ConcurrentHashMap<NetworkGUID, Float>()
  val isGroggying = ConcurrentHashMap<NetworkGUID, Boolean>()
  val isReviving = ConcurrentHashMap<NetworkGUID, Boolean>()
  
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        1 -> if (readBit()) {//bHidden
          visualActors.remove(actor.netGUID)
          bugln { ",bHidden id$actor" }
        }
        2 -> if (!readBit()) {// bReplicateMovement
          if (!actor.isVehicle) {
            visualActors.remove(actor.netGUID)
          }
          bugln { ",!bReplicateMovement id$actor " }
        }
        3 -> if (readBit()) {//bTearOff
          visualActors.remove(actor.netGUID)
          bugln { ",bTearOff id$actor" }
        }
        4 -> {
          val role = readInt(ROLE_MAX)
          val b = role
        }
        5 -> {
          val (netGUID, obj) = readObject()
          actor.owner = if (netGUID.isValid()) netGUID else null
          bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
        }
        6 -> {
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
        7 -> {
          val (a, obj) = readObject()
          val attachTo = if (a.isValid()) {
            actors[a]?.attachChildren?.put(actor.netGUID, actor.netGUID)
            a
          } else null
          if (actor.attachParent != null)
            actors[actor.attachParent!!]?.attachChildren?.remove(actor.netGUID)
          actor.attachParent = attachTo
          if (actor.netGUID == selfID) {
            selfAttachTo = if (attachTo != null)
              actors[actor.attachParent!!]
            else
              null
          }          
          bugln { ",attachTo [$actor---------> $a ${guidCache.getObjectFromNetGUID(a)} ${actors[a]}" }
        }
        8 -> {
          val locationOffset = propertyVector100()
          if (actor.Type == DroopedItemGroup) {
            bugln { "${actor.location} locationOffset $locationOffset" }
          }
          bugln { ",attachLocation $actor ----------> $locationOffset" }
        }
        9 -> propertyVector100()
        10 -> readRotationShort()
        11 -> {
          val attachSocket = propertyName()
        }
        12 -> {
          val (attachComponnent, attachName) = bunch.readObject()
        }
        13 -> {
          readInt(ROLE_MAX)
        }
        14 -> propertyBool()
        15 -> propertyObject()
        16 -> {
          val (playerStateGUID, playerState) = propertyObject()
          if (playerStateGUID.isValid()) {
            actorWithPlayerState[actor.netGUID] = playerStateGUID
            playerStateToActor[playerStateGUID] = actor.netGUID
          }
        }
        17 -> {//RemoteViewPitch 2
          val result = readUInt16() * shortRotationScale//pitch
        }
        18 -> {
          val result = propertyObject()
        }
      //ACharacter
        19 -> {
          val result = propertyObject()
        }
        20 -> {
          val result = propertyName()
        }
        21 -> {
          val result = propertyVector100()
        }
        22 -> {
          val Rotation = readRotationShort()
        }//propertyRotator()
        23 -> {
          val result = propertyBool()
        }
        24 -> {
          val result = propertyBool()
        }
        25 -> {
          val result = propertyBool()
        }
        26 -> {
          val result = propertyFloat()
        }
        27 -> {
          val ReplicatedServerLastTransformUpdateTimeStamp = propertyFloat()
        }
        28 -> {
          val result = propertyByte()
        }
        29 -> {
          val result = propertyBool()
        }
        30 -> {
          val result = propertyFloat()
        }
        31 -> {
          val result = propertyInt()
        }
      //struct FRepRootMotionMontage RepRootMotion;
        32 -> {
          val result = propertyBool()
        }
        33 -> {
          val result = propertyObject()
        }
        34 -> {
          val result = propertyFloat()
        }
        35 -> {
          val result = propertyVector100()
        }
        36 -> {
          val result = readRotationShort()
        }//propertyRotator()
        37 -> {
          val result = propertyObject()
        }
        38 -> {
          val result = propertyName()
        }
        39 -> {
          val result = propertyBool()
        }
        40 -> {
          val result = propertyBool()
        }
        41 -> {//player
          val bHasAdditiveSources = readBit()
          val bHasOverrideSources = readBit()
          val lastPreAdditiveVelocity = propertyVector10()
          val bIsAdditiveVelocityApplied = readBit()
          val flags = readUInt8()
        }
        42 -> {
          val result = propertyVector10()
        }
        43 -> {
          val result = propertyVector10()
        }
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
        45 -> {
          val remote_CastAnim = readInt(8)
        }
        46 -> {
          val result = propertyInt()
        }
        47 -> {
          val result = propertyFloat()
        }
        48 -> {
          val result = propertyObject()
        }
        49 -> {
          val result = propertyObject()
        }
        50 -> {
          val CharacterState = propertyByte()
        }
        51 -> {
          val bIsScopingRemote = propertyBool()
        }
        52 -> {
          val bIsAimingRemote = propertyBool()
        }
        53 -> {
          val bIsFirstPersonRemote = propertyBool()
        }
        54 -> {
          val bIsInVehicleRemote = propertyBool()
        }
        55 -> {
          val SpectatedCount = propertyInt()
        }
        /*
        56 -> {
          val (id, team) = propertyObject()
        }
        */
        57 -> {
          val ActualDamage = propertyFloat()
        }
        58 -> {
          val damageType = propertyObject()
        }
        59 -> {
          val PlayerInstigator = propertyObject()
        }
        60 -> {
          val DamageOrigin = propertyVectorQ()
        }
        61 -> {
          val RelHitLocation = propertyVectorQ()
        }
        62 -> {
          val result = propertyName()
          val b = result
        }
        63 -> {
          val DamageMaxRadius = propertyFloat()
        }
        64 -> {
          val ShotDirPitch = propertyByte()
        }
        65 -> {
          val ShotDirYaw = propertyByte()
        }
        66 -> {
          val result = propertyBool()
        }
        67 -> {
          val result = propertyBool()
        }
        68 -> {
          val bKilled = propertyBool()
        }
        69 -> {
          val EnsureReplicationByte = propertyByte()
        }
        70 -> {
          val AttackerWeaponName = propertyName()
        }
        71 -> {
          val AttackerLocation = propertyVector()
        }
        72 -> {
          val TargetingType = readInt(3)
          val a = TargetingType
        }
        73 -> {
          val reviveCastingTime = propertyFloat()
          val a = reviveCastingTime
        }
        74 -> {
          val result = propertyBool()
          val b = result
        }
        75 -> {
          val result = propertyBool()
          val b = result
        }
        76 -> {
          val result = propertyBool()
          val b = result
        }
        77 -> {
          val result = propertyBool()
          val b = result
        }
        78 -> {
          val result = propertyBool()
          val b = result
        }
        79 -> {
          val result = propertyBool()
          val b = result
        }
        80 -> {
          val result = propertyBool()
          val b = result
        }
        81 -> {
          val result = propertyBool()
          val b = result
        }
        82 -> {
          val bIsGroggying=propertyBool()
          val b = bIsGroggying
          isGroggying[actor.netGUID] = bIsGroggying
          //println("ActorCMD 82 bIsGroggying ? $b")
        }
        83 -> {
          val bIsThirdPerson=propertyBool()
        }
        84 -> {
          val bIsReviving=propertyBool()
          val b=bIsReviving
          //actorGroggyHealth[actor.netGUID] = GroggyHealth
          isReviving[actor.netGUID] = bIsReviving
          
        }
        85 -> {
          val bIsWeaponObstructed = propertyBool()
        }
        86 -> {
          val bIsCoatEquipped = propertyBool()
        }
        87 -> {
          val result = propertyBool()
          val b = result
        }
        88 -> {
          val result = propertyBool()
          val b = result
        }
        89 -> {
          val result = propertyBool()
          val b = result
        }
        90 -> {
          val result = readRotationShort()//propertyRotator()
          val b = result
        }
        91 -> {
          val AimOffsets = propertyVectorNormal()
          val b = AimOffsets
        }
        92 -> {
          val result = readObject()
          val b = result
        }
        93 -> {
          val result = propertyBool()
          val b = result
        }
        94 -> {
          val result = propertyBool()
          val b = result
        }
        95 -> {
          val health = propertyFloat()
          actorHealth[actor.netGUID] = health
        }
        96 -> {
          val healthMax = propertyFloat()
        }
        97 -> {
          val GroggyHealth = propertyFloat()
          actorGroggyHealth[actor.netGUID] = GroggyHealth
        }
        98 -> {
          val GroggyHealthMax = propertyFloat()
        }
        99 -> {
          val BoostGauge = propertyFloat()
        }
        100 -> {
          val BoostGaugeMax = propertyFloat()
        }
        101 -> {
          val ShoesSoundType = readInt(8)
          val b = ShoesSoundType
        }
        102 -> {
          val result = readObject()
          val b = result
        }
        103 -> {
          val result = propertyBool()
          val b = result
        }
        104 -> {
          val result = readInt(4)
          val b = result
        }
        105 -> {
          val result = propertyBool()
          val b = result
        }
        106 -> {
          val result = propertyBool()
          val b = result
        }
        107 -> {
          val result = propertyBool()
          val b = result
        }
        else -> return false
      }
      return true
    }
  }
}