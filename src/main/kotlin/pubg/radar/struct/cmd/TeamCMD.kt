package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.*
import pubg.radar.struct.*
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector100
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
import java.util.concurrent.ConcurrentHashMap

object TeamCMD: GameListener {
  val team = ConcurrentHashMap<String, String>()
  val teamMapMarkerPosition = ConcurrentHashMap<NetworkGUID, Vector2>()
  val teamShowMapMarker = ConcurrentHashMap<NetworkGUID, Boolean>()
  val teamMemberNumber = ConcurrentHashMap<NetworkGUID, Int>()
  
  init {
    register(this)
  }
  
  override fun onGameOver() {
    team.clear()
  }
  
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      //      println("${actor.netGUID} $waitingHandle")
      when (waitingHandle) {
        5 -> {
          val (netGUID, obj) = readObject()
          actor.owner = if (netGUID.isValid()) netGUID else null
          bugln { " owner: [$netGUID] $obj ---------> beOwned:$actor" }
        }
        16 -> {//bIsDying
            val bIsDying = readBit()
            val a = bIsDying
        }
        17 -> {//bIsGroggying
            val bIsGroggying = readBit()
            val a = bIsGroggying
        }
        18 -> {//BoostGauge
          val BoostGauge = readFloat()
          val a = BoostGauge
        }
        19 -> {//bQuitter
          val bQuitter = readBit()
          val a = bQuitter
        }
        20 -> {//bShowMapMarker
          val bShowMapMarker = readBit()
          teamShowMapMarker[actor.netGUID] = bShowMapMarker
          //println("TeamCMD: ${actor.netGUID}: $bShowMapMarker")
        }
        21 -> {//bUsingSquadInTeam
          val bUsingSquadInTeam = propertyBool()
        }
        22 -> {//GroggyHealth
          val GroggyHealth = readUInt8()
          val a = GroggyHealth
        }
        23 -> {//GroggyHealthMax
          val GroggyHealthMax = readUInt8()
          val a = GroggyHealthMax
        }
        24 -> {//Health
          val health = readUInt8()
          val a = health
        }
        25 -> {//HealthMax
          val HealthMax = readUInt8()
          val a = HealthMax
        }
        26 -> {//MapMarkerPosition
          val MapMarkerPosition = readVector2D()
          teamMapMarkerPosition[actor.netGUID] = MapMarkerPosition
          //println("TeamCMD: ${actor.netGUID}: ${teamMapMarkerPosition[actor.netGUID]}")
        }
        27 -> {//MemberNumber
          val MemberNumber = readInt8()
          teamMemberNumber[actor.netGUID] = MemberNumber
          //println("TeamCMD: ${actor.netGUID}: $MemberNumber")
        }
        28 -> {
          val playerLocation = propertyVector100()
        }
        29 -> {
          val playerName = propertyString()
          team[playerName] = playerName
        }
        30 -> {
          val playerRotation = readRotationShort()
        }
        31 -> {//SquadIndex
          val SquadIndex = readInt8()
        }
        32 -> {//SquadMemberIndex
          val SquadMemberIndex = readInt8()
        }
        33 -> {//TeamVehicleType
           val TeamVehicleType = readInt(7)
           val a = TeamVehicleType
        }
        34 -> {//UniqueId
           val UniqueId = readString()
           val a = UniqueId
        }
        else -> return false
      }
      return true
    }
  }
}