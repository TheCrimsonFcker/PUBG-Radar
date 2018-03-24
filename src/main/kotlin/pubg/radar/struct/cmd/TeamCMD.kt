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
        16 -> {
          val playerLocation = propertyVector100()
        }
        17 -> {
          val playerRotation = readRotationShort()
        }
        18 -> {
          val playerName = propertyString()
          team[playerName] = playerName
        }
        19   ->
        {//Health
           val health = readUInt8()
           val a = health
        }
        20   ->
        {//HealthMax
           val HealthMax = readUInt8()
           val a = HealthMax
        }
        21   ->
        {//GroggyHealth
           val GroggyHealth = readUInt8()
           val a = GroggyHealth
        }
        22   ->
        {//GroggyHealthMax
           val GroggyHealthMax = readUInt8()
           val a = GroggyHealthMax
        }
        23   ->
        {//MapMarkerPosition
           val MapMarkerPosition = readVector2D()
           teamMapMarkerPosition[actor.netGUID] = MapMarkerPosition
           //println("TeamCMD: ${actor.netGUID}: ${teamMapMarkerPosition[actor.netGUID]}")
        }
        24   ->
        {//bIsDying
           val bIsDying = readBit()
           val a = bIsDying
        }
        25   ->
        {//bIsGroggying
           val bIsGroggying = readBit()
           val a = bIsGroggying
        }
        26   ->
        {//bQuitter
           val bQuitter = readBit()
           val a = bQuitter
        }
        27   ->
        {//bShowMapMarker
           val bShowMapMarker = readBit()
           teamShowMapMarker[actor.netGUID] = bShowMapMarker
           //println("TeamCMD: ${actor.netGUID}: $bShowMapMarker")
        }
        28   ->
        {//TeamVehicleType
           val TeamVehicleType = readInt(3)
           val a = TeamVehicleType
        }
        29   ->
        {//BoostGauge
           val BoostGauge = readFloat()
           val a = BoostGauge
        }
        30   ->
        {//MemberNumber
           val MemberNumber = readInt8()
           teamMemberNumber[actor.netGUID] = MemberNumber
           //println("TeamCMD: ${actor.netGUID}: $MemberNumber")
        }
        31   ->
        {//UniqueId
           val UniqueId = readString()
           val a = UniqueId
        }
        else -> return false
      }
      return true
    }
  }
}