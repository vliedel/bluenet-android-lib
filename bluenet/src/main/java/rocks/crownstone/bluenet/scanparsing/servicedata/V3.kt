/**
 * Author: Crownstone Team
 * Copyright: Crownstone (https://crownstone.rocks)
 * Date: Jan 15, 2019
 * License: LGPLv3+, Apache License 2.0, and/or MIT (triple-licensed)
 */

package rocks.crownstone.bluenet.scanparsing.servicedata

import rocks.crownstone.bluenet.structs.OperationMode
import rocks.crownstone.bluenet.encryption.Encryption
import rocks.crownstone.bluenet.scanparsing.CrownstoneServiceData
import rocks.crownstone.bluenet.structs.ServiceDataType
import rocks.crownstone.bluenet.util.Conversion
import rocks.crownstone.bluenet.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object V3 {
	fun parseHeader(bb: ByteBuffer, servicedata: CrownstoneServiceData): Boolean {
		if (bb.remaining() < 16) {
			return false
		}
		servicedata.setDeviceTypeFromServiceUuid()
		servicedata.operationMode = OperationMode.NORMAL

		// Get first 4 encrypted bytes as changing data (but leave the bb position as it is).
		servicedata.changingData = bb.getInt(bb.position())
		return true
	}

	fun parse(bb: ByteBuffer, servicedata: CrownstoneServiceData, key: ByteArray?): Boolean {
		if (key == null) {
			return parseServiceData(bb, servicedata)
		}
		val decryptedBytes = Encryption.decryptEcb(bb, key)
		if (decryptedBytes == null) {
			return false
		}
		val decryptedBB = ByteBuffer.wrap(decryptedBytes)
		decryptedBB.order(ByteOrder.LITTLE_ENDIAN)
		return parseServiceData(decryptedBB, servicedata)
	}

	private fun parseServiceData(bb: ByteBuffer, servicedata: CrownstoneServiceData): Boolean {
		servicedata.type = ServiceDataType.fromNum(Conversion.toUint8(bb.get()))
		when (servicedata.type) {
			ServiceDataType.STATE -> return Shared.parseStatePacket(bb, servicedata, false, false)
			ServiceDataType.ERROR -> return Shared.parseErrorPacket(bb, servicedata, false, false)
			ServiceDataType.EXT_STATE -> return Shared.parseStatePacket(bb, servicedata, true, false)
			ServiceDataType.EXT_ERROR -> return Shared.parseErrorPacket(bb, servicedata, true, false)
			else -> {
				Log.v("V3", "invalid type")
				return false
			}
		}
	}

}
