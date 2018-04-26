package pubg.radar.http

import pubg.radar.*
import pubg.radar.struct.cmd.GameStateCMD.isTeamMatch
import pubg.radar.struct.cmd.GameStateCMD.NumAlivePlayers
import pubg.radar.struct.cmd.GameStateCMD.NumAliveTeams
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL

object HTMLParser {
    fun getUserID(html: String): String {
        val document = Jsoup.parse(html)
        return document.select("#userNickname").toString()
    }

    fun getUserInfo(htmlInfo: String): String {
        val document = Jsoup.parse(htmlInfo)
        return document.toString()
    }
}

data class PlayerInfo(
    val killDeathRatio: Float,
    val headshotKillRatio: Float,
    val isHacker: Boolean)

class PlayerProfile {
  companion object: GameListener {
    init {
      register(this)
    }
    
    override fun onGameStart() {
      running.set(true)
      scheduled.set(false)
    }
    
    override fun onGameOver() {
      running.set(false)
      completedPlayerInfo.clear()
      pendingPlayerInfo.clear()
      baseCount.clear()
    }
    
    val completedPlayerInfo = ConcurrentHashMap<String, PlayerInfo>()
    val pendingPlayerInfo = ConcurrentHashMap<String, Int>()
    private val baseCount = ConcurrentHashMap<String, Int>()
    val scheduled = AtomicBoolean(false)
    val running = AtomicBoolean(true)
    
    fun query(name: String, sleepTime: Long) {
      if (completedPlayerInfo.containsKey(name)) {
        return
      }
      baseCount.putIfAbsent(name, 0)
      pendingPlayerInfo.compute(name) { _, count ->
        (count ?: 0) + 1
      }
      if (scheduled.compareAndSet(false, true))
        thread(isDaemon = true) {
          while (running.get()) {
            Thread.sleep(sleepTime)
            var next = pendingPlayerInfo.maxBy { it.value + baseCount[it.key]!! }
            if (next == null) {
              scheduled.set(false)
              next = pendingPlayerInfo.maxBy { it.value + baseCount[it.key]!! }
              if (next == null || !scheduled.compareAndSet(false, true))
                break
            }
            val (name) = next
            if (completedPlayerInfo.containsKey(name)) {
              pendingPlayerInfo.remove(name)
              continue
            }
            val playerInfo = search(name)
            if (playerInfo == null) {
              baseCount.compute(name) { _, count ->
                count!! - 1
              }
              Thread.sleep(2000)
            } else {
              completedPlayerInfo[name] = playerInfo
              pendingPlayerInfo.remove(name)
              //println("Pending: ${pendingPlayerInfo.size} Completed: ${completedPlayerInfo.size}")
            }
          }
        }
    }
    
    fun search(name: String): PlayerInfo? {
      if (name == null) {
        println("name null")
        return PlayerInfo(0f, 0f, false)
      } else {
        try {          
          val url = URL("https://pubg.op.gg/user/$name")
          val html = url.readText()
          val elements = HTMLParser.getUserID(html)
          if (elements == "") {
            println("$name get no ID.")
            return PlayerInfo(0f, 0f, false)
          }
          var strList:List<String> = elements.split("\"")
          val userID = strList[5]

          var queue_size = (if (isTeamMatch) "4" else "1")
          var elementsInfo = ""
          
          try {
            val urlASInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-04&server=as&queue_size=$queue_size&mode=tpp")
            elementsInfo = urlASInfo.readText()
          } catch (e: Exception ) {
            try {
              val urlSEAInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-04&server=sea&queue_size=$queue_size&mode=tpp")
              elementsInfo = urlSEAInfo.readText()
            } catch (e: Exception ) {
              try {
                val urlKRJPInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-04&server=jp&queue_size=$queue_size&mode=tpp")
                elementsInfo = urlKRJPInfo.readText()
              } catch (e: Exception ) {
                try {
                  val urlKRJPInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-04&server=kr&queue_size=$queue_size&mode=tpp")
                  elementsInfo = urlKRJPInfo.readText()
                } catch (e: Exception ) {
                  println("op.gg Error: $name Mode: $queue_size")
                }
              }
            }
          }

          var strListInfo:List<String> = elementsInfo.split("sum\":")
          var strResult = ""
          for (item in strListInfo) {
            strResult = strResult + item + "\n"
          }

          val kills_sum = ((strListInfo[1].split(","))[0]).toFloat()
          val headshot_kills_sum = ((strListInfo[3].split(","))[0]).toFloat()
          val deaths_sum = ((strListInfo[4].split(","))[0]).toFloat()

          if (headshot_kills_sum < 10f) {
            return PlayerInfo(0f, 0f, false)
          } else if (headshot_kills_sum / deaths_sum > 0.55f) {
            return PlayerInfo((kills_sum / deaths_sum), (headshot_kills_sum / kills_sum), true)
          } else {
            return PlayerInfo((kills_sum / deaths_sum), (headshot_kills_sum / kills_sum), false)
          }
          
        } catch (e: Exception) {
          println("RetrievePlayerInfo Error: $name")
        }
      }
      return PlayerInfo(0f, 0f, false)
    }
    
  }
}