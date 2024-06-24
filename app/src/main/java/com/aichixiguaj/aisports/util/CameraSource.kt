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

package com.aichixiguaj.aisports.util

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
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import com.aichixiguaj.aisports.App
import com.aichixiguaj.aisports.data.Person
import com.aichixiguaj.aisports.ml.MoveNetMultiPose
import com.aichixiguaj.aisports.ml.PoseDetector
import com.aichixiguaj.aisports.ml.TrackerType
import com.aichixiguaj.aisports.ui.activity.star_jump.StarJumpPoseClassifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("DEPRECATION", "unused")
class CameraSource(
    private val textureView: TextureView,
    private var listener: CameraSourceListener? = null
) {

    companion object {
        /** Threshold for confidence score. */
        const val MIN_CONFIDENCE = 0.40f
        private const val TAG = "Camera Source"

        // 界面显示的大小
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var classifier: StarJumpPoseClassifier? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter? = YuvToRgbConverter(App.get())
    private lateinit var imageBitmap: Bitmap

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    private var showPersonCircles = false

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        App.get().getSystemService(Context.CAMERA_SERVICE) as CameraManager
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

    // 是否是前置摄像头
    private var isFrontCamera = AtomicBoolean(false)

    // 支持的相机数量(一般情况前置后置2个  开发板一般一个)
    private var cameraSupportCount = 0

    // 切换摄像头的标识
    private var switchCameraFlag = AtomicBoolean(false)

    suspend fun initCamera() {
        // 打开相机
        camera = openCamera(cameraManager, cameraId)

        if (imageReader == null) {
            // 图片渲染器
            imageReader = ImageReader.newInstance(
                PREVIEW_WIDTH,
                PREVIEW_HEIGHT,
                ImageFormat.YUV_420_888,
                3
            )

            // 图片渲染监听
            imageReader?.setOnImageAvailableListener({ reader ->
                // 获取新的图片数据
                val image = reader.acquireLatestImage()
                if (image != null) {
                    if (!::imageBitmap.isInitialized) {
                        imageBitmap = Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                    }

                    if (!switchCameraFlag.get()){
                        // 转换图片的色彩空间
                        yuvConverter?.yuvToRgb(image, imageBitmap)

                        // Create rotated version for portrait display
                        val rotateMatrix = Matrix()

                        if (textureView.width < textureView.height) {
                            rotateMatrix.postRotate(90.0f)
                        }

                        if (isFrontCamera.get()) {
                            rotateMatrix.setScale(-1f, 1f)
                            rotateMatrix.postRotate(90f)
                        }

                        val rotatedBitmap = Bitmap.createBitmap(
                            imageBitmap,
                            0, 0,
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            rotateMatrix,
                            false
                        )

                        processImage(rotatedBitmap)
                    }

                    image.close()
                }
            }, imageReaderHandler)
        }

        requestPreview()
    }

    private suspend fun requestPreview() {
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
                override fun onConfigured(captureSession: CameraCaptureSession) {
                    cont.resume(captureSession)
                }

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

                override fun onDisconnected(camera: CameraDevice) = camera.close()

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }

    fun prepareCamera(firstCameraLensFacing: Int = CameraCharacteristics.LENS_FACING_FRONT) {
        if (firstCameraLensFacing > 1 || firstCameraLensFacing < 0) {
            throw IllegalArgumentException("UNSUPPORTED_CAMERAS_LENS_FACING")
        }
        val cameraIdList = cameraManager.cameraIdList

        isFrontCamera.set(false)
        cameraSupportCount = cameraIdList.size
        for (cameraId in cameraIdList) {
            // 获取到相机id
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // 如果这个相机方向为空就获取下一个
            val cameraLensFacing =
                characteristics.get(CameraCharacteristics.LENS_FACING) ?: continue

            // 如果有多个相机可用  首选一个自定义的方向
            if (cameraManager.cameraIdList.size > 1) {
                if (firstCameraLensFacing != cameraLensFacing) {
                    continue
                }
            }

            // 更新是否是前置摄像头
            isFrontCamera.set(cameraLensFacing == CameraCharacteristics.LENS_FACING_FRONT)

            // 获取到了摄像头id
            this.cameraId = cameraId
        }

        listener?.apply {
            cameraSupportCount(cameraSupportCount)
            updateCameraLensFacing(isFrontCamera.get())
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

    fun setClassifier(classifier: StarJumpPoseClassifier?) {
        synchronized(lock) {
            if (this.classifier != null) {
                this.classifier?.close()
                this.classifier = null
            }
            this.classifier = classifier
        }
    }

    /**
     * Set Tracker for Movenet MuiltiPose model.
     */
    fun setTracker(trackerType: TrackerType) {
        isTrackerEnabled = trackerType != TrackerType.OFF
        (this.detector as? MoveNetMultiPose)?.setTracker(trackerType)
    }

    fun showPersonCircle(boolean: Boolean) {
        showPersonCircles = boolean
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
        yuvConverter?.close()
        yuvConverter = null
    }

    suspend fun switchCamera(cameraLensFacing: Int) {
        switchCameraFlag.set(true)
        // 先关闭相机
        camera?.close()
        // 先关闭
        session?.close()
        // 切换摄像头
        prepareCamera(cameraLensFacing)
        // 重新初始化
        initCamera()
        delay(300)
        switchCameraFlag.set(false)
    }

    /**
     *  处理图片
     */
    private fun processImage(bitmap: Bitmap) {
        // 人的数据 和 分类的 结果
        val persons = mutableListOf<Person>()
        var classificationResult: List<Pair<Int, Float>>? = null

        synchronized(lock) {
            // 进行姿态推理
            detector?.estimatePoses(bitmap)?.let {
                // 得到人体的关节点数据
                if (it.isNotEmpty()) {
                    persons.add(it[0])
                    // 如果得到的人体数据不为空且置信度达标就可以运行分类
                    if (persons.isNotEmpty() && persons[0].score > MIN_CONFIDENCE) {

                        classifier?.run {
                            // 如果姿态符合分类的情况(比如开合跳需要是站立) 才可进行分类
                            if (poseIsCorrect(it)) {
                                classificationResult = classify(persons[0])
                            }
                        }
                    }
                }
            }
        }

        // 计算帧率
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            // 通知更新视图显示FPS
            listener?.onFPSListener(framesPerSecond)
        }

        // 回调显示这一帧的得分 和 分类的结果
        if (persons.isNotEmpty()) {
            listener?.onDetectedInfo(persons[0].score, classificationResult)
        }

        // 把这一帧显示到界面
        visualize(persons, bitmap)
    }

    /**
     *  显示这一帧的图片
     */
    private fun visualize(persons: List<Person>, bitmap: Bitmap) {

        // 把人体关键点绘制到图片并显示
        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE },
            isTrackerEnabled = isTrackerEnabled,
            showPersonCircles = showPersonCircles,
        )

        // 拿到Surface的Canvas
        val surfaceCanvas = textureView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int

            if (canvas.height > canvas.width) {
                // 竖屏
                // 高度比例
                val ratio = canvas.height.toFloat() / outputBitmap.height
                screenWidth = canvas.width
                // 把图片按照比例放大宽度
                val bitmapWidth = outputBitmap.width * ratio
                val imageHalf = bitmapWidth / 2
                val screenHalf = screenWidth / 2
                val offsetValue = imageHalf.toInt() - screenHalf
                left = -offsetValue
                top = 0
                right = screenWidth + offsetValue
                bottom = canvas.height
            } else {
                // 横屏
                // 宽比例
                val ratio = canvas.width / outputBitmap.width
                // 缩放后的高度
                val scaleHeight = outputBitmap.height * ratio
                val imageHalf = scaleHeight / 2
                val screenHeightHalf = canvas.height / 2
                val offsetValue = imageHalf - screenHeightHalf
                left = 0
                top = -offsetValue
                right = canvas.width
                bottom = canvas.height + offsetValue
            }
            canvas.drawBitmap(
                outputBitmap,
                Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom),
                null
            )
            // 渲染
            textureView.unlockCanvasAndPost(canvas)
        }
        bitmap.recycle()
        outputBitmap.recycle()
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Timber.e("停止线程异常: ${e.message}")
        }
    }

    interface CameraSourceListener {
        fun onFPSListener(fps: Int)

        fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<Int, Float>>?)

        fun cameraSupportCount(count: Int)

        fun updateCameraLensFacing(isFront: Boolean)

        fun initView(container: FrameLayout): View?
    }
}
