package pubg.radar.struct

class Item {
  companion object {
    
    val category = mapOf(
        "Attach" to mapOf(
            "Weapon" to mapOf(
                "Lower" to mapOf(
                    "AngledForeGrip" to "grip",
                    "Foregrip" to "grip"),
                "Magazine" to mapOf(
                    "Extended" to mapOf(
                        "Large" to "AR_Extended",
                        "SniperRifle" to "SR_Extended"),
                    "ExtendedQuickDraw" to mapOf(
                        "Large" to "AR_Extended",
                        "SniperRifle" to "SR_Extended")),
                "Muzzle" to mapOf(
                    "Choke" to "Choke",
                    "Compensator" to mapOf(
                        "Large" to "AR_Compensator",
                        "SniperRifle" to "SR_Compensator"),
                    "FlashHider" to mapOf(
                        "Large" to "AR_FlashHider",
                        "SniperRifle" to "SR_FlashHider"),
                    "Suppressor" to mapOf(
                        "Large" to "AR_Suppressor",
                        "SniperRifle" to "SR_Suppressor")),
                "Stock" to mapOf(
                    "AR" to "AR_Composite",
                    "SniperRifle" to mapOf(
                        "BulletLoops" to "BulletLoops",
                        "CheekPad" to "CheekPad")),
                "Upper" to mapOf(
                    "DotSight" to "reddot",
                    "Holosight" to "holo",
                    "Aimpoint" to "2x",
                    "ACOG" to "4x",
                    "CQBSS" to "8x",
                    "PM2" to "15x"))),
        "Ghillie" to "ghillie",
        "Boost" to "drink",
        "Heal" to mapOf(
            "FirstAid" to "heal",
            "MedKit" to "heal"
        ),
        "Weapon" to mapOf(
            "K98" to "k98",
            "Kar98" to "k98",
            "Kar98k" to "k98",
            "HK416" to "m416",
            "SCAR-L" to "scar",
            "M16A4" to "m16",
            "AK47" to "ak",
            "DP28" to "dp28",
            "AUG" to "AUG",
            "Groza" to "Groza",
            "AWM" to "AWM",
            "M24" to "M24",
            "M249" to "M249",
            "Mk14" to "Mk14",
            "FlareGun" to "FlareGun",
            "Grenade" to "grenade"),
        "Ammo" to mapOf(
            "556mm" to "556",
            "762mm" to "762"),
        "Armor" to mapOf(
            "C" to mapOf("01" to mapOf("Lv3" to "armor3")),
            "D" to mapOf("01" to mapOf("Lv2" to "armor2"))),
        "Back" to mapOf(
            "C" to mapOf(
                "01" to mapOf("Lv3" to "bag3"),
                "02" to mapOf("Lv3" to "bag3")),
            "F" to mapOf(
                "01" to mapOf("Lv2" to "bag2"),
                "02" to mapOf("Lv2" to "bag2"))),
        "Head" to mapOf(
            "F" to mapOf(
                "01" to mapOf("Lv2" to "helmet2"),
                "02" to mapOf("Lv2" to "helmet2")),
            "G" to mapOf("01" to mapOf("Lv3" to "helmet3"))))
    
    /**
     * @return null if not good, or short name for it
     */
    fun isGood(description: String): String? {
      try {
        val start = description.indexOf("Item_")
        if (start == -1) return null//not item
        val words = description.substring(start + 5).split("_")
        var c = category
        for (word in words) {
          if (word !in c)
            return null
          val sub = c[word]
          if (sub is String)
            return sub
          c = sub as Map<String, Any>
        }
      } catch (e: Exception) {
      }
      return null
    }

    fun simplify(description:String):String {
        try {
            val words = description.split("_")
            var c = category
            for (word in words) {
                if (word !in c)
                    return description
                val sub:Any? = c[word]
                if (sub is String)
                    return sub
                c = sub as Map<String, Any>
            }
        } catch (e:Exception) {
        }
        return description
    }
    
  }
}