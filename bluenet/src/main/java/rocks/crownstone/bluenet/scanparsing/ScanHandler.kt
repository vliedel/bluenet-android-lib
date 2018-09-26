package rocks.crownstone.bluenet.scanparsing

import android.bluetooth.le.ScanResult
import android.util.Log
import rocks.crownstone.bluenet.*
import rocks.crownstone.bluenet.encryption.EncryptionManager

class ScanHandler(evtBus: EventBus, encryptionMngr: EncryptionManager) {
	private val TAG = this::class.java.canonicalName
	private val eventBus = evtBus
	private val encryptionManager = encryptionMngr
	private val validators = HashMap<DeviceAddress, Validator>() // TODO: this grows over time!

	init {
		eventBus.subscribe(BluenetEvent.SCAN_RESULT_RAW, { result: Any -> onRawScan(result as ScanResult) })
	}

	private fun onRawScan(result: ScanResult) {
		Log.d(TAG, "onScan")

		if (result.device.address == null) {
			Log.w(TAG, "Device without address")
			return
		}
		val device = ScannedDevice(result)
		device.parseIbeacon()
		device.parseServiceDataHeader()
		device.parseDfu()

		// After parsing service data header and dfu, the operation mode should be known.
		when (device.operationMode) {
				OperationMode.NORMAL -> {
					val key = encryptionManager.getKeySet(device)?.getGuestKey()
					if (key != null) {
						device.parseServiceData(key)
					}
				}
				OperationMode.SETUP -> {
					device.parseServiceData(null)
				}
				OperationMode.DFU -> {
					// TODO
				}
				else -> {
					// TODO
				}
		}

		// Validate device
		val validator = getValidator(device)
		if (validator.validate(device)) {
			device.validated = true
		}

		eventBus.emit(BluenetEvent.SCAN_RESULT, device)
	}

	private fun getValidator(device: ScannedDevice): Validator {
		var validator: Validator?
		validator = validators.get(device.address)
		if (validator == null) {
			validator = Validator()
			validators.put(device.address, validator)
		}
		return validator
	}
}