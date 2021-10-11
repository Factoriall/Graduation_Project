/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.techtown.graduateproject

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.techtown.graduateproject.camera.CameraSource
import org.techtown.graduateproject.data.Device
import org.techtown.graduateproject.ml.MoveNet

class CameraActivity : AppCompatActivity() {
    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
    }

    /** A [SurfaceView] for camera preview.   */
    private lateinit var surfaceView: SurfaceView

    /** Default device is GPU */
    private var device = Device.CPU

    private lateinit var statusText: TextView
    private lateinit var perfectValue : TextView
    private lateinit var badValue : TextView
    private lateinit var timer : Chronometer
    private var cameraSource: CameraSource? = null
    private var perfectCnt = 0
    private var badCnt = 0
    private var timeWhenStopped: Long = 0
    private var isTimerRunning = false
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        // keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        surfaceView = findViewById(R.id.surfaceView)
        statusText = findViewById(R.id.statusText)
        perfectValue = findViewById(R.id.perfectValue)
        perfectValue.text = perfectCnt.toString()
        badValue = findViewById(R.id.badValue)
        badValue.text = badCnt.toString()
        timer = findViewById(R.id.timer)

        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener {
            val ret = Intent()
            ret.putExtra("time", -timeWhenStopped)
            ret.putExtra("perfect", perfectCnt)
            ret.putExtra("bad", badCnt)
            setResult(RESULT_OK, ret)
            finish()
        }


        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.stopUpdatePose()
        cameraSource?.close()
        cameraSource = null
        super.onPause()
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    // open camera
    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                        override fun onDetectPose(status: String){
                            statusText.text = status
                        }

                        override suspend fun onCountUpPerfect() {
                            perfectCnt += 1
                            withContext(Dispatchers.Main) {
                                perfectValue.text = perfectCnt.toString()
                            }
                        }

                        override suspend fun onCountDownPerfect() {
                            perfectCnt -= 1
                            withContext(Dispatchers.Main) {
                                perfectValue.text = perfectCnt.toString()
                            }
                        }

                        override suspend fun onCountUpBad() {
                            badCnt += 1
                            withContext(Dispatchers.Main) {
                                badValue.text = badCnt.toString()
                            }
                        }

                        override suspend fun onResumeTimer() {
                            withContext(Dispatchers.Main) {
                                if (!isTimerRunning) {
                                    timer.base = SystemClock.elapsedRealtime() + timeWhenStopped
                                    timer.start()
                                    isTimerRunning = true
                                }
                            }
                        }

                        override suspend fun onPauseTimer() {
                            withContext(Dispatchers.Main) {
                                if (isTimerRunning) {
                                    timeWhenStopped = timer.base - SystemClock.elapsedRealtime()
                                    timer.stop()
                                    isTimerRunning = false
                                }
                            }
                        }
                    }).apply {
                        prepareCamera()
                    }
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }


    private fun createPoseEstimator() {
        cameraSource?.setDetector(MoveNet.create(this, device))
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {
            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }

    override fun onBackPressed() {}
}
