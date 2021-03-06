/**
 * Author: Crownstone Team
 * Copyright: Crownstone (https://crownstone.rocks)
 * Date: Jan 15, 2019
 * License: LGPLv3+, Apache License 2.0, and/or MIT (triple-licensed)
 */

package rocks.crownstone.bluenet.core

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import rocks.crownstone.bluenet.util.Log

class LocationServiceRequestActivity : AppCompatActivity() {
	private val TAG = this.javaClass.simpleName
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.i(TAG, "onCreate")
		// No need to set a content view, we just use this activity to show the alert dialog

		// Prevent this dialog to be shown multiple times.
		if (!dialogShown) {
			dialogShown = true
			val builder = AlertDialog.Builder(this)
			builder.setTitle("Location not enabled")  // GPS not found
			builder.setMessage("Location needs to be enabled to scan for bluetooth devices") // Want to enable?
			builder.setPositiveButton("Settings") { dialogInterface, i ->
				dialogShown = false
				finish()
				val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
				startActivity(intent)
			}
			builder.setNegativeButton("Cancel") { dialog, which ->
				dialogShown = false
				finish()
			}
			builder.setCancelable(true)
			builder.setOnCancelListener {
				dialogShown = false
				finish()
			}
			builder.create().show()
		}
		else {
			finish()
		}
	}

	companion object {
		private var dialogShown = false
	}

}