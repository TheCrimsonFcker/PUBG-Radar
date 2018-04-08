package pubg.radar.ui

import com.badlogic.gdx.graphics.Color
import pubg.radar.gridWidth
import pubg.radar.mapWidth

const val initialWindowWidth = 1000f
const val windowToMapUnit = mapWidth / initialWindowWidth

const val runSpeed = 6.3 * 100 //6.3m/s
const val unit = gridWidth / 8
const val unit2 = unit / 10
//1m=100
const val playerRadius = 3800f
const val healthBarWidth = 15000f
const val healthBarHeight = 2500f
const val vehicle2Width = 400f
const val vehicle4Width = 800f
const val vehicle6Width = 1600f
const val directionRadius = 16000f
const val airDropRadius = 4000f
const val markerRadius = 6000f
const val corpseRadius = 350f
const val itemRadius = 250f
const val visionRadius = mapWidth / 8
const val fov = 60f

const val aimLineWidth = 1500f
const val aimLineRange = 100000f
const val aimCircleRadius = 200f
const val aimTimeThreshold = 1000
const val attackLineDuration = 3000
const val attackMeLineDuration = 7000
const val pinRadius = 3000f

val selfColor = Color(0f, 1f, 0f, 1f)	//green
val teamColor = Color(1f, 1f, 0f, 1f)	//yellow
val teamEdgeColor = Color(1f, 1f, 0f, 1f)  //yellow
val playerColor = Color.RED!!
val playerEdgeColor = Color(0.9f, 0.1f, 0.1f, 1f)
val parachuteColor = Color(0.8f, 0.8f, 0.8f, 1f)  //gray-white
val corpseColor = Color(1f, 0.49f, 0f, 1f)  //orange

val markerColor1 = Color(1f, 1f, 0.3f, 1f)		//yellow
val markerColor2 = Color(1f, 0.3f, 0.3f, 1f)	//red
val markerColor3 = Color(0.3f, 0.3f, 1f, 1f)	//blue
val markerColor4 = Color(0.3f, 1f, 0.3f, 1f)	//green
val markerColor5 = Color(1f, 1f, 0.3f, 1f)		//yellow
val markerColor6 = Color(1f, 0.3f, 0.3f, 1f)	//red
val markerColor7 = Color(0.3f, 0.3f, 1f, 1f)	//blue
val markerColor8 = Color(0.3f, 1f, 0.3f, 1f)	//green

val carColor = Color(0.12f, 0.56f, 1f, 0.9f)  //blue
val bikeColor = Color(0.5f, 0.7f, 1f, 0.9f)  //blue
val boatColor = Color(1f, 0.49f, 0f, 0.9f)  //orange
val planeColor = Color.ORANGE!!

val sightColor = Color(1f, 1f, 1f, 0.5f)
val selfSightColor = Color(0.3f, 1f, 0.3f, 0.4f)
val teamSightColor = Color(1f, 1f, 0.3f, 0.5f)
val enemySightColor = Color(1f, 0.3f, 0.3f, 0.5f)

val pinColor = Color(1f, 1f, 0f, 1f)

val aimLineColor = Color(0f, 0f, 1f, 1f)
val attackLineColor = Color(1.0f, 0f, 0f, 1f)
val safeDirectionColor = Color(0.22f, 0.32f, 0.95f, 1f)  //blue

val visionColor = Color(1f, 1f, 1f, 0.1f)  //white
val redZoneColor = Color(1f, 0f, 0f, 0.1f)
val safeZoneColor = Color(1f, 1f, 1f, 0.9f)

val normalItemColor = Color(0.87f, 0.01f, 1.0f, 1f)  //
val rare4xColor = Color(0.41f,	0.61f,	0.91f, 1f)  //dodgerblue
val rare8xColor = Color(0.7f, 1.0f, 1.0f, 1f)  //Cyan
val rare15xColor = Color(0.8f, 0.8f, 0.8f, 1f)  //Cyan

val rareBagColor = Color(0.01f, 0.01f, 0.7f, 1f)  //blue
val rareHelmetColor = Color(1.0f, 0.01f, 1.0f, 1f)  //magenta
val rareArmorColor = Color(0.63f, 0.13f, 0.94f, 1f)  //purple

val rareSniperColor = Color(1.0f, 0.19f, 0.19f, 1f)  //firebrick
val rareRifleColor = Color(0.54f, 0.27f, 0.14f, 1f)  //sienna4
val rareRifle556Color = Color(0.54f, 0.27f, 0.14f, 1f)  //sienna4
val rareRifle762Color = Color(0.86f, 0.43f, 0.24f, 1f)  //sienna2
val rareAirdropWeaponColor = Color(1.0f, 0.19f, 0.19f, 1f)	//firebrick
val rareFlareColor = Color(1.0f, 0.01f, 1.0f, 1f)	//firebrick

val rareARAttachColor = Color(0.80f, 0.73f, 0.59f, 1f)  //wheat3
val rareSRAttachColor = Color(1.00f, 0.68f, 0.73f, 1f)  //LightPink1

val healItemColor = Color.LIME!! /*Color(0.16f, 0.86f, 0.16f, 1)*/  //green
val drinkItemColor = Color(0.20f, 0.55f, 0.20f, 1f)  //limeGreen

val ghillieColor = Color(0.20f, 0.20f, 0.20f, 1f)

val airDropBlueColor = Color(0.0f, 0.75f, 1f, 1f)
val airDropRedColor = Color(0.90f, 0.21f, 0.21f, 1f)  
val airDropBlueEdgeColor = Color(0.0f, 0.75f, 1f, 1f)
val airDropRedEdgeColor = Color(0.90f, 0.21f, 0.21f, 1f)