package rocks.crownstone.bluenet.core

import android.util.Log
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.resolve
import rocks.crownstone.bluenet.Errors

enum class Action {
	NONE,
	CONNECT,
	DISCONNECT,
	DISCOVER,
	READ,
	WRITE,
	SUBSCRIBE,
	UNSUBSCRIBE,
	REFRESH,
}

enum class PromiseType {
	NONE,
	UNIT,
	BYTE_ARRAY,
}

class CorePromises {
	private val TAG = this.javaClass.simpleName

	// Keeps up what action is expected to be performed
	private var action = Action.NONE

	// Keep up promises
	private var promiseType = PromiseType.NONE
	private var unitPromise: Deferred<Unit, Exception>? = null
	private var byteArrayPromise: Deferred<ByteArray, Exception>? = null

	@Synchronized fun isBusy(): Boolean {
		Log.d(TAG, "isBusy action=${action.name} promiseType=${promiseType.name}")
		if (action == Action.NONE) {
			// Extra check, for development
			if (promiseType != PromiseType.NONE) {
				Log.e(TAG, "promise type is not none")
			}
			return false
		}
		return true
	}

	@Synchronized fun <V> setBusy(action: Action, deferred: Deferred<V, Exception>): Boolean {
		if (isBusy()) {
			return false
		}
		Log.d(TAG, "setBusy action=${action.name}")
		when (action) {
			Action.CONNECT, Action.DISCONNECT, Action.REFRESH, Action.DISCOVER, Action.WRITE -> {
				promiseType = PromiseType.UNIT
				unitPromise = deferred as Deferred<Unit, Exception> // Can't check :(
				this.action = action
			}
			Action.READ -> {
				promiseType = PromiseType.BYTE_ARRAY
				byteArrayPromise = deferred as Deferred<ByteArray, Exception> // Can't check :(
				this.action = action
			}
			else -> {
				Log.e(TAG, "wrong action or promise type")
				deferred.reject(Errors.PromiseTypeWrong())
				return false
			}
		}
		return true
	}

//	@Synchronized fun setBusy(action: Action, deferred: Deferred<Unit, Exception>): Boolean {
//		if (isBusy()) {
//			return false
//		}
//		Log.d(TAG, "setBusy action=${action.name}")
//		when (action) {
//			Action.CONNECT, Action.DISCONNECT, Action.REFRESH, Action.DISCOVER, Action.WRITE -> {
//				promiseType = PromiseType.UNIT
//				unitPromise = deferred
//				this.action = action
//			}
//			else -> {
//				Log.e(TAG, "wrong action or promise type")
//				return false
//			}
//		}
//		return true
//	}
//
//	@Synchronized fun setBusyByteArray(action: Action, deferred: Deferred<ByteArray, Exception>): Boolean {
//		if (isBusy()) {
//			return false
//		}
//		Log.d(TAG, "setBusy action=${action.name}")
//		when (action) {
//			Action.READ -> {
//				promiseType = PromiseType.BYTE_ARRAY
//				byteArrayPromise = deferred
//			}
//			else -> {
//				Log.e(TAG, "wrong action or promise type")
//				return false
//			}
//		}
//		return true
//	}

	@Synchronized fun resolve(action: Action) {
		Log.d(TAG, "resolve unit action=${action.name}")
		if (action != this.action) {
			// This shouldn't happen
			Log.e(TAG, "wrong action resolved")
			reject(Errors.ActionTypeWrong())
			return
		}
		if (promiseType != PromiseType.UNIT) {
			// Reject, cause wrong resolve type
			reject(Errors.PromiseTypeWrong())
			return
		}

		unitPromise?.resolve()
		cleanupPromises()
	}

	@Synchronized fun resolve(action: Action, byteArray: ByteArray) {
		Log.d(TAG, "resolve byte array action=${action.name}")
		if (action != this.action) {
			// This shouldn't happen
			Log.e(TAG, "wrong action resolved")
			reject(Errors.ActionTypeWrong())
			return
		}
		if (promiseType != PromiseType.BYTE_ARRAY) {
			// Reject, cause wrong resolve type
			reject(Errors.PromiseTypeWrong())
			return
		}

		byteArrayPromise?.resolve(byteArray)
		cleanupPromises()
	}

	@Synchronized fun reject(error: Exception) {
		Log.d(TAG, "reject error=${error.message}")
		when (promiseType) {
			PromiseType.UNIT -> {
				unitPromise?.reject(error)
				unitPromise = null
			}
			else -> Log.w(TAG, "no promise set")
		}
		cleanupPromises()
	}

	@Synchronized fun cleanupPromises() {
		action = Action.NONE
		promiseType = PromiseType.NONE
		unitPromise = null
		byteArrayPromise = null
	}
}