package pubg.radar.struct.cmd

import pubg.radar.*
import pubg.radar.deserializer.ROLE_MAX
import pubg.radar.struct.*
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyUInt32
import pubg.radar.struct.cmd.CMD.propertyNetId
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.Item.Companion.simplify
import pubg.radar.util.DynamicArray
import pubg.radar.util.tuple2
import java.util.concurrent.*

object PlayerStateCMD: GameListener {
  init {
    register(this)
  }
 
  override fun onGameOver() {
    playerNames.clear()
    playerNumKills.clear()
    uniqueIds.clear()
    teamNumbers.clear()
    attacks.clear()
    selfID = NetworkGUID(0)
    selfStateID = NetworkGUID(0)
    countMedKit.clear()
    countFirstAid.clear()
    countPainKiller.clear()
    countEnergyDrink.clear()
    playerHead.clear()
    playerArmor.clear()
    playerBack.clear()
  }
 
  val playerNames = ConcurrentHashMap<NetworkGUID, String>()
  val playerNumKills = ConcurrentHashMap<NetworkGUID, Int>()
  val uniqueIds = ConcurrentHashMap<String, NetworkGUID>()
  val teamNumbers = ConcurrentHashMap<NetworkGUID, Int>()
  val attacks = ConcurrentLinkedQueue<Pair<NetworkGUID, NetworkGUID>>()//A -> B
  var selfID = NetworkGUID(0)
  var selfStateID = NetworkGUID(0)
  var castableItems = DynamicArray<tuple2<String, Int>?>(8, 0)
  var countMedKit = ConcurrentHashMap<NetworkGUID, Int>()
  var countFirstAid = ConcurrentHashMap<NetworkGUID, Int>()
  var countPainKiller = ConcurrentHashMap<NetworkGUID, Int>()
  var countEnergyDrink = ConcurrentHashMap<NetworkGUID, Int>()
  var equipableItems = DynamicArray<tuple2<String, Float>?>(3, 0)
  val playerHead = ConcurrentHashMap<NetworkGUID, String>()
  val playerArmor = ConcurrentHashMap<NetworkGUID, String>()
  val playerBack = ConcurrentHashMap<NetworkGUID, String>()
 
  fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
    with(bunch) {
      //item_dbg {"$actor"}
      //item_dbg {""}
      when (waitingHandle) {
        1 -> {//1: AttachmentReplication_AttachParent = Object
          val (AttachParent, _) = propertyObject()
        }
        2 -> {//2: AttachmentReplication_LocationOffset = Vector(100, 30)
          val LocationOffset = propertyVector100()
        }
        3 -> {//3: AttachmentReplication_RelativeScale3D = Vector(100, 30)
          val RelativeScale3d = propertyVector100()
        }
        4 -> {//4: AttachmentReplication_RotationOffset = Rotation
          val RotationOffset = readRotationShort()
        }
        5 -> {//5: AttachmentReplication_AttachSocket = Name
          val AttachSocket = readName()
        }
        6 -> {//6: AttachmentReplication_AttachComponent = Object
          val (AttachComponent, _) = propertyObject()
        }
        7 -> {
          val bCanBeDamaged = readBit()
          //println("bCanBeDamaged=$bCanBeDamaged")
        }
        8 -> {
          val bHidden = readBit()
//          println("bHidden=$bHidden")
        }
        9 -> {
          val bReplicateMovement = readBit()
//          println("bHidden=$bReplicateMovement")
        }
        10 -> {
          val bTearOff = readBit()
//          println("bHidden=$bTearOff")
        }
        11 -> {
          val (instigatorGUID, instigator) = propertyObject()
        }
        12 -> {
          val (ownerGUID, owner) = propertyObject()
        }
        13 -> {
          val remoteRole = readInt(ROLE_MAX)
          //readInt(ROLE_MAX)
        }
        /*14 -> {//Struct FRepMovement ReplicatedMovement
          repMovement(actor)
          playerDictionary[actor.netGUID] = actor;
        }*/
        15 -> {
          val role = readInt(ROLE_MAX)
        }
        16 -> {
          val bFromPreviousLevel = propertyBool()
//        println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
        }
        17 -> {
          val isABot = propertyBool()
//        println("${actor.netGUID} isABot=$isABot")
        }
        18 -> {
          val bIsInactive = propertyBool()
//        println("${actor.netGUID} bIsInactive=$bIsInactive")
        }
        19 -> {
          val bIsSpectator = propertyBool()
//        println("${actor.netGUID} bIsSpectator=$bIsSpectator")
        }
        20 -> {
          val bOnlySpectator = propertyBool()
//        println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
        }
        21 -> {
          val ping = propertyByte()
        }
        22 -> {
          val playerID = propertyInt()
//          println("${actor.netGUID} playerID=$playerID")
        }
        23 -> {
          val name = propertyString()
          playerNames[actor.netGUID] = name
//          println("${actor.netGUID} playerID=$name")
        }
        24 -> {
          val score = propertyFloat()  //2969 Engine classes
        }
        25 -> {
          val StartTime = propertyInt()
//        println("${actor.netGUID} StartTime=$StartTime")
        }
        26 -> {
          val uniqueId = propertyNetId()
          uniqueIds[uniqueId] = actor.netGUID
//        println("${playerNames[actor.netGUID]}${actor.netGUID} uniqueId=$uniqueId")
        }
        27 -> {
          val AccountId = propertyString()
//        println("${actor.netGUID} AccountId=$AccountId")
        }
        28 -> {//bIsInAircraft
          val bIsInAircraft = propertyBool()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} bIsInAircraft=$bIsInAircraft")
        }
        29 -> {
          val bIsZombie = propertyBool()
//          println("${playerNames[actor.netGUID]}${actor.netGUID}bIsZombie=$bIsZombie")
        }
        30 -> {
          val currentAttackerPlayerNetId = propertyString()
          attacks.add(Pair(uniqueIds[currentAttackerPlayerNetId]!!, actor.netGUID))
//          println("${playerNames[actor.netGUID]}${actor.netGUID} currentAttackerPlayerNetId=$currentAttackerPlayerNetId")
        }
        31 -> { // EmoteBitArray
          val arraySize = readUInt16()
          castableItems.resize(arraySize)
          var index = readIntPacked()
          while (index != 0)
          {
            val idx = index - 1
            val arrayIdx = idx / 3
            val structIdx = idx % 3
            val element = castableItems[arrayIdx] ?: tuple2("", 0)
            when (structIdx)
            {
              0 -> {
                val (guid, castableItemClass) = readObject()
                if (castableItemClass != null)
                  element._1 = simplify(castableItemClass.pathName)
                //println(element._1)
              }
              1 -> {
                val ItemType = readInt(8)
                val a = ItemType
              }
              2 -> {
                val itemCount = readInt32()
                element._2 = itemCount
              }
            }
            castableItems[arrayIdx] = element
            if ("MedKit" in element._1) {
              countMedKit[actor.netGUID] = element._2
              //println("${actor.netGUID} ${countMedKit[actor.netGUID]}")
              //println(element)
            } else if ("FirstAid" in element._1) {
              countFirstAid[actor.netGUID] = element._2
              //println("${actor.netGUID} ${countFirstAid[actor.netGUID]}")
              //println(element)
            } else if ("PainKiller" in element._1) {
              countPainKiller[actor.netGUID] = element._2
              //println("${actor.netGUID} ${countPainKiller[actor.netGUID]}")
              //println(element)
            } else if ("EnergyDrink" in element._1) {
              countEnergyDrink[actor.netGUID] = element._2
              //println("${actor.netGUID} ${countEnergyDrink[actor.netGUID]}")
              //println(element)
            }
            index = readIntPacked()
          }
          return true
        }
        32 -> {//LastHitTime
          val lastHitTime = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} lastHitTime=$lastHitTime")
        }
        33 -> {//MyGameScoreInTeam
          val scoreInTeam = propertyInt()
        }
        34 -> {
          val ObserverAuthorityType = readInt(5)
//        println("${playerNames[actor.netGUID]}${actor.netGUID} ObserverAuthorityType=$ObserverAuthorityType")
        }
        35 -> {//PlayerScores_ScoreByDamage
          val scoreByDamage = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} scoreByDamage=$scoreByDamage")
        }
        36 -> {//PlayerScores_ScoreByKill
          val ScoreByKill = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreByKill=$ScoreByKill")
        }
        37 -> {//PlayerScores_ScoreByRanking
          val ScoreByRanking = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreByRanking=$ScoreByRanking")
        }
        38 -> {//PlayerScores_ScoreFactor
          val ScoreFactor = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} ScoreFactor=$ScoreFactor")
        }
        39 -> {//PlayerStatistics_NumKills
          val NumKills = propertyInt()
          //val NumKills = propertyInt()
          playerNumKills[actor.netGUID] = NumKills
          //println("${playerNames[actor.netGUID]}${actor.netGUID} NumKills=$NumKills")
        }
        40 -> {//PlayerStatistics_NumKills
          val HeadShots = propertyUInt32()
          //val HeadShots = propertyInt()
          //playerHeadshots[actor.netGUID] = HeadShots
          //println("${playerNames[actor.netGUID]}${actor.netGUID} HeadShots=$HeadShots")
        }
        41 -> {
          val LongestDistanceKill = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} LongestDistanceKill=$LongestDistanceKill")
        }
        42 -> {
          val TotalGivenDamages = propertyFloat()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} TotalGivenDamages=$TotalGivenDamages")
        }
        43 -> {
          val TotalMovedDistanceMeter = propertyFloat()
          selfStateID = actor.netGUID//only self will get this update
//          println("${playerNames[actor.netGUID]}${actor.netGUID} TotalMovedDistanceMeter=$TotalMovedDistanceMeter")
        }
        44 -> {//indicate player's death
          val Ranking = propertyInt()
//        println("${playerNames[actor.netGUID]}${actor.netGUID} Ranking=$Ranking")
        }
        //45: ReplicatedCastableItems = TArray(FReplicatedCastableItem)
        46 -> {//ReplicatedEquipableItems
          val arraySize = readUInt16()
          equipableItems.resize(arraySize)
          var index = readIntPacked()
          while (index != 0) {
            val idx = index - 1
            val arrayIdx = idx / 2
            val structIdx = idx % 2
            val element = equipableItems[arrayIdx] ?: tuple2("",0f)
            when (structIdx) {
              0 -> {
                val (guid,equipableItemClass) = readObject()
                if (equipableItemClass != null) {
                  element._1 = simplify(equipableItemClass.pathName)
                  if ("Head" in element._1.toString()) {
                    playerHead[actor.netGUID] = when {
                      "Lv1" in element._1.toString() -> "1"
                      "Lv2" in element._1.toString() -> "2"
                      "Lv3" in element._1.toString() -> "3"
                      else -> "_"
                    }
                  } else if ("Armor" in element._1.toString()) {
                    playerArmor[actor.netGUID] = when {
                      "Lv1" in element._1.toString() -> "1"
                      "Lv2" in element._1.toString() -> "2"
                      "Lv3" in element._1.toString() -> "3"
                      else -> "_"
                    }
                  } else if ("Back" in element._1.toString()) {
                    playerBack[actor.netGUID] = when {
                      "Lv1" in element._1.toString() -> "1"
                      "Lv2" in element._1.toString() -> "2"
                      "Lv3" in element._1.toString() -> "3"
                      else -> "_"
                    }
                  }
                }
                //println("${actor.netGUID} = ${playerHead[actor.netGUID]}/${playerArmor[actor.netGUID]}")
                val a = guid
              }
              1 -> {
                val durability = readFloat()
                element._2 = durability
                val a = durability
              }
            }
            equipableItems[arrayIdx]=element
            index=readIntPacked()
          }
          return true
        }
        47 -> {
          val ReportToken = propertyString()
//        println("${actor.netGUID} ReportToken=$ReportToken")
        }
        48 -> {
          val teamNumber = propertyInt()
          //val teamNumber = readInt(100)
          teamNumbers[actor.netGUID] = teamNumber
//        println("${playerNames[actor.netGUID]}${actor.netGUID} TeamNumber=$teamNumber")
        }
        else -> return false
      }
    }
    return true
  }
}