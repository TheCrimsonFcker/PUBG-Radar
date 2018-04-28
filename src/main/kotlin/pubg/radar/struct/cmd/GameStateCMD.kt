package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.*
import pubg.radar.struct.*
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector

object GameStateCMD: GameListener {
  init {
    register(this)
  }
  
  override fun onGameOver() {
    SafetyZonePosition.setZero()
    SafetyZoneRadius = 0f
    SafetyZoneBeginPosition.setZero()
    SafetyZoneBeginRadius = 0f
    PoisonGasWarningPosition.setZero()
    PoisonGasWarningRadius = 0f
    RedZonePosition.setZero()
    RedZoneRadius = 0f
    TotalWarningDuration = 0f
    ElapsedWarningDuration = 0f
    TotalReleaseDuration = 0f
    ElapsedReleaseDuration = 0f
    NumJoinPlayers = 0
    NumAlivePlayers = 0
    NumAliveTeams = 0
    RemainingTime = 0
    MatchElapsedMinutes = 0
    NumTeams = 0
    isTeamMatch = false
    MatchStartType = ""
    isWarMode = false
  }
  
  var TotalWarningDuration = 0f
  var ElapsedWarningDuration = 0f
  var RemainingTime = 0
  var MatchElapsedMinutes = 0
  val SafetyZonePosition = Vector2()
  var SafetyZoneRadius = 0f
  val SafetyZoneBeginPosition = Vector2()
  var SafetyZoneBeginRadius = 0f
  val PoisonGasWarningPosition = Vector2()
  var PoisonGasWarningRadius = 0f
  val RedZonePosition = Vector2()
  var RedZoneRadius = 0f
  var TotalReleaseDuration = 0f
  var ElapsedReleaseDuration = 0f
  var NumJoinPlayers = 0
  var NumAlivePlayers = 0
  var NumAliveTeams = 0
  var NumTeams = 0
  var isTeamMatch = false
  var MatchStartType = ""
  var isWarMode = false

  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      when (waitingHandle) {
        16 -> {
          val bReplicatedHasBegunPlay = propertyBool()
          val b = bReplicatedHasBegunPlay
        }
        17 -> {
          val GameModeClass = propertyObject()
          val b = GameModeClass
        }
        18 -> {
          val ReplicatedWorldTimeSeconds = propertyFloat()
          val b = ReplicatedWorldTimeSeconds
        }
        19 -> {
          val SpectatorClass = propertyObject()
          val b = SpectatorClass
        }
        20 -> {
          val ElapsedTime = propertyInt()
          val b = ElapsedTime
          //println("21 $b")
        }
        21 -> {
          val MatchState = propertyName()
          val b = MatchState
        }
        22 -> {
          val bCanKillerSpectate = propertyBool()
        }
        23 -> {
          val bCanShowLastCircleMark = propertyBool()
        }
        24 -> propertyBool()//bIsCustomGame
        25 -> {
          val bIsGasRelease = propertyBool()
        }
        26 -> {
          val bIsTeamElimination = propertyBool()
        }
        27 -> {
          val bIsTeamMatch = propertyBool()
          isTeamMatch = bIsTeamMatch
          println("TeamMatch: $isTeamMatch")
          //println("GameStateCMD 53: $b")
        }
        28 -> {
          val bIsWarMode = propertyBool()
          isWarMode = bIsWarMode
          println("GameStateCMD 60: $bIsWarMode")
        }
        29 -> propertyBool() //bIsWinnerZombieTeam
        30 -> propertyBool() //bIsWorkingBlueZone
        31 -> {
          val bIsZombieMode = propertyBool()
          println("GameStateCMD 54: $bIsZombieMode")
        }
        32 -> {
          val bShowAircraftRoute = propertyBool()
          println("GameStateCMD 59: $bShowAircraftRoute")
        }
        33 -> {
          val bShowLastCircleMark = propertyBool()
        }
        34 -> {
          val bTimerPaused = propertyBool()
          val b = bTimerPaused
        }
        35 -> propertyBool() //bUseWarRoyaleBluezone
        36 -> {
          val bUseXboxUnauthorizedDevice = propertyBool()
        }
        37 -> propertyBool() //bUsingSquadInTeam
        38 -> {
          ElapsedReleaseDuration = propertyFloat()
          val b = ElapsedReleaseDuration
          //println("45 $b")
        }
        39 -> {
          ElapsedWarningDuration = propertyFloat()
          //println("47 $ElapsedWarningDuration")
        }
        40 -> {
          val GoalScore = propertyInt()
        }
        41 -> {
          val LastCirclePosition = readVector2D()
        }
        42 -> {
          MatchElapsedMinutes = propertyInt()
        }
        43 -> {
          val MatchElapsedTimeSec = propertyFloat()
        }
        44 -> {
          val MatchId = propertyString()
          val b = MatchId
        }
        45 -> {
          val MatchShortGuid = propertyString()
          val b = MatchShortGuid
        }
        46 -> {
          val result = propertyByte()
          MatchStartType = result.toString()
          println("GameStateCMD 58: ${MatchStartType}")
        }
        47 -> {
          val NextRespawnTimeInSeconds = propertyFloat()
        }
        48 -> {
          NumAlivePlayers = propertyInt()
        }
        49 -> {
          NumAliveTeams = propertyInt()
        }
        50 -> {
          val NumAliveZombiePlayers = propertyInt()
          val b = NumAliveZombiePlayers
        }
        51 -> {
          NumJoinPlayers = propertyInt()
        }
        52 -> {
          val NumStartPlayers = propertyInt()
          val b = NumStartPlayers
        }
        53 -> {
          val NumStartTeams = propertyInt()
          val b = NumStartTeams
        }
        54 -> {
          val NumTeams = propertyInt()
          val b = NumTeams
        }
        55 -> {
          val pos = propertyVector()
          PoisonGasWarningPosition.set(pos.x, pos.y)
        }
        56 -> {
          PoisonGasWarningRadius = propertyFloat()
        }
        57 -> {
          val pos = propertyVector()
          RedZonePosition.set(pos.x, pos.y)

          val b = RedZonePosition
        }
        58 -> {
          RedZoneRadius = propertyFloat()
          val b = RedZoneRadius
        }
        59 -> { // At Spawn Spot
          RemainingTime = propertyInt()
          //println("27 $RemainingTime")
        }
        60 -> {
          val pos = propertyVector()
          SafetyZoneBeginPosition.set(pos.x, pos.y)
        }
        61 -> {
          SafetyZoneBeginRadius = propertyFloat()
        }
        62 -> {
          val pos = propertyVector()
          SafetyZonePosition.set(pos.x, pos.y)
        }
        63 -> {
          SafetyZoneRadius = propertyFloat()
        }
        //64: TeamIds = TArray(Int32)
        //65: TeamIndices = TArray(Int32)
        //66: TeamLeaderNames = TArray(String)
        //67: TeamScores = TArray(Int32)
        //68: TimeLimitSeconds = Float
        69 -> {
          TotalReleaseDuration = propertyFloat()
          val b = TotalReleaseDuration
          //println("44 $b")
        }
        70 -> {
          TotalWarningDuration = propertyFloat()
          //println("46 $b")
        }
        /*51 -> {
          val result = propertyFloat()
          //println("GameStateCMD 51: $result")
        }
        61 -> {
          val result = propertyBool()
          //isWarMode = bIsWarMode
          println("GameStateCMD 61: $result")
        }
        62 -> {
          //val result = propertyBool()
          //isWarMode = bIsWarMode
          //println("GameStateCMD 62: $result")
        }*/
        else -> return false
      }
      return true
    }
  }
}