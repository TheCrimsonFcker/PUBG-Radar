package pubg.radar.ui

import com.badlogic.gdx.*
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Buttons.BACK
import com.badlogic.gdx.Input.Buttons.FORWARD
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.*
import com.badlogic.gdx.math.*
import pubg.radar.*
import pubg.radar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.corpseLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.deserializer.channel.ActorChannel.Companion.weapons
import pubg.radar.http.PlayerProfile.Companion.completedPlayerInfo
import pubg.radar.http.PlayerProfile.Companion.pendingPlayerInfo
import pubg.radar.http.PlayerProfile.Companion.query
import pubg.radar.sniffer.Sniffer.Companion.sniffOption
import pubg.radar.struct.*
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Archetype.Plane
import pubg.radar.struct.cmd.*
import pubg.radar.struct.cmd.ActorCMD.actorHealth
import pubg.radar.struct.cmd.ActorCMD.actorGroggyHealth
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.ActorCMD.isGroggying
import pubg.radar.struct.cmd.ActorCMD.isReviving
import pubg.radar.struct.cmd.ActorCMD.playerStateToActor
import pubg.radar.struct.cmd.GameStateCMD.ElapsedWarningDuration
import pubg.radar.struct.cmd.GameStateCMD.MatchElapsedMinutes
import pubg.radar.struct.cmd.GameStateCMD.NumAlivePlayers
import pubg.radar.struct.cmd.GameStateCMD.NumAliveTeams
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import pubg.radar.struct.cmd.GameStateCMD.RedZonePosition
import pubg.radar.struct.cmd.GameStateCMD.RedZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.SafetyZonePosition
import pubg.radar.struct.cmd.GameStateCMD.SafetyZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.TotalWarningDuration
import pubg.radar.struct.cmd.PlayerStateCMD.attacks
import pubg.radar.struct.cmd.PlayerStateCMD.playerArmor
import pubg.radar.struct.cmd.PlayerStateCMD.playerHead
import pubg.radar.struct.cmd.PlayerStateCMD.playerBack
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
import pubg.radar.struct.cmd.PlayerStateCMD.playerNumKills
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import pubg.radar.struct.cmd.PlayerStateCMD.selfStateID
import pubg.radar.struct.cmd.PlayerStateCMD.teamNumbers
import pubg.radar.util.tuple4
import pubg.radar.struct.cmd.TeamCMD.team
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

typealias renderInfo = tuple4<Actor, Float, Float, Float>

fun Float.d(n: Int) = String.format("%.${n}f", this)
class GLMap: InputAdapter(), ApplicationListener, GameListener {
  companion object {
    operator fun Vector3.component1(): Float = x
    operator fun Vector3.component2(): Float = y
    operator fun Vector3.component3(): Float = z
    operator fun Vector2.component1(): Float = x
    operator fun Vector2.component2(): Float = y

    val spawnErangel = Vector2(795548.3f, 17385.875f)
    val spawnDesert = Vector2(78282f, 731746f)    
  }
  
  init {
    register(this)
  }
  
  override fun onGameStart() {
    //preSelfCoords.set(if (isErangel) spawnErangel else spawnDesert)
    //selfCoords.set(preSelfCoords)
    //preDirection.setZero()
    //selfCoordsSniffer.setZero()
    selfCoords.setZero()
    selfAttachTo = null
    airdropListX.clear()
    airdropListY.clear()
    gameStartTime = System.currentTimeMillis()
  }
  
  override fun onGameOver() {
    camera.zoom = 1 / 5f
    
    aimStartTime.clear()
    attackLineStartTime.clear()
    //pinLocation.setZero()
  }
  
