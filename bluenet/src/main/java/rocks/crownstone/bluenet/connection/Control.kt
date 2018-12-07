package rocks.crownstone.bluenet.connection

import android.util.Log
import nl.komponents.kovenant.Promise
import rocks.crownstone.bluenet.encryption.AccessLevel
import rocks.crownstone.bluenet.packets.ControlPacket
import rocks.crownstone.bluenet.packets.SetupPacket
import rocks.crownstone.bluenet.packets.keepAlive.KeepAlivePacket
import rocks.crownstone.bluenet.packets.keepAlive.MultiKeepAlivePacket
import rocks.crownstone.bluenet.packets.meshCommand.MeshCommandPacket
import rocks.crownstone.bluenet.packets.multiSwitch.MultiSwitchPacket
import rocks.crownstone.bluenet.packets.schedule.ScheduleCommandPacket
import rocks.crownstone.bluenet.structs.*
import rocks.crownstone.bluenet.util.Conversion
import rocks.crownstone.bluenet.util.EventBus

class Control(evtBus: EventBus, connection: ExtConnection) {
	private val TAG = this.javaClass.simpleName
	private val eventBus = evtBus
	private val connection = connection

	fun setSwitch(value: Uint8): Promise<Unit, Exception> {
		return writeCommand(ControlType.SWITCH, value)
	}

	fun setRelay(value: Boolean): Promise<Unit, Exception> {
		return writeCommand(ControlType.RELAY, value)
	}

	fun setPwm(value: Uint8): Promise<Unit, Exception> {
		return writeCommand(ControlType.PWM, value)
	}

	fun multiSwtich(packet: MultiSwitchPacket): Promise<Unit, Exception> {
		return writeCommand(ControlPacket(ControlType.MULTI_SWITCH, packet))
	}

	fun allowDimming(allow: Boolean): Promise<Unit, Exception> {
		return writeCommand(ControlType.ALLOW_DIMMING, allow)
	}

	fun lockSwitch(lock: Boolean): Promise<Unit, Exception> {
		return writeCommand(ControlType.LOCK_SWITCH, lock)
	}

	fun enableSwitchCraft(enable: Boolean): Promise<Unit, Exception> {
		return writeCommand(ControlType.ENABLE_SWITCHCRAFT, enable)
	}

	fun setUart(mode: Uint8): Promise<Unit, Exception> {
		return writeCommand(ControlType.UART_ENABLE, mode)
	}

//	fun uartMessage(): Promise<Unit, Exception>


	fun goToDfu(): Promise<Unit, Exception> {
		return writeCommand(ControlType.GOTO_DFU)
	}

	fun reset(): Promise<Unit, Exception> {
		return writeCommand(ControlType.RESET)
	}

	fun keepAliveState(switchValue: Uint8, timeout: Uint16): Promise<Unit, Exception> {
		val keepAlivePacket = KeepAlivePacket(KeepAliveAction.CHANGE, switchValue, timeout)
		return writeCommand(ControlPacket(ControlType.KEEP_ALIVE_STATE, keepAlivePacket))
	}

	fun keepAlive(): Promise<Unit, Exception> {
		return writeCommand(ControlType.KEEP_ALIVE)
	}

	fun setSchedule(packet: ScheduleCommandPacket): Promise<Unit, Exception> {
		return writeCommand(ControlType.SCHEDULE_ENTRY_SET, packet)
	}

	fun removeSchedule(id: Uint8): Promise<Unit, Exception> {
		return writeCommand(ControlType.SCHEDULE_ENTRY_REMOVE, id)
	}

	fun disconnect(): Promise<Unit, Exception> {
		return writeCommand(ControlType.DISCONNECT)
	}

	fun noop(): Promise<Unit, Exception> {
		return writeCommand(ControlType.NOOP)
	}

	fun increaseTx(): Promise<Unit, Exception> {
		return writeCommand(ControlType.INCREASE_TX)
	}

	fun resetErrors(): Promise<Unit, Exception> {
		return resetErrors(0xFFFFFFFF)
	}

	fun resetErrors(bitmask: Uint32): Promise<Unit, Exception> {
		return writeCommand(ControlType.RESET_STATE_ERRORS, bitmask)
	}

	fun factoryReset(): Promise<Unit, Exception> {
		return writeCommand(ControlType.FACTORY_RESET, BluenetProtocol.FACTORY_RESET_CODE)
	}

	// mesh
	fun keepAliveMeshRepeat(): Promise<Unit, Exception> {
		return writeCommand(ControlType.KEEP_ALIVE_REPEAT_LAST)
	}

	fun keepAliveMeshState(packet: MultiKeepAlivePacket): Promise<Unit, Exception> {
		return writeCommand(ControlPacket(ControlType.KEEP_ALIVE_MESH, packet))
	}

	fun meshCommand(packet: MeshCommandPacket): Promise<Unit, Exception> {
		return writeCommand(ControlPacket(ControlType.MESH_COMMAND, packet))
	}




	internal fun validateSetup(): Promise<Unit, Exception> {
		return writeCommand(ControlType.VALIDATE_SETUP)
	}

	internal fun setup(packet: SetupPacket): Promise<Unit, Exception> {
		val controlPacket = ControlPacket(ControlType.SETUP, packet)
		return writeCommand(controlPacket)
	}



	// Commands without payload
	private fun writeCommand(type: ControlType): Promise<Unit, Exception> {
		return writeCommand(ControlPacket(type))
	}

	// Commands with simple value
	private inline fun <reified T> writeCommand(type: ControlType, value: T): Promise<Unit, Exception> {
		val packet = ControlPacket(type, Conversion.toByteArray(value))
		return writeCommand(packet)
	}

	private fun writeCommand(packet: ControlPacket): Promise<Unit, Exception> {
		val array = packet.getArray()
		Log.i(TAG, "writeCommand ${Conversion.bytesToString(array)}")
		if (connection.mode == CrownstoneMode.SETUP) {
			if (connection.hasCharacteristic(BluenetProtocol.SETUP_SERVICE_UUID, BluenetProtocol.CHAR_SETUP_CONTROL2_UUID)) {
				return connection.write(BluenetProtocol.SETUP_SERVICE_UUID, BluenetProtocol.CHAR_SETUP_CONTROL2_UUID, array, AccessLevel.SETUP)
			}
			else {
				return connection.write(BluenetProtocol.SETUP_SERVICE_UUID, BluenetProtocol.CHAR_SETUP_CONTROL_UUID, array, AccessLevel.SETUP)
			}
		}
		return connection.write(BluenetProtocol.CROWNSTONE_SERVICE_UUID, BluenetProtocol.CHAR_CONTROL_UUID, array, AccessLevel.HIGHEST_AVAILABLE)
	}
}