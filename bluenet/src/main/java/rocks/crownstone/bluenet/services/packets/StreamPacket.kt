package rocks.crownstone.bluenet.services.packets

import rocks.crownstone.bluenet.OpcodeType
import rocks.crownstone.bluenet.Uint8
import rocks.crownstone.bluenet.util.Conversion
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class StreamPacket(type: Uint8, data: ByteArray?, payload: PacketInterface?, opCode: OpcodeType = OpcodeType.WRITE): PacketInterface {
	val TAG = this.javaClass.simpleName
	companion object {
		const val SIZE = 1+1+2
	}
	var type: Uint8 = type
		protected set
	var opCode: OpcodeType = opCode // Not always used
		protected set
	protected var dataSize: Int = 0
	protected var data: ByteArray? = data
	protected var payload: PacketInterface? = payload
	init {
		if (data != null) {
			dataSize = data.size
		}
	}

	constructor(): this(-1, null, null)
	constructor(type: Uint8, data: ByteArray, opCode: OpcodeType = OpcodeType.WRITE): this(type, data, null, opCode)
	constructor(type: Uint8, payload: PacketInterface, opCode: OpcodeType = OpcodeType.WRITE): this(type, null, payload, opCode)
	constructor(type: Uint8, byte: Byte,   opCode: OpcodeType = OpcodeType.WRITE): this(type, byteArrayOf(byte), null, opCode)
	constructor(type: Uint8, short: Short, opCode: OpcodeType = OpcodeType.WRITE): this(type, Conversion.int16ToByteArray(short), null, opCode)
	constructor(type: Uint8, int: Int,     opCode: OpcodeType = OpcodeType.WRITE): this(type, Conversion.int32ToByteArray(int), null, opCode)
	constructor(type: Uint8, float: Float, opCode: OpcodeType = OpcodeType.WRITE): this(type, Conversion.floatToByteArray(float), null, opCode)

	override fun getSize(): Int {
		return SIZE + dataSize
	}

	override fun toBuffer(bb: ByteBuffer): Boolean {
		if (bb.remaining() < getSize()) {
			return false
		}
		bb.order(ByteOrder.LITTLE_ENDIAN)
		bb.put(type.toByte())
		bb.put(opCode.num.toByte())
		bb.putShort(dataSize.toShort())
		val payload = this.payload
		if (payload != null) {
			return payload.toBuffer(bb)
		}
		if (data != null) {
			bb.put(data)
		}
		return true
	}

	override fun fromBuffer(bb: ByteBuffer): Boolean {
		if (bb.remaining() < SIZE) {
			return false
		}
		type = Conversion.toUint8(bb.get())
		opCode = OpcodeType.fromNum(Conversion.toUint8(bb.get()))
		dataSize = Conversion.toUint16(bb.getShort())
		// TODO: also fill up and parse the payload
		return true
	}

//	fun setOpcode(opCode: OpcodeType) {
//		this.opCode = opCode
//	}

	fun getPayload(): ByteArray? {
		if (data != null) {
			return data
		}
		val payload = this.payload
		if (payload != null) {
			return payload.getArray()
		}
		return null
	}
}

class StreamPacketInt8(type: Uint8, value: Byte): StreamPacket(type, value)
class StreamPacketUint8(type: Uint8, value: Short): StreamPacket(type, value.toByte())
class StreamPacketInt16(type: Uint8, value: Short): StreamPacket(type, value)
class StreamPacketUint16(type: Uint8, value: Int): StreamPacket(type, value.toShort())
class StreamPacketInt32(type: Uint8, value: Int): StreamPacket(type, value)
class StreamPacketUint32(type: Uint8, value: Long): StreamPacket(type, value.toInt())
class StreamPacketFloat(type: Uint8, value: Float): StreamPacket(type, value)