  fun show() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("${sniffOption.name}")
    config.useOpenGL3(false, 2, 1)
    // config.useOpenGL3(true, 3, 3)
    config.setWindowedMode(600, 600)
    config.setResizable(true)
    // config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode())
    // config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8)
    config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 4)
    config.setIdleFPS(60)
    Lwjgl3Application(this, config)
  }
  
  lateinit var spriteBatch: SpriteBatch
  lateinit var shapeRenderer: ShapeRenderer
  lateinit var mapErangelTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
  lateinit var mapMiramarTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
  lateinit var mapTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
  lateinit var hud_panel: Texture
  lateinit var hud_panel_blank: Texture
  lateinit var bg_compass: Texture
  lateinit var littleFont: BitmapFont
  lateinit var littleFontShadow: BitmapFont
  lateinit var nameFont: BitmapFont
  lateinit var nameFontShadow: BitmapFont
  lateinit var compassFont: BitmapFont
  lateinit var compassFontShadow: BitmapFont
  lateinit var hudFont: BitmapFont
  lateinit var hudFontShadow: BitmapFont
  lateinit var espFont: BitmapFont
  lateinit var espFontShadow: BitmapFont
  lateinit var fontCamera: OrthographicCamera
  lateinit var camera: OrthographicCamera
  lateinit var alarmSound: Sound
  
  val tileZooms = listOf("256", "512", "1024", "2048", "4096", "8192")
  val tileRowCounts = listOf(1, 2, 4, 8, 16, 32)
  val tileSizes = listOf(819200f, 409600f, 204800f, 102400f, 51200f, 25600f)
  

  val layout = GlyphLayout()
  var windowWidth = initialWindowWidth
  var windowHeight = initialWindowWidth
  
  val aimStartTime = HashMap<NetworkGUID, Long>()
  val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
  //val pinLocation = Vector2()

  var filterWeapon = 1
  var filterAttach = -1
  var filterLvl2 = -1
  var filterScope = -1
  var showPlayerGear = 1
  var showCompass = -1
  var zoomSwitch = 1

  var dragging = false
  var prevScreenX = -1f
  var prevScreenY = -1f
  var screenOffsetX = 0f
  var screenOffsetY = 0f

  
  fun windowToMap(x: Float, y: Float) =
      Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
              selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)
  
  fun mapToWindow(x: Float, y: Float) =
      Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
              (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)

  fun Vector2.mapToWindow() = mapToWindow(x, y)   
  fun Vector2.windowToMap() = windowToMap(x, y)  
  
  override fun scrolled(amount: Int): Boolean {
    if (camera.zoom >= 0.04823f && camera.zoom <= 1.1f) {
            camera.zoom *= 1.1f.pow(amount)
    } else {
      if (camera.zoom < 0.04823f) {
        camera.zoom = 0.04823f
      }
      if (camera.zoom > 1.1f) {
        camera.zoom = 1.1f
      }
    }
    return true
  }  
  
  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    if (button == MIDDLE) {
      // pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
      if (screenOffsetX != 0f || screenOffsetY != 0f) {
        screenOffsetX = 0f
        screenOffsetY = 0f
        return true
      } else {
        if (camera.zoom < 0.1f) {
          camera.zoom = 1 / 4f
          return true
        } else {
          camera.zoom = 1 / 12f
          return true
        }
      }
    } else if (button == BACK) {
      camera.zoom /= 1.75f
      camera.update()
      return true
    } else if (button == FORWARD) {
      camera.zoom *= 1.75f
      camera.update()
      return true
    } else if (button == LEFT) {
      dragging = true
      prevScreenX = screenX.toFloat()
      prevScreenY = screenY.toFloat()
      return true
    } else if (button == RIGHT) {
      if (zoomSwitch == 1) {
        camera.zoom = 1 / 12f
        camera.update()
        zoomSwitch = zoomSwitch * -1
        return true
      } else if (zoomSwitch == -1) {
        camera.zoom = 1 / 5f
        camera.update()
        zoomSwitch = zoomSwitch * -1
        return true
      }
    }
    return false
  }

  override fun touchDragged (screenX: Int, screenY: Int, pointer: Int): Boolean {
    if (!dragging) return false
    with (camera) {
      screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
      screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
      prevScreenX = screenX.toFloat()
      prevScreenY = screenY.toFloat()
    }
    // showCompass = -1
    return true
  }

  override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    if (button == LEFT) {
      dragging = false
      return true
    }
    return false
  }

  override fun keyDown(keycode: Int): Boolean {
    if (keycode == NUM_1) {
      filterWeapon = filterWeapon * -1
      return true

    } else if (keycode == NUM_2) {
      filterAttach = filterAttach * -1
      return true

    } else if (keycode == NUM_3) {
      filterLvl2 = filterLvl2 * -1
      return true

    } else if (keycode == NUM_4) {
      filterScope = filterScope * -1
      return true

    } else if (keycode == NUM_5) {
      if (screenOffsetX != 0f || screenOffsetY != 0f) {
        screenOffsetX = 0f
        screenOffsetY = 0f
        // showCompass = 1
        return true
      } else {
        if (camera.zoom < 0.1f) {
          camera.zoom = 1 / 4f
          return true
        } else {
          camera.zoom = 1 / 12f
          return true
        }
      }

    } else if (keycode == NUM_6) {
      if (camera.zoom < 0.7f) {
            camera.zoom *= 1.75f
            return true
      } else {
        camera.zoom = 1.2F
        return true
      }

    } else if (keycode == NUM_7) {
      if (camera.zoom > 0.045f) {
            camera.zoom /= 1.75f
            return true
      } else {
        camera.zoom = 0.045F
        return true
      }

    } else if (keycode == NUM_8) {
      if (screenOffsetX != 0f || screenOffsetY != 0f) {
        screenOffsetX = 0f
        screenOffsetY = 0f
        return true
      } else if (!airdropListX.isEmpty()) {
        val (selfX, selfY) = selfCoords
        screenOffsetX = airdropListX.last() * 200 - selfX
        screenOffsetY = airdropListY.last() * 200 - selfY
        camera.zoom = 1 / 12f
        return true
      }

    } else if (keycode == NUM_9) {
      showCompass = showCompass * -1
      return true

    } else if (keycode == NUM_0) {
      showPlayerGear = showPlayerGear * -1
      return true

    } else if (keycode == DPAD_LEFT) {
      screenOffsetX -= 200000f * camera.zoom
      return true

    } else if (keycode == DPAD_RIGHT) {
      screenOffsetX += 200000f * camera.zoom
      return true

    } else if (keycode == DPAD_UP) {
      screenOffsetY -= 200000f * camera.zoom
      return true

    } else if (keycode == DPAD_DOWN) {
      screenOffsetY += 200000f * camera.zoom
      return true
    } 
    return false
  }
  
  override fun create() {
    spriteBatch = SpriteBatch()
    shapeRenderer = ShapeRenderer()
    Gdx.input.inputProcessor = this;
    camera = OrthographicCamera(windowWidth, windowHeight)
    with(camera) {
      setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
      zoom = 1 / 5f
      update()
      position.set(mapWidth / 2, mapWidth / 2, 0f)
      update()
    }
    
    fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
    alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
    hud_panel = Texture(Gdx.files.internal("images/hud_panel.png"))
    hud_panel_blank = Texture(Gdx.files.internal("images/hud_panel_blank.png"))
    bg_compass = Texture(Gdx.files.internal("images/bg_compass.png"))
    mapErangelTiles = mutableMapOf()
    mapMiramarTiles = mutableMapOf()
    var cur = 0
    tileZooms.forEach{
        mapErangelTiles.set(it, mutableMapOf())
        mapMiramarTiles.set(it, mutableMapOf())
        for (i in 1..tileRowCounts[cur]) {
            val y = if (i < 10) "0$i" else "$i"
            mapErangelTiles[it]?.set(y, mutableMapOf())
            mapMiramarTiles[it]?.set(y, mutableMapOf())
            for (j in 1..tileRowCounts[cur]) {
                val x = if (j < 10) "0$j" else "$j"
                mapErangelTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Erangel/${it}/${it}_${y}_${x}.png")))
                mapMiramarTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Miramar/${it}/${it}_${y}_${x}.png")))
            }
        }
        cur++
    }
    mapTiles = mapErangelTiles

    val generatorHud = FreeTypeFontGenerator(Gdx.files.internal("fonts/HUD.ttf"))
    val paramHud = FreeTypeFontParameter()
    paramHud.characters = DEFAULT_CHARS
    paramHud.size = 30
    paramHud.color = WHITE
    hudFont = generatorHud.generateFont(paramHud)
    paramHud.color = Color(1f, 1f, 1f, 0.4f) 
    hudFontShadow = generatorHud.generateFont(paramHud)
    paramHud.size = 16
    paramHud.color = WHITE
    espFont = generatorHud.generateFont(paramHud)
    paramHud.color = Color(1f, 1f, 1f, 0.2f) 
    espFontShadow = generatorHud.generateFont(paramHud)
    paramHud.size = 14
    paramHud.color = WHITE
    compassFont = generatorHud.generateFont(paramHud)
    paramHud.color = Color(0f, 0f, 0f, 0.5f) 
    compassFontShadow = generatorHud.generateFont(paramHud)

    val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/TEXT.ttf"))
    val param = FreeTypeFontParameter()
    param.characters = DEFAULT_CHARS
    param.size = 16
    param.color = WHITE
    littleFont = generator.generateFont(param)
    param.color = Color(0f, 0f, 0f, 0.5f) 
    littleFontShadow = generator.generateFont(param)
    param.size = 10
    param.color = Color(0.9f, 0.9f, 0.9f, 1f) 
    nameFont = generator.generateFont(param)
    param.color = Color(0f, 0f, 0f, 0.5f) 
    nameFontShadow = generator.generateFont(param)

    generatorHud.dispose()
    generator.dispose()    
  }
  
  val dirUnitVector = Vector2(1f, 0f)
  override fun render() {
    Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    if (gameStarted)
      mapTiles = if (isErangel) mapErangelTiles else mapMiramarTiles
    else
      return
    val currentTime = System.currentTimeMillis()
    selfAttachTo?.apply {
      selfCoords.set(location.x, location.y)
      selfDirection = rotation.y
    }    
    val (selfX, selfY) = selfCoords

    /*
    val selfDir = Vector2(selfX, selfY).sub(preSelfCoords)
    if (selfDir.len() < 1e-8)
      selfDir.set(preDirection)
    */

    // MOVE CAMERA
    camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
    camera.update()
    
    // DRAW MAP
    val cameraTileScale = Math.max(windowWidth, windowHeight) / camera.zoom
    var useScale = 0
    when {
        cameraTileScale > 4096 -> useScale = 5
        cameraTileScale > 2048 -> useScale = 5
        cameraTileScale > 1024 -> useScale = 4
        cameraTileScale > 512  -> useScale = 3
        cameraTileScale > 256  -> useScale = 2
        else -> useScale = 1
    }
    val (tlX, tlY) = Vector2(0f, 0f).windowToMap()
    val (brX, brY) = Vector2(windowWidth, windowHeight).windowToMap()
    var tileZoom = tileZooms[useScale]
    var tileRowCount = tileRowCounts[useScale]
    var tileSize = tileSizes[useScale]
    paint(camera.combined) {
      val xMin = (tlX.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
      val xMax = ((brX.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)
      val yMin = (tlY.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
      val yMax = ((brY.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)
      for (i in yMin..yMax) {
        val y = if (i < 10) "0$i" else "$i"
        for (j in xMin..xMax) {
          val x = if (j < 10) "0$j" else "$j"
          val tileStartX = (j-1)*tileSize
          val tileStartY = (i-1)*tileSize
          draw(mapTiles[tileZoom]!![y]!![x], tileStartX, tileStartY, tileSize, tileSize,
           0, 0, 256, 256,
            false, true)
        }
      }
    }
    
    shapeRenderer.projectionMatrix = camera.combined
    Gdx.gl.glEnable(GL20.GL_BLEND)
    
    // drawGrid()
    drawCircles()
    
    val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
    for ((_, actor) in visualActors)
      typeLocation.compute(actor.Type) { _, v ->
        val list = v ?: ArrayList()
        val (centerX, centerY) = actor.location
        val direction = actor.rotation.y
        list.add(tuple4(actor, centerX, centerY, direction))
        list
      }
    
    paint(fontCamera.combined) {
      
      // NUMBER PANEL
      val numText = "$NumAlivePlayers"
      layout.setText(hudFont, numText)
      spriteBatch.draw(hud_panel, windowWidth - 130f, windowHeight - 60f)
      hudFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
      hudFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width /2, windowHeight - 29f)

      val teamText = "$NumAliveTeams"
      if (teamText != numText) {
        layout.setText(hudFont, teamText)
        spriteBatch.draw(hud_panel, windowWidth - 260f, windowHeight - 60f)
        hudFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
        hudFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width /2, windowHeight - 29f)
      } 

      val timeText = "${ElapsedWarningDuration.toInt() - TotalWarningDuration.toInt()}"
      layout.setText(hudFont, timeText)
      
      var offset = 0f
      if (teamText == numText)
        offset = 130f
      spriteBatch.draw(hud_panel, windowWidth - 390f + offset, windowHeight - 60f)
      hudFontShadow.draw(spriteBatch, "SECS", windowWidth - 345f + offset, windowHeight - 29f)
      hudFont.draw(spriteBatch, "${ElapsedWarningDuration.toInt() - TotalWarningDuration.toInt()}", windowWidth - 370f + offset - layout.width /2, windowHeight - 29f)



      // ITEM ESP FILTER PANEL
      spriteBatch.draw(hud_panel_blank, 30f, windowHeight - 60f)
      
      if (filterWeapon == 1) 
        espFont.draw(spriteBatch, "WEAPON", 37f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "WEAPON", 37f, windowHeight - 25f)
      
      if (filterAttach == 1) 
        espFont.draw(spriteBatch, "ATTACH", 37f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "ATTACH", 37f, windowHeight - 42f)
      
      if (filterLvl2 == 1) 
        espFont.draw(spriteBatch, "GEAR", 92f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "GEAR", 92f, windowHeight - 25f)
      
      if (filterScope == 1) 
        espFont.draw(spriteBatch, "SCOPE", 92f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "SCOPE", 92f, windowHeight - 42f)

      // COMPASS BACKGROUND
      if (showCompass == 1)
        spriteBatch.draw(bg_compass, windowWidth/2 - 165f, windowHeight/2 - 165f)      
      
      safeZoneHint()
      drawPlayerInfos(typeLocation[Player], selfX, selfY)

      /* DISABLE PIN
      val time = (pinLocation.cpy().sub(selfX, selfY).len() / runSpeed).toInt()
      val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
      val (x, y) = pinLocation.mapToWindow()

      for(i in -1..1) {
          for(j in -1..1) {
            littleFontShadow.draw(spriteBatch, "$pinDistance", x + i, windowHeight - y + j)
          }
      }
      littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)
      */

      if (showCompass == 1) {
        layout.setText(compassFont, "0")
        compassFont.draw(spriteBatch, "0"  , windowWidth/2 - layout.width/2, windowHeight/2 + layout.height/2 + 151)                // N
        layout.setText(compassFont, "45")
        compassFont.draw(spriteBatch, "45" , windowWidth/2 - layout.width/2 + 107, windowHeight/2 + layout.height/2 + 107)          // NE
        layout.setText(compassFont, "90")
        compassFont.draw(spriteBatch, "90" , windowWidth/2 - layout.width/2 + 151, windowHeight/2 + layout.height/2)                // E
        layout.setText(compassFont, "135")
        compassFont.draw(spriteBatch, "135", windowWidth/2 - layout.width/2 + 107, windowHeight/2 + layout.height/2 - 107)          // SE
        layout.setText(compassFont, "180")
        compassFont.draw(spriteBatch, "180", windowWidth/2 - layout.width/2, windowHeight/2 + layout.height/2 - 151)                // S
        layout.setText(compassFont, "225")
        compassFont.draw(spriteBatch, "225", windowWidth/2 - layout.width/2 - 107, windowHeight/2 + layout.height/2 - 107)          // SW
        layout.setText(compassFont, "270")
        compassFont.draw(spriteBatch, "270", windowWidth/2 - layout.width/2 - 151, windowHeight/2 + layout.height/2)                // W
        layout.setText(compassFont, "315")
        compassFont.draw(spriteBatch, "315", windowWidth/2 - layout.width/2 - 107, windowHeight/2 + layout.height/2 + 107)          // NW
      }
      
    }
    
    val zoom = camera.zoom
    
    Gdx.gl.glEnable(GL20.GL_BLEND)
    draw(Filled) {
      color = redZoneColor
      circle(RedZonePosition, RedZoneRadius, 100)
      
      color = visionColor
      circle(selfX, selfY, visionRadius, 100)
      
      //color = pinColor
      //circle(pinLocation, pinRadius * zoom, 10)
      drawAirDrop(zoom)
      drawItem()
      drawAirdropWeapon()
      drawAPawn(typeLocation, selfX, selfY, zoom, currentTime)
      drawCorpse()
      // DRAW SELF
      drawPlayer(GREEN, tuple4(null, selfX, selfY, selfDirection))
      // drawPlayer(GREEN, tuple4(null, selfX, selfY, selfDir.angle()))
    }
    
    drawAttackLine(currentTime)

    //preSelfCoords.set(selfX, selfY)
    //preDirection = selfDir
    
    Gdx.gl.glDisable(GL20.GL_BLEND)
  }
  
  private fun drawAttackLine(currentTime: Long) {
    while (attacks.isNotEmpty()) {
      val (A, B) = attacks.poll()
      attackLineStartTime.add(Triple(A, B, currentTime))
    }
    if (attackLineStartTime.isEmpty()) return
    draw(Line) {
      val iter = attackLineStartTime.iterator()
      while (iter.hasNext()) {
        val (A, B, st) = iter.next()
        if (A == selfStateID || B == selfStateID) {
          if (A != B) {
            val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
            if (otherGUID == null) {
              iter.remove()
              continue
            }
            val other = actors[otherGUID]
            if (other == null || currentTime - st > attackMeLineDuration) {
              iter.remove()
              continue
            }
            color = attackLineColor
            val (xA, yA) = other.location
            val (xB, yB) = selfCoords
            line(xA, yA, xB, yB)
          }
        } else {
          val actorAID = playerStateToActor[A]
          val actorBID = playerStateToActor[B]
          if (actorAID == null || actorBID == null) {
            iter.remove()
            continue
          }
          val actorA = actors[actorAID]
          val actorB = actors[actorBID]
          if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
            iter.remove()
            continue
          }
          color = attackLineColor
          val (xA, yA) = actorA.location
          val (xB, yB) = actorB.location
          line(xA, yA, xB, yB)
        }               
      }
    }
  }

  private fun drawCircles() {
    Gdx.gl.glLineWidth(4f)
    draw(Line) {
      color = safeZoneColor
      circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)
      
      color = BLUE
      circle(SafetyZonePosition, SafetyZoneRadius, 100)
      
      if (PoisonGasWarningPosition.cpy().sub(selfCoords).len() > PoisonGasWarningRadius && PoisonGasWarningRadius > 0) {
        color = safeDirectionColor
        line(selfCoords, PoisonGasWarningPosition)
      }
    }
    Gdx.gl.glLineWidth(1f)
  }
  
  /*
  private fun drawGrid() {
    draw(Filled) {
      color = GRAY
      for (i in 0..7) {
        rectLine(0f, i * unit, gridWidth, i * unit, 500f)
        rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
      }

    }
  }
  */

  private fun ShapeRenderer.drawAPawn(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>,
                                      selfX: Float, selfY: Float,
                                      zoom: Float,
                                      currentTime: Long) {
    for ((type, actorInfos) in typeLocation) {
      when (type) {
        TwoSeatBoat -> actorInfos?.forEach {
          drawVehicle(boatColor, it, vehicle2Width, vehicle6Width)
        }
        SixSeatBoat -> actorInfos?.forEach {
          drawVehicle(boatColor, it, vehicle4Width, vehicle6Width)
        }
        TwoSeatBike -> actorInfos?.forEach {
          drawVehicle(bikeColor, it, vehicle2Width, vehicle6Width)
        }
        ThreeSeatBike -> actorInfos?.forEach {
          drawVehicle(bikeColor, it, vehicle4Width, vehicle6Width)
        }
        TwoSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle2Width, vehicle6Width)
        }
        FourSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle4Width, vehicle6Width)
        }
        SixSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle6Width, vehicle6Width)
        }
        Plane -> actorInfos?.forEach {
          drawPlayer(planeColor, it)
        }
        Player -> actorInfos?.forEach {
          drawPlayer(playerColor, it)
          aimAtMe(it, selfX, selfY, currentTime, zoom)
        }
        Parachute -> actorInfos?.forEach {
          drawParachute(it)
        }
        Grenade -> actorInfos?.forEach {
          drawPlayer(WHITE, it, false)
        }
        else -> {
          //            actorInfos?.forEach {
          //            bugln { "${it._1!!.archetype.pathName} ${it._1.location}" }
          //            drawPlayer(BLACK, it)
          //            }
        }
      }
    }
  }
  
  private fun ShapeRenderer.drawCorpse() {
    corpseLocation.values.forEach {
      val (x, y) = it
      val backgroundRadius = (corpseRadius + 50f)
      val radius = corpseRadius
      /*
      color = BLACK
      rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
      color = corpseColor
      rect(x - radius, y - radius, radius * 2, radius * 2)
      color = BLACK
      rectLine(x - radius, y, x + radius, y, 50f)
      rectLine(x, y - radius, x, y + radius, 50f)
      */
      color = BLACK
      rectLine(x - backgroundRadius, y - backgroundRadius, x + backgroundRadius, y + backgroundRadius, 300f)
      rectLine(x + backgroundRadius, y - backgroundRadius, x - backgroundRadius, y + backgroundRadius, 300f)
      color = corpseColor
      rectLine(x - radius, y - radius, x + radius, y + radius, 150f)
      rectLine(x + radius, y - radius, x - radius, y + radius, 150f)
    }
  }
  
  var airdropListX = mutableListOf<Int>()
  var airdropListY = mutableListOf<Int>()
  private fun ShapeRenderer.drawAirDrop(zoom: Float) {
    airDropLocation.values.forEach {
      val (x, y) = it
      val backgroundRadius = (airDropRadius + 1500) * zoom
      val airDropRadius = airDropRadius * zoom
      val (selfX, selfY) = selfCoords

      color = BLACK
      rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
      color = airDropBlueColor
      rect(x - airDropRadius, y - airDropRadius, airDropRadius * 2, airDropRadius)
      color = airDropRedColor
      rect(x - airDropRadius, y, airDropRadius * 2, airDropRadius)
      
      val airDropDistance = (Vector2(x, y).sub(selfX, selfY).len() / 100).toInt()
      val (airx, airy) = Vector2(x, y).mapToWindow()
      
      if ((airx < 0 || airx > windowWidth || airy < 0 || airy >windowHeight) && airDropDistance < 2000) {
        var inWindowX = 0f
        var inWindowY = 0f
        if (airx < 0 && ((windowHeight/2 - airy)/(windowWidth/2 - airx) < windowHeight/windowWidth) && windowHeight/2 > airy) {
          inWindowX = 0f
          inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (windowWidth/2 - airx)
        } else if (airx < 0 && ((airy - windowHeight/2)/(windowWidth/2 - airx) < windowHeight/windowWidth) && windowHeight/2 < airy) {
          inWindowX = 0f
          inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (windowWidth/2 - airx)
        } else if (airx > windowWidth && ((windowHeight/2 - airy)/(airx - windowWidth/2) < windowHeight/windowWidth) && windowHeight/2 > airy) {
          inWindowX = windowWidth
          inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (airx - windowWidth/2)
        } else if (airx > windowWidth && ((airy - windowHeight/2)/(airx - windowWidth/2) < windowHeight/windowWidth) && windowHeight/2 < airy) {
          inWindowX = windowWidth
          inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (airx - windowWidth/2)
        } else if (airy < 0 && ((windowWidth/2 - airx)/(windowHeight/2 - airy) < windowWidth/windowHeight)) {
          inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (windowHeight/2 - airy)
          inWindowY = 0f
        } else if (airy > windowHeight && ((windowWidth/2 - airx)/(airy - windowHeight/2) < windowWidth/windowHeight)) {
          inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (airy - windowHeight/2)
          inWindowY = windowHeight
        }

        val (inMapX, inMapY) = Vector2(inWindowX, inWindowY).windowToMap()
        color = BLACK
        circle(inMapX, inMapY, airDropRadius * 2f + 1500 * zoom, 10)
        color = airDropBlueEdgeColor
        circle(inMapX, inMapY, airDropRadius * 2f, 10)
        color = airDropRedEdgeColor
        circle(inMapX, inMapY, airDropRadius * 1.3f, 10)
      }

      // Airdrop Notification (change to UID later)
      val markX = (x / 200).toInt()
      val markY = (y / 200).toInt()
      if (airdropListX.contains(markX) && airdropListY.contains(markY)) {
        //println(mark)
      }
      else {
        airdropListX.add(markX)
        airdropListY.add(markY)
        alarmSound.play()
      }
    }
  }

  private fun ShapeRenderer.drawAirdropWeapon() { // Can be merged in drawItem
    droppedItemLocation.values
      .forEach {
        val (x, y) = it._1
        val items = it._2

        val backgroundRadius = (itemRadius + 50f)
        val radius = itemRadius
        val triBackRadius = radius + 50f
        val triRadius = radius
        var offsetX = 600f
        var offsetY = 600f

        if (filterWeapon == 1) {
          if ("AWM" in items) {
            color = WHITE
            rect(x - backgroundRadius - offsetX, y - backgroundRadius - offsetY, backgroundRadius * 2, backgroundRadius * 2)
            color = rareAirdropWeaponColor
            rect(x - radius - offsetX, y - radius - offsetY, radius * 2, radius * 2)
          } 
          else if ((items in "M24")) {
            color = WHITE
            rectLine(x - backgroundRadius/1.4f - offsetX, y - backgroundRadius/1.4f - offsetY,
                     x + backgroundRadius/1.4f - offsetX, y + backgroundRadius/1.4f - offsetY, backgroundRadius * 2)
            color = rareAirdropWeaponColor
            rectLine(x - radius/1.4f - offsetX, y - radius/1.4f - offsetY,
                     x + radius/1.4f - offsetX, y + radius/1.4f - offsetY, radius * 2)
          }
          else if (("Mk14" in items)) {
            color = WHITE
            circle(x - offsetX, y - offsetY, backgroundRadius, 10)
            color = rareAirdropWeaponColor
            circle(x - offsetX, y - offsetY, radius, 10)
          } 
          else if ("M249" in items) {
            color = WHITE
            triangle(x - triBackRadius - offsetX, y - triBackRadius - offsetY,
                    x - triBackRadius - offsetX, y + triBackRadius - offsetY,
                    x + triBackRadius - offsetX, y - triBackRadius - offsetY)
            color = rareAirdropWeaponColor
            triangle(x - triRadius - offsetX, y - triRadius - offsetY,
                    x - triRadius - offsetX, y + triRadius - offsetY,
                    x + triRadius - offsetX, y - triRadius - offsetY)
          }
          else if (("AUG" in items)) {
            color = WHITE
            triangle(x - triBackRadius - offsetX, y + triBackRadius - offsetY,
                    x + triBackRadius - offsetX, y + triBackRadius - offsetY,
                    x + triBackRadius - offsetX, y - triBackRadius - offsetY)
            color = rareAirdropWeaponColor
            triangle(x - triRadius - offsetX, y + triRadius - offsetY,
                    x + triRadius - offsetX, y + triRadius - offsetY,
                    x + triRadius - offsetX, y - triRadius - offsetY)
          }
          else if (("Groza" in items)) {
            color = WHITE
            triangle(x - triBackRadius - offsetX, y - triBackRadius - offsetY,
                    x + triBackRadius - offsetX, y + triBackRadius - offsetY,
                    x + triBackRadius - offsetX, y - triBackRadius - offsetY)
            color = rareAirdropWeaponColor
            triangle(x - triRadius - offsetX, y - triRadius - offsetY,
                    x + triRadius - offsetX, y + triRadius - offsetY,
                    x + triRadius - offsetX, y - triRadius - offsetY)
          }
        }


        
      }
  }
  
  private fun ShapeRenderer.drawItem() {
    droppedItemLocation.values
      .forEach {
        val (x, y) = it._1
        val items = it._2

        val finalColor = when {
                "helmet3" in items || "helmet2" in items -> rareHelmetColor
                "armor3" in items || "armor2" in items -> rareArmorColor
                "bag3" in items || "bag2" in items-> rareBagColor
                
                "15x" in items -> rare15xColor
                "8x" in items -> rare8xColor
                "4x" in items -> rare4xColor
                "reddot" in items || "holo" in items || "2x" in items -> rare4xColor
                
                "k98" in items -> rareSniperColor
                "m416" in items || "scar" in items || "m16" in items -> rareRifleColor
                "dp28" in items || "ak" in items -> rareRifleColor

                "heal" in items -> healItemColor
                "drink" in items -> drinkItemColor

                "AR_Extended" in items || "AR_Suppressor" in items || "AR_Composite" in items -> rareARAttachColor
                "SR_Extended" in items || "SR_Suppressor" in items || "CheekPad" in items -> rareSRAttachColor

                "ghillie" in items -> ghillieColor

                else -> normalItemColor
        }

        val backgroundRadius = (itemRadius + 50f)
        val radius = itemRadius
        val triBackRadius = radius * 1.2f + 50f
        val triRadius = radius * 1.2f
        var offsetX = 600f
        var offsetY = 600f

        if ("heal" in items || "drink" in items) {
          color = BLACK
          rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
          color = finalColor
          rect(x - radius, y - radius, radius * 2, radius * 2)
        

        } else if ("bag2" in items || "helmet2" in items || "armor2" in items) {
          if (filterLvl2 == 1) {
            color = BLACK
            triangle(x - triBackRadius, y - triBackRadius,
                    x - triBackRadius, y + triBackRadius,
                    x + triBackRadius, y- triBackRadius)
            color = finalColor
            triangle(x - triRadius, y - triRadius,
                    x - triRadius, y + triRadius,
                    x + triRadius, y - triRadius)
          }
         
        
        } else if ("reddot" in items || "holo" in items || "2x" in items) {
          if (filterScope == 1) {
            color = BLACK
            triangle(x - triBackRadius, y - triBackRadius,
                    x - triBackRadius, y + triBackRadius,
                    x + triBackRadius, y - triBackRadius)
            color = finalColor
            triangle(x - triRadius, y - triRadius,
                    x - triRadius, y + triRadius,
                    x + triRadius, y - triRadius)
          }

        } else if ("AR_Extended" in items || "SR_Extended" in items) {
          if (filterAttach == 1) {   
            color = BLACK
            rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
            color = finalColor
            rect(x - radius, y - radius, radius * 2, radius * 2)
          }
        } else if ("AR_Suppressor" in items || "SR_Suppressor" in items) {
          if (filterAttach == 1) {
            color = BLACK
            circle(x, y, backgroundRadius * 1.2f, 10)
            color = finalColor
            circle(x, y, radius * 1.2f, 10)
          }
        } else if ("AR_Composite" in items || "CheekPad" in items) {
          if (filterAttach == 1) {
            color = BLACK
            triangle(x - triBackRadius, y - triBackRadius,
                    x - triBackRadius, y + triBackRadius,
                    x + triBackRadius, y - triBackRadius)
            color = finalColor
            triangle(x - triRadius, y - triRadius,
                    x - triRadius, y + triRadius,
                    x + triRadius, y - triRadius)
          }


        } else if ("k98" in items || "m416" in items) {
          if (filterWeapon == 1) {
            color = BLACK
            rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
            color = finalColor
            rect(x - radius, y - radius, radius * 2, radius * 2)
          }
        } else if ("scar" in items) {
          if (filterWeapon == 1) {
            color = BLACK
            rectLine(x - backgroundRadius/1.4f, y - backgroundRadius/1.4f,
                     x + backgroundRadius/1.4f, y + backgroundRadius/1.4f, backgroundRadius * 2)
            color = finalColor
            rectLine(x - radius/1.4f, y - radius/1.4f,
                     x + radius/1.4f, y + radius/1.4f, radius * 2)
          }
        } else if ("m16" in items) {
          if (filterWeapon == 1) {
            color = BLACK
            circle(x, y, backgroundRadius * 1.2f, 10)
            color = finalColor
            circle(x, y, radius * 1.2f, 10)
          }
        } else if ("ak" in items) {
          if (filterWeapon == 1) {
            color = BLACK
            triangle(x - triBackRadius, y - triBackRadius,
                    x - triBackRadius, y + triBackRadius,
                    x + triBackRadius, y - triBackRadius)
            color = finalColor
            triangle(x - triRadius, y - triRadius,
                    x - triRadius, y + triRadius,
                    x + triRadius, y - triRadius)
          }
        } else if (("dp28" in items)) {
          if (filterWeapon == 1) {
            color = BLACK
            triangle(x - triBackRadius, y - triBackRadius,
                    x + triBackRadius, y + triBackRadius,
                    x + triBackRadius, y - triBackRadius)
            color = finalColor
            triangle(x - triRadius, y - triRadius,
                    x + triRadius, y + triRadius,
                  x + triRadius, y - triRadius)
          }
        
        } else if ("4x" in items || "8x" in items || "15x" in items  || "ghillie" in items || 
                   "bag3" in items || "armor3" in items || "helmet3" in items) {
          val markX = (x / 200).toInt()
          val markY = (y / 200).toInt()
          if (airdropListX.contains(markX) && airdropListY.contains(markY)) {
            when {
              "15x" in items -> {offsetX *= 0}
              "8x" in items -> {offsetX *= -1}

              "ghillie" in items -> {offsetY *= 0}
              "4x" in items -> {offsetX *= -1; offsetY *= 0}

              "helmet3" in items -> {offsetY *= -1}
              "bag3" in items -> {offsetX *= 0; offsetY *= -1}
              "armor3" in items -> {offsetX *= -1; offsetY *= -1}
            }
            color = WHITE
            rect(x - backgroundRadius - offsetX, y - backgroundRadius - offsetY, backgroundRadius * 2, backgroundRadius * 2)
            color = finalColor
            rect(x - radius - offsetX, y - radius - offsetY, radius * 2, radius * 2)
          }
          else {
            color = BLACK
            rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
            color = finalColor
            rect(x - radius, y - radius, radius * 2, radius * 2)
          }
        }

      }
  }
  
  
  fun drawPlayerInfos(players: MutableList<renderInfo>?, selfX: Float, selfY: Float) {
    players?.forEach {
      val (actor, x, y, _) = it
      actor!!

      val dir = Vector2(x - selfX, y - selfY)
      val distance = (dir.len() / 1000).toInt() * 10
      val angle = ((dir.angle() + 90) % 360 / 5).toInt() * 5
      val (sx, sy) = mapToWindow(x, y)
      
      
      val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
      val name = playerNames[playerStateGUID] ?: return@forEach
      val teamNumber = teamNumbers[playerStateGUID] ?: 0

      val equippedWeapons = actorHasWeapons[actor.netGUID]
      var weapon: String? = ""
      if (equippedWeapons != null) {
        for (w in equippedWeapons) {
          val a = weapons[w] ?: continue
          val result = a.archetype.pathName.split("_")
          weapon += result[2].substring(4) + "\n"
        }
      }

      val weaponAbbr = when {
        "HK416" in weapon.toString() -> "  M4"
        "SCAR-L" in weapon.toString() -> "  SCR"
        "M16A4" in weapon.toString() -> "  M16"
        "AK47" in weapon.toString() -> "  AK"
        "DP28" in weapon.toString() -> "  DP"
        "Kar98" in weapon.toString() -> "  Kar98"
        "SKS" in weapon.toString() -> "  SKS"
        "Mini" in weapon.toString() -> "  Mini"
        "Win94" in weapon.toString() -> "  Win"
        "UMP" in weapon.toString() -> "  UMP"
        "UZI" in weapon.toString() -> "  Uzi"
        "Vector" in weapon.toString() -> "  Vec"
        "Thompson" in weapon.toString() -> "  Tom"
        "Saiga12" in weapon.toString() -> "  S12K"
        "Berreta686" in weapon.toString() -> "  S686"
        "Winchester" in weapon.toString() -> "  S1897"
        "Crossbow" in weapon.toString() -> "  Xbow"
        "G18" in weapon.toString() || "M1911" in weapon.toString() || "M9" in weapon.toString() ||
        "Nagant" in weapon.toString() || "Rhino" in weapon.toString() || "Sawnoff" in weapon.toString() -> "  Pistol"
        "Crowbar" in weapon.toString() || "Machete" in weapon.toString() || "Sickle" in weapon.toString() -> "  Melee"
        weapon.toString() in "" -> ""
        else -> "  $weapon"
      }

      val equippedHead = playerHead[playerStateGUID] ?: " "
      val equippedArmor = playerArmor[playerStateGUID] ?: " "

      // Disabled show items
      /*
      var items=""
      for (element in PlayerState.equipableItems) {
          if (element == null || element._1.isBlank()) continue
          items+="${element._1}->${element._2.toInt()}\n"
      }
      for (element in PlayerState.castableItems) {
          if (element == null || element._1.isBlank()) continue
          items+="${element._1}->${element._2}\n"
      }
      */

      var textTop = "$teamNumber$weaponAbbr"
      if (NumAliveTeams == NumAlivePlayers) 
        textTop = "$weaponAbbr  "
      var textBottom = "$angle°$distance"
      if (isTeamMate(actor)) {
        textTop = "$weaponAbbr  "
        textBottom = "$name"
      }

      layout.setText(nameFont, textTop)
      val widthTop = layout.width
      layout.setText(nameFont, textBottom)
      val widthBottom = layout.width

      if (showPlayerGear == 1) {
      layout.setText(nameFont, equippedHead)
      val widthLeft = layout.width
      layout.setText(nameFont, equippedArmor)
      val widthRight = layout.width
        for(i in -1..1)
          for(j in -1..1) {
            nameFontShadow.draw(spriteBatch, equippedHead, 
                                sx - 14 - widthLeft/2 + i, windowHeight - sy + 1.8f + j)
            nameFontShadow.draw(spriteBatch, equippedArmor, 
                                sx + 14 - widthRight/2 + i, windowHeight - sy + 1.8f + j)
          }
        nameFont.draw(spriteBatch, equippedHead, sx - 14 - widthLeft/2, windowHeight - sy + 1.8f)
        nameFont.draw(spriteBatch, equippedArmor, sx + 14 - widthRight/2, windowHeight - sy + 1.8f)
      }

      for(i in -1..1)
        for(j in -1..1) {
          nameFontShadow.draw(spriteBatch, textTop, 
                              sx - widthTop/2 + i, windowHeight - sy + 15 + j)
          nameFontShadow.draw(spriteBatch, textBottom, 
                              sx - widthBottom/2 + i, windowHeight - sy - 12 + j)
        }
      nameFont.draw(spriteBatch, textTop, sx - widthTop/2, windowHeight - sy + 15)
      nameFont.draw(spriteBatch, textBottom, sx - widthBottom/2, windowHeight - sy - 12)

    }
  }
  
  
  var lastPlayTime = System.currentTimeMillis()
  fun safeZoneHint() {
    if (PoisonGasWarningPosition.cpy().sub(selfCoords).len() > PoisonGasWarningRadius && PoisonGasWarningRadius > 0) {
      val dir = PoisonGasWarningPosition.cpy().sub(selfCoords)
      val road = dir.len() - PoisonGasWarningRadius
      if (road > 0) {
        val runningTime = (road / runSpeed).toInt()
        val (x, y) = dir.nor().scl(road).add(selfCoords).mapToWindow()
        for(i in -1..1)
          for(j in -1..1)
            littleFontShadow.draw(spriteBatch, "$runningTime", x + i, windowHeight - y + j)
        littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
        /*
        val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
        if (remainingTime == 60 && runningTime > remainingTime) {
          val currentTime = System.currentTimeMillis()
          if (currentTime - lastPlayTime > 10000) {
            lastPlayTime = currentTime
            alarmSound.play()
          }
        }
        */
      }
    }
  }
  
  inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
    shapeRenderer.apply {
      begin(type)
      draw()
      end()
    }
  }
  
  inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
    spriteBatch.apply {
      projectionMatrix = matrix
      begin()
      paint()
      end()
    }
  }
  
  fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
    circle(loc.x, loc.y, radius, segments)
  }
  
  fun ShapeRenderer.aimAtMe(it: renderInfo, selfX: Float, selfY: Float, currentTime: Long, zoom: Float) {
    // DRAW AIM LINE
    val (actor, x, y, dir) = it
    if (isTeamMate(actor)) return
    val actorID = actor!!.netGUID
    val dirVec = dirUnitVector.cpy().rotate(dir)
    val focus = Vector2(selfX - x, selfY - y)
    val distance = focus.len()
    var aim = false
    if (distance < aimLineRange && distance > aimCircleRadius) {
      val aimAngle = focus.angle(dirVec)
      if (aimAngle.absoluteValue < asin(aimCircleRadius / distance) * MathUtils.radiansToDegrees) {//aim
        aim = true
        aimStartTime.compute(actorID) { _, startTime ->
          if (startTime == null) currentTime
          else {
            if (currentTime - startTime > aimTimeThreshold) {
              color = aimLineColor
              rectLine(x, y, selfX, selfY, aimLineWidth * zoom)
            }
            startTime
          }
        }
      }
    }
    if (!aim)
      aimStartTime.remove(actorID)
  }
  
  var gameStartTime = System.currentTimeMillis()
  fun ShapeRenderer.drawPlayer(pColor: Color?, actorInfo: renderInfo, drawSight: Boolean = true) {
    val zoom = camera.zoom
    val backgroundRadius = (playerRadius + 1500f) * zoom
    val playerRadius = playerRadius * zoom
    val directionRadius = directionRadius * zoom

    val (actor, x, y, dir) = actorInfo
    val attach = actor?.attachChildren?.values?.firstOrNull()

    val playerID = when {
      actor != null -> actorWithPlayerState[actor!!.netGUID]
      else -> null
    }

    val (selfX, selfY) = selfCoords
    val enemyDistance = (Vector2(x, y).sub(selfX, selfY).len() / 1000).toInt() * 10
    val (airx, airy) = Vector2(x, y).mapToWindow()

    //if (actor?.netGUID == selfID) return
    
    if (drawSight) {
      if (playerID == null) {
        val dirVector = dirUnitVector.cpy().rotate(dir).scl(directionRadius * 10)
        color = LIME
        rectLine(x, y,
                 x + dirVector.x, y + dirVector.y, aimLineWidth * zoom)
      } else {
        //color = sightColor
        color = when {
          isTeamMate(actor) -> teamSightColor
          playerID == null -> selfSightColor
          attach == null -> enemySightColor
          isTeamMate(actors[attach]) -> teamSightColor
          else -> enemySightColor
        }
        arc(x, y, directionRadius, dir - fov / 2, fov, 10)
      }
    }

    // DRAW SELF
    if (playerID == null) {
      color = BLACK
      circle(x, y, backgroundRadius, 10)    
      color = pColor
      circle(x, y, playerRadius, 10)  
    }
    

    // DRAW PLAYER

    if (actor != null && actor.isACharacter) {
      val health = actorHealth[actor.netGUID] ?: 100f
      val groggyHealth = actorGroggyHealth[actor.netGUID] ?: 101f
      
      val width = healthBarWidth * zoom
      val widthBackground = (healthBarWidth + 2000) * zoom
      val height = healthBarHeight * zoom
      val heightBackground = (healthBarHeight + 2000) * zoom
      val positonY = y + playerRadius + heightBackground / 2
      val healthWidth = (health / 100.0 * width).toFloat()

      val playerStateGUID = actorWithPlayerState[actor.netGUID]
      val numKills = playerNumKills[playerStateGUID] ?: 0
      val head = playerHead[playerStateGUID] ?: " "
      val armor = playerArmor[playerStateGUID] ?: " "

      color = BLACK
      rectLine(x - widthBackground / 2 , positonY, x + widthBackground / 2, positonY, heightBackground)
      
      if ("3" in armor) {
        if ("3" in head) {
          color = when {
            health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)    
            health > 57f -> Color(0.16f, 0.86f, 0.16f, 1f)    // 1 headshot by kar98
            health > 38f -> YELLOW                            // 3 bodyshots
            health > 19f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        } else {
          color = when {
            health > 57f -> Color(0.16f, 0.86f, 0.16f, 1f)    
            health > 38f -> YELLOW                            // 3 bodyshots
            health > 19f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        }
      } else if ("2" in armor) {
        if ("3" in head) {
          color = when {
            health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)    
            health > 78f -> Color(0.16f, 0.86f, 0.16f, 1f)    // 1 headshot
            health > 52f -> YELLOW                            // 3 bodyshots
            health > 26f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        } else {
          color = when {
            health > 78f -> Color(0.16f, 0.86f, 0.16f, 1f)    // 1 headshot
            health > 52f -> YELLOW                            // 3 bodyshots
            health > 26f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        }
      } else if ("1" in armor) {
        if ("3" in head) {
          color = when {
            health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)    
            health > 60f -> YELLOW                            // 3 bodyshots
            health > 30f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        } else {
          color = when {
            health > 84f -> Color(0.16f, 0.86f, 0.16f, 1f)    
            health > 60f -> YELLOW                            // 3 bodyshots
            health > 30f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        }
      } else if (" " in armor) {
        if ("3" in head) {
          color = when {
            health > 84f -> Color(0.00f, 0.93f, 0.93f, 1f)    
            health > 44f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        } else {
          color = when {
            health > 88f -> Color(0.16f, 0.86f, 0.16f, 1f)    
            health > 44f -> ORANGE                            // 2 bodyshots
            else -> RED                                       // 1 bodyshot
          }
        }
      }
      rectLine(x - width / 2, positonY, x - width / 2 + healthWidth, positonY, height)

      val playerIsGroggying = isGroggying[actor.netGUID] ?: false
      val playerIsReviving = isReviving[actor.netGUID] ?: false
      //val currentTime = System.currentTimeMillis()

      val name = playerNames[playerStateGUID] ?: return
      query(name)

      if (playerIsGroggying == true) {
        color = if (isTeamMate(actor))
          CYAN
        else 
          Color(0f, 0f, 0f, 0.55f)
        circle(x, y, backgroundRadius, 10)
      } else if (playerIsReviving == true) {
        color = BLACK
        circle(x, y, backgroundRadius, 10)
        color = if (isTeamMate(actor))
          CYAN
        else
          ORANGE
        circle(x, y, playerRadius, 10)
      } else if (completedPlayerInfo.containsKey(name)) {
        val info = completedPlayerInfo[name]!!
        if ((info.killDeathRatio > 3f || info.headshotKillRatio > 0.3f) && !isTeamMate(actor)) {
          color = BLACK
          circle(x, y, backgroundRadius, 10)
          color = Color(1.0f, 0.1f, 1.0f, 1f)
          circle(x, y, playerRadius, 10)
          println("Name: $name, KD: ${info.killDeathRatio}, Headshot: ${info.headshotKillRatio}")
        }
      } else {
        color = BLACK
        circle(x, y, backgroundRadius, 10)
        color = when {
          isTeamMate(actor) -> teamColor
          attach == null -> pColor
          //attach == selfID -> selfColor
          //playerID == null -> pColor
          isTeamMate(actors[attach]) -> teamColor
          else -> pColor
          }
        circle(x, y, playerRadius, 10)
      }

    }

    // DRAW WINDOW EDGE
    if ((airx < 0 || airx > windowWidth || airy < 0 || airy >windowHeight) && enemyDistance < 700) {
      var inWindowX = 0f
      var inWindowY = 0f
      if (airx < 0 && (windowHeight/2 - airy)/(windowWidth/2 - airx) < windowHeight/windowWidth && windowHeight/2 > airy) {
        inWindowX = 0f
        inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (windowWidth/2 - airx)
      } else if (airx < 0 && (airy - windowHeight/2)/(windowWidth/2 - airx) < windowHeight/windowWidth && windowHeight/2 < airy) {
        inWindowX = 0f
        inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (windowWidth/2 - airx)
      } else if (airx > windowWidth && (windowHeight/2 - airy)/(airx - windowWidth/2) < windowHeight/windowWidth && windowHeight/2 > airy) {
        inWindowX = windowWidth
        inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (airx - windowWidth/2)
      } else if (airx > windowWidth && (airy - windowHeight/2)/(airx - windowWidth/2) < windowHeight/windowWidth && windowHeight/2 < airy) {
        inWindowX = windowWidth
        inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (airx - windowWidth/2)
      } else if (airy < 0 && (windowWidth/2 - airx)/(windowHeight/2 - airy) < windowWidth/windowHeight) {
        inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (windowHeight/2 - airy)
        inWindowY = 0f
      } else if (airy > windowHeight && (windowWidth/2 - airx)/(airy - windowHeight/2) < windowWidth/windowHeight) {
        inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (airy - windowHeight/2)
        inWindowY = windowHeight
      }

      val (inMapX, inMapY) = Vector2(inWindowX, inWindowY).windowToMap()
      color = BLACK
      circle(inMapX, inMapY, backgroundRadius, 10)      
      color = if (isTeamMate(actor))
        teamEdgeColor
      else if (playerID == null)
        selfColor
      else
        playerEdgeColor
      circle(inMapX, inMapY, playerRadius, 10)
    }
  }

  private fun isTeamMate(actor: Actor?): Boolean {
    if (actor != null) {
      val playerStateGUID = actorWithPlayerState[actor.netGUID]
      if (playerStateGUID != null) {
        val name = playerNames[playerStateGUID] ?: return false
        if (name in team)
          return true
      }
    }
    return false
  }
  
  fun ShapeRenderer.drawVehicle(_color: Color, actorInfo: renderInfo,
                                width: Float, height: Float) {
    
    val (actor, x, y, dir) = actorInfo
    val v_x = actor!!.velocity.x
    val v_y = actor.velocity.y

    val (selfX, selfY) = selfCoords
    val enemyDistance = (Vector2(x, y).sub(selfX, selfY).len() / 1000).toInt() * 10
    val (airx, airy) = Vector2(x, y).mapToWindow()
    
    val dirVector = dirUnitVector.cpy().rotate(dir).scl(height / 2)
    color = BLACK
    val backVector = dirVector.cpy().nor().scl(height / 2 + 200f)
    rectLine(x - backVector.x, y - backVector.y,
             x + backVector.x, y + backVector.y, width + 400f)
    color = _color
    rectLine(x - dirVector.x, y - dirVector.y,
             x + dirVector.x, y + dirVector.y, width)
    
    color = playerColor
    if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
      actor.attachChildren.forEach { k, _ ->
        if (k == selfID) {
          color = selfColor
          return@forEach
        } else if (isTeamMate(actors[k])) {
          color = teamColor
          return@forEach
        }
      }
      circle(x, y, playerRadius * camera.zoom, 10)

      // DRAW WINDOW EDGE
      if ((airx < 0 || airx > windowWidth || airy < 0 || airy >windowHeight) && enemyDistance < 700) {
        var inWindowX = 0f
        var inWindowY = 0f
        if (airx < 0 && (windowHeight/2 - airy)/(windowWidth/2 - airx) < windowHeight/windowWidth && windowHeight/2 > airy) {
          inWindowX = 0f
          inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (windowWidth/2 - airx)
        } else if (airx < 0 && (airy - windowHeight/2)/(windowWidth/2 - airx) < windowHeight/windowWidth && windowHeight/2 < airy) {
          inWindowX = 0f
          inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (windowWidth/2 - airx)
        } else if (airx > windowWidth && (windowHeight/2 - airy)/(airx - windowWidth/2) < windowHeight/windowWidth && windowHeight/2 > airy) {
          inWindowX = windowWidth
          inWindowY = windowHeight/2 - (windowHeight/2 - airy) * windowWidth/2 / (airx - windowWidth/2)
        } else if (airx > windowWidth && (airy - windowHeight/2)/(airx - windowWidth/2) < windowHeight/windowWidth && windowHeight/2 < airy) {
          inWindowX = windowWidth
          inWindowY = windowHeight/2 + (airy - windowHeight/2) * windowWidth/2 / (airx - windowWidth/2)
        } else if (airy < 0 && (windowWidth/2 - airx)/(windowHeight/2 - airy) < windowWidth/windowHeight) {
          inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (windowHeight/2 - airy)
          inWindowY = 0f
        } else if (airy > windowHeight && (windowWidth/2 - airx)/(airy - windowHeight/2) < windowWidth/windowHeight) {
          inWindowX = windowWidth/2 - (windowWidth/2 - airx) * windowHeight/2 / (airy - windowHeight/2)
          inWindowY = windowHeight
        }

        val (inMapX, inMapY) = Vector2(inWindowX, inWindowY).windowToMap()
        val backgroundRadius = playerRadius + 1500f
        color = BLACK
        rect(inMapX - backgroundRadius * camera.zoom, inMapY - backgroundRadius * camera.zoom, 
             backgroundRadius * camera.zoom * 2, backgroundRadius * camera.zoom * 2)      
        color = if (isTeamMate(actor))
          teamEdgeColor
        else
          playerEdgeColor
        rect(inMapX - playerRadius * camera.zoom, inMapY - playerRadius * camera.zoom, 
             playerRadius * camera.zoom * 2, playerRadius * camera.zoom * 2)
      }
    }
  }

  fun ShapeRenderer.drawParachute(actorInfo: renderInfo, drawSight: Boolean = true) {
    
    val (actor, x, y, dir) = actorInfo

    if (actor!!.attachChildren.isNotEmpty()) {      
      color = BLACK
      circle(x, y, (playerRadius + 1500f) * camera.zoom, 10)
      actor.attachChildren.forEach { k, _ ->
        if (k == selfID) {
          color = selfColor
          return@forEach
        } else if (isTeamMate(actors[k])) {
          color = teamColor
          return@forEach
        } else {
          color = parachuteColor
        }
      }
      circle(x, y, playerRadius * camera.zoom, 10)
    }

    if (drawSight) {
      color = sightColor
      if (actor!!.attachChildren.isNotEmpty()) {
        actor.attachChildren.forEach { k, _ ->
          if (k == selfID) {
            color = selfSightColor
            return@forEach
          } else if (isTeamMate(actors[k])) {
            color = teamSightColor
            return@forEach
          } else {
            color = sightColor
          }
        }
      }
      arc(x, y, directionRadius * camera.zoom, dir - fov / 2, fov, 10)
    }
  }
  
  override fun resize(width: Int, height: Int) {
    windowWidth = width.toFloat()
    windowHeight = height.toFloat()
    camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
    fontCamera.setToOrtho(false, windowWidth, windowHeight)
  }
  
  override fun pause() {
  }
  
  override fun resume() {
  }
  
  override fun dispose() {
    deregister(this)
    alarmSound.dispose()
    hud_panel.dispose()
    hud_panel_blank.dispose()
    bg_compass.dispose()
    hudFont.dispose()
    hudFontShadow.dispose()
    espFont.dispose()
    espFontShadow.dispose()
    nameFont.dispose()
    nameFontShadow.dispose()
    littleFont.dispose()
    littleFontShadow.dispose()
    compassFont.dispose()
    compassFontShadow.dispose()
    var cur = 0
    tileZooms.forEach{
        for (i in 1..tileRowCounts[cur]) {
            val y = if (i < 10) "0$i" else "$i"
            for (j in 1..tileRowCounts[cur]) {
                val x = if (j < 10) "0$j" else "$j"
                mapErangelTiles[it]!![y]!![x]!!.dispose()
                mapMiramarTiles[it]!![y]!![x]!!.dispose()
                mapTiles[it]!![y]!![x]!!.dispose()
            }
        }
        cur++
    }
    spriteBatch.dispose()
    shapeRenderer.dispose()
  }
  
}