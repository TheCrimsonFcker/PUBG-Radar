package pubg.radar.struct

import com.badlogic.gdx.math.Vector3
import pubg.radar.deserializer.Buffer

class Bunch(
    val BunchDataBits: Int,
    buffer: Buffer,
    val PacketID: Int,
    val ChIndex: Int,
    val ChType: Int,
    var ChSequence: Int,
    val bOpen: Boolean,
    var bClose: Boolean,
    var bDormant: Boolean,
    var bIsReplicationPaused: Boolean,
    val bReliable: Boolean,
    val bPartial: Boolean,
    val bPartialInitial: Boolean,
    var bPartialFinal: Boolean,
    val bHasPackageMapExports: Boolean,
    var bHasMustBeMappedGUIDs: Boolean
): Buffer(buffer) {
  
  override fun deepCopy(copyBits: Int): Bunch {
    val buf = super.deepCopy(copyBits)
    return Bunch(
        BunchDataBits,
        buf,
        PacketID,
        ChIndex,
        ChType,
        ChSequence,
        bOpen,
        bClose,
        bDormant,
        bIsReplicationPaused,
        bReliable,
        bPartial,
        bPartialInitial,
        bPartialFinal,
        bHasPackageMapExports,
        bHasMustBeMappedGUIDs
    )
  }
  
  var next: Bunch? = null
}

fun Bunch.propertyBool() = readBit()
fun Bunch.propertyFloat() = readFloat()
fun Bunch.propertyInt() = readInt32()
fun Bunch.propertyString() = readString()
fun Bunch.propertyByte() = readByte()
fun Bunch.propertyName() = readName()
fun Bunch.propertyObject() = readObject()
fun Bunch.propertyVector() = Vector3(readFloat(), readFloat(), readFloat())
fun Bunch.propertyRotator() = Vector3(readFloat(), readFloat(), readFloat())
fun Bunch.propertyVector100() = readVector(100, 30)
fun Bunch.propertyVectorQ() = readVector(1, 20)
fun Bunch.propertyVectorNormal() = readFixedVector(1, 16)
fun Bunch.propertyVector10() = readVector(10, 24)
fun Bunch.propertyUInt64() = readInt64()
fun Bunch.propertyNetId() = if (readInt32() > 0) readString() else ""
fun Bunch.repMovement(actor: Actor) {
    val bSimulatedPhysicSleep = readBit()
    val bRepPhysics = readBit()
    actor.location = if (actor.isAPawn)
        readVector(100, 30)
    else readVector(1, 24)

    actor.rotation = if (actor.isACharacter)
        readRotationShort()
    else readRotation()

    actor.velocity = readVector(1, 24)
    if (bRepPhysics)
        readVector(1, 24)
}