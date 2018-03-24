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
    
    fun query(name: String, sleepTime: Long) {
      if (completedPlayerInfo.containsKey(name)) {
        //println(completedPlayerInfo)
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
              println("null")
              Thread.sleep(2000)
            } else {
              completedPlayerInfo[name] = playerInfo
              pendingPlayerInfo.remove(name)
              //println("$name $playerInfo")
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
          val url = URL("https://pubg.op.gg/user/$name?server=as")
          val html = url.readText()
          val elements = HTMLParser.getUserID(html)
          var strList:List<String> = elements.split("\"")
          if (strList[5] == null) {
            println("ID null")
            return PlayerInfo(0f, 0f)
          }

          val userID = strList[5]
          //println(name)
          //println(url)

          val urlInfo = URL("https://pubg.op.gg/api/users/$userID/ranked-stats?season=2018-03&server=as&queue_size=4&mode=tpp")
          //val htmlInfo = urlInfo.readText()
          //val elementsInfo = HTMLParser.getUserInfo(htmlInfo)
          val elementsInfo = urlInfo.readText()
          var strListInfo:List<String> = elementsInfo.split("sum\":")
          var strResult = ""
          for (item in strListInfo) {
            strResult = strResult + item + "\n"
          }
          
          // println(urlInfo)
          //println(elementsInfo)

          val kills_sum = ((strListInfo[1].split(","))[0]).toFloat()
          //println(kills_sum)
          val headshot_kills_sum = ((strListInfo[3].split(","))[0]).toFloat()
          //println(headshot_kills_sum)
          val deaths_sum = ((strListInfo[4].split(","))[0]).toFloat()
          //println(deaths_sum)
          /*
          val headshot_kills_sum = (strListInfo[11].dropLast(13)).toFloat()
          println(headshot_kills_sum)
          val deaths_sum = (strListInfo[12].dropLast(19)).toFloat()
          println(deaths_sum)
          */

          if (kills_sum == null || headshot_kills_sum == null || deaths_sum == null) {
            println("Info null")
            return PlayerInfo(0f, 0f)
          } else {
            if (deaths_sum == 0f) {
              println("New Account")
              return PlayerInfo(kills_sum, (headshot_kills_sum / kills_sum))
            } else
              return PlayerInfo((kills_sum / deaths_sum), (headshot_kills_sum / kills_sum))
          }
        } catch (e: Exception) {
        }
      }
      return null
    }
    
  }
}