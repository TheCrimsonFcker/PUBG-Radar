package pubg.radar.http

import pubg.radar.*
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
    val headshotKillRatio: Float)

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
    
    fun query(name: String) {
      if (completedPlayerInfo.containsKey(name)) return
      baseCount.putIfAbsent(name, 0)
      pendingPlayerInfo.compute(name) { _, count ->
        (count ?: 0) + 1
      }
      if (scheduled.compareAndSet(false, true))
        thread(isDaemon = true) {
          while (running.get()) {
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
              println("null")
              Thread.sleep(2000)
            } else {
              completedPlayerInfo[name] = playerInfo
              pendingPlayerInfo.remove(name)
              //println(playerInfo)
              //Thread.sleep(2000)
            }
          }
        }
    }
    
    fun search(name: String): PlayerInfo? {
      if (name == null) {
        println("name null")
        return PlayerInfo(0f, 0f)
      } else {
        try {
          val url = URL("https://pubg.op.gg/user/$name")
          val html = url.readText()
          val elements = HTMLParser.getUserID(html)
          var strList:List<String> = elements.split("\"")
          if (strList[5] == null) {
            println("ID null")
            return PlayerInfo(0f, 0f)
          }

          val userID = strList[5]
          val urlInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-03&server=as&queue_size=2&mode=tpp")
          val htmlInfo = urlInfo.readText()
          val elementsInfo = HTMLParser.getUserInfo(htmlInfo)
          var strListInfo:List<String> = elementsInfo.split(":")

          val matches_cnt = (strListInfo[3].dropLast(18)).toFloat()
          val kills_sum = (strListInfo[6].dropLast(12)).toFloat()
          val headshot_kills_sum = (strListInfo[9].dropLast(13)).toFloat()

          if (strListInfo == null) {
            println("Info null")
            return PlayerInfo(0f, 0f)
          } else {
            if (matches_cnt == 0f) {
              println("New Account")
              return PlayerInfo(0f, 0f)
            } else
              return PlayerInfo((kills_sum / matches_cnt), (headshot_kills_sum / matches_cnt))
          }
        } catch (e: Exception) {
        }
      }
      return null
    }
    
  }
}