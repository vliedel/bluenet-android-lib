/**
 * Author: Crownstone Team
 * Copyright: Crownstone (https://crownstone.rocks)
 * Date: Nov 26, 2019
 * License: LGPLv3+, Apache License 2.0, and/or MIT (triple-licensed)
 */

package rocks.crownstone.bluenet.packets.behaviour

import rocks.crownstone.bluenet.structs.Uint8
import java.nio.ByteBuffer

class SmartTimerBehaviourPacket(
		switchVal: Uint8,
		profileId: Uint8,
		daysOfWeek: DaysOfWeekPacket,
		from: TimeOfDayPacket,
		until: TimeOfDayPacket,
		presence: PresencePacket,
		endConditionPresence: PresencePacket
): BehaviourPacket(BehaviourType.SMART_TIMER, switchVal, profileId, daysOfWeek, from, until) {
	var presence = presence
		private set
	var endConditionPresence = endConditionPresence
		private set

	constructor(): this(0U, 0U, DaysOfWeekPacket(), TimeOfDayPacket(), TimeOfDayPacket(), PresencePacket(), PresencePacket())
	constructor(switchBehaviourPacket: SwitchBehaviourPacket, endConditionPresence: PresencePacket):
			this(switchBehaviourPacket.switchVal,
					switchBehaviourPacket.profileId,
					switchBehaviourPacket.daysOfWeek,
					switchBehaviourPacket.from,
					switchBehaviourPacket.until,
					switchBehaviourPacket.presence,
					endConditionPresence)

	companion object {
		const val SIZE = BehaviourPacket.SIZE + PresencePacket.SIZE + PresencePacket.SIZE
	}

	override fun getPacketSize(): Int {
		return SIZE
	}

	override fun toBuffer(bb: ByteBuffer): Boolean {
		if (bb.remaining() < getPacketSize()) {
			return false
		}
		var success = true
		success = success && super.toBuffer(bb)
		success = success && presence.toBuffer(bb)
		success = success && endConditionPresence.toBuffer(bb)
		success = success && type == BehaviourType.SMART_TIMER
		return success
	}

	override fun fromBuffer(bb: ByteBuffer): Boolean {
		if (bb.remaining() < getPacketSize()) {
			return false
		}
		var success = true
		success = success && super.fromBuffer(bb)
		success = success && presence.fromBuffer(bb)
		success = success && endConditionPresence.fromBuffer(bb)
		success = success && type == BehaviourType.SMART_TIMER
		return success
	}

	override fun toString(): String {
		return "SmartTimerBehaviourPacket(${super.toString()} presence=$presence, endConditionPresence=$endConditionPresence)"
	}
}
