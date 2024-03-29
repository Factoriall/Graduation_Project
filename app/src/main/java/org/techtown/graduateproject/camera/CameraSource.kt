package org.techtown.graduateproject.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techtown.graduateproject.EvaluateSquartUtils
import org.techtown.graduateproject.VisualizationUtils
import org.techtown.graduateproject.YuvToRgbConverter
import org.techtown.graduateproject.data.Person
import org.techtown.graduateproject.ml.PoseClassifier
import org.techtown.graduateproject.ml.PoseDetector
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*

class CameraSource(
    private val surfaceView: SurfaceView,
    private val listener: CameraSourceListener? = null
) {

    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480

        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .4f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var classifier: PoseClassifier? = null
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Readers used as buffers for camera still shots */
    private var imageReader: ImageReader? = null

    /** The [CameraDevice] that will be opened in this fragment */
    private var camera: CameraDevice? = null

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private var session: CameraCaptureSession? = null

    /** [HandlerThread] where all buffer reading operations run */
    private var imageReaderThread: HandlerThread? = null

    /** [Handler] corresponding to [imageReaderThread] */
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

    /** Squart 관련 Coroutine */
    private var squartJob : Job? = null
    private var squartPerson : Person? = null
    private var isSquartMode = false

    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    imageBitmap =
                        Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                }
                yuvConverter.yuvToRgb(image, imageBitmap)
                // Create rotated version for portrait display
                val rotateMatrix = Matrix()
                rotateMatrix.setValues(floatArrayOf(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f))
                rotateMatrix.postRotate(90.0f)

                val rotatedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, false
                )
                processImage(rotatedBitmap)
                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }

    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // We don't use a front facing camera in this sample.
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null &&
                cameraDirection == CameraCharacteristics.LENS_FACING_BACK
            ) {
                continue
            }
            this.cameraId = cameraId
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        classifier?.close()
        classifier = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    // process image
    private fun processImage(bitmap: Bitmap) {
        var person: Person? = null
        var classificationResult: List<Pair<String, Float>>? = null

        synchronized(lock) {
            detector?.estimateSinglePose(bitmap)?.let {
                person = it
                classifier?.run {
                    classificationResult = classify(person)
                }
            }
        }
        frameProcessedInOneSecondInterval++

        person?.let {
            visualize(it, bitmap)
        }
    }

    private fun visualize(person: Person, bitmap: Bitmap) {
        var outputBitmap = bitmap

        if (person.score >= MIN_CONFIDENCE) {
            outputBitmap = VisualizationUtils.drawBodyKeypoints(bitmap, person)
            squartPerson = person
            if(squartJob == null) squartJob = updatePose()
        }
        else{
            stopUpdatePose()
        }

        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom), null
            )
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    fun stopUpdatePose(){
        Log.d("updatePose", "stop")
        squartJob?.cancel()
        squartJob = null
        squartPerson = null
    }

    interface CameraSourceListener {
        fun onDetectPose(status: String)
        suspend fun onCountUpPerfect()
        suspend fun onCountDownPerfect()
        suspend fun onCountUpBad()
        suspend fun onResumeTimer()
        suspend fun onPauseTimer()
    }



    private fun updatePose(): Job {
        var standFlag = false
        var isStand = true
        var isPerfect = false
        var isBad = false
        var prev = "stand"
        var isCounting = false

        return CoroutineScope(Dispatchers.IO).launch {
            while (camera != null) {
                if(squartPerson == null) break
                val now = EvaluateSquartUtils.evaluateSquartPosture(squartPerson!!, isSquartMode)
                if(isSquartMode){
                    when(prev){
                        "not", "side", "straight" -> isSquartMode = false
                        "stand" -> {
                            when (now) {
                                "stand" -> {
                                    if(isCounting) {
                                        Log.d("cameraSource","pauseTimer in squartMode")
                                        listener?.onPauseTimer()
                                        isCounting = false
                                    }
                                }
                                "mid" -> {
                                    if(!isCounting) {
                                        listener?.onResumeTimer()
                                        isCounting = true
                                    }
                                }
                                "sit" -> {
                                    isPerfect = true
                                    isStand = false
                                    listener?.onCountUpPerfect()
                                    if(!isCounting) {
                                        listener?.onResumeTimer()
                                        isCounting = true
                                    }
                                }
                                "badSit" -> {
                                    isBad = true
                                    isStand = false
                                    listener?.onCountUpBad()
                                    if(!isCounting) {
                                        listener?.onResumeTimer()
                                        isCounting = true
                                    }
                                }
                            }
                        }
                        "mid" -> {
                            when (now) {
                                "stand" -> {
                                    isStand = true
                                    isPerfect = false
                                    isBad = false
                                }
                                "sit" ->{
                                    if(!isPerfect && isStand) {
                                        listener?.onCountUpPerfect()
                                        isPerfect = true
                                    }
                                    else if(!isBad && !isStand){
                                        listener?.onCountUpBad()
                                        isBad = true
                                    }
                                }
                                "badSit" -> {
                                    if(!isBad){
                                        listener?.onCountUpBad()
                                        isBad = true
                                    }
                                }
                            }
                        }
                        "sit" -> {
                            when (now) {
                                "stand" -> {
                                    isStand = true
                                    isPerfect = false
                                    isBad = false
                                }
                                "badSit" -> {
                                    if(!isBad){
                                        listener?.onCountDownPerfect()
                                        listener?.onCountUpBad()
                                        isBad = true
                                    }
                                }
                            }
                        }
                        "badSit" -> {
                            when (now) {
                                "stand" -> {
                                    isStand = true
                                    isPerfect = false
                                    isBad = false
                                }
                            }
                        }
                    }
                    prev = now
                    listener?.onDetectPose(now)
                }
                else{
                    if(isCounting) {
                        Log.d("cameraSource","pauseTimer")
                        listener?.onPauseTimer()
                        isCounting = false
                    }


                    if(now == "side"){
                        if(!standFlag) standFlag = true
                        else isSquartMode = true
                    }
                    else isSquartMode = false
                    listener?.onDetectPose("$now standing")
                }

                delay(500)
            }
        }
    }
}
