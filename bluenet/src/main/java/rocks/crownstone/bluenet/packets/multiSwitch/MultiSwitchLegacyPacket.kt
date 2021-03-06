/**
 * Author: Crownstone Team
 * Copyright: Crownstone (https://crownstone.rocks)
 * Date: Jan 15, 2019
 * License: LGPLv3+, Apache License 2.0, and/or MIT (triple-licensed)
 */

package rocks.crownstone.bluenet.packets.multiSwitch

import rocks.crownstone.bluenet.structs.MultiSwitchIntent
import rocks.crownstone.bluenet.structs.Uint16
import rocks.crownstone.bluenet.structs.Uint8
import rocks.crownstone.bluenet.packets.PacketInterface
import rocks.crownstone.bluenet.structs.MultiSwitchType
import rocks.crownstone.bluenet.util.put
import rocks.crownstone.bluenet.util.putShort
import java.nio.ByteBuffer

class MultiSwitchLegacyPacket: PacketInterface {
	val type = MultiSwitchType.LIST
	val list = ArrayList<MultiSwitchLegacyItemPacket>()

	companion object {
		const val HEADER_SIZE = 2
	}

	fun add(item: MultiSwitchLegacyItemPacket): Boolean {
		list.add(item)
		// TODO: return false when list is too large
		return true
	}

	override fun getPacketSize(): Int {
		return HEADER_SIZE + list.size * MultiSwitchLegacyItemPacket.SIZE
	}

	override fun toBuffer(bb: ByteBuffer): Boolean {
		if (list.isEmpty() || bb.remaining() < getPacketSize()) {
			return false
		}
		bb.put(type.num)
		bb.put(list.size.toByte())
		for (it in list) {
			if (!it.toBuffer(bb)) {
				return false
			}
		}
		return true
	}

	override fun fromBuffer(bb: ByteBuffer): Boolean {
		return false
	}
}

class MultiSwitchLegacyItemPacket(var id: Uint8, var switchValue: Uint8, val timeout: Uint16, var intent: MultiSwitchIntent): PacketInterface {
	companion object {
		const val SIZE = 5
	}

	override fun getPacketSize(): Int {
		return SIZE
	}

	override fun toBuffer(bb: ByteBuffer): Boolean {
		if (bb.remaining() < getPacketSize()) {
			return false
		}
		bb.put(id)
		bb.put(switchValue)
		bb.putShort(timeout)
		bb.put(intent.num)
		return true
	}

	override fun fromBuffer(bb: ByteBuffer): Boolean {
		return false
	}
}