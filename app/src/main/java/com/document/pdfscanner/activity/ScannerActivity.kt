package com.document.pdfscanner.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.andyyang.camera2.view.AutoFitTextureView
import com.bumptech.glide.Glide
import com.document.pdfscanner.R
import com.document.pdfscanner.internal.Statics
import com.document.pdfscanner.scanlibrary.Loader
import com.document.pdfscanner.scanlibrary.ScanConstants
import com.document.pdfscanner.scanlibrary.ScanFragment
import com.document.pdfscanner.ulti.ImageLoader
import com.document.pdfscanner.ulti.Utils
import com.document.pdfscanner.view.hideBottomUIMenu
import com.parse.ParseFileUtils
import kotlinx.android.synthetic.main.activity_scan.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import cn.pedant.SweetAlert.SweetAlertDialog
import com.document.pdfscanner.view.OnSingleClickListener
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.android.synthetic.main.activity_scan.checkDone
import kotlinx.android.synthetic.main.activity_scan.countView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Long
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class ScannerActivity : AppCompatActivity() {
    private val CAMERA_FRONT = "1"
    private val CAMERA_BACK = "0"
    private var cameraId = CAMERA_BACK
    private lateinit var textureView: AutoFitTextureView
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size
    private var sizetype = -1
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var imageReader: ImageReader? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private var state = STATE_PREVIEW
    private var flashmode = 0
    private val cameraOpenCloseLock = Semaphore(1)
    private var flashSupported = false
    private var sensorOrientation = 0
    private var currentFragment: Fragment? = null
    private var images = java.util.ArrayList<Uri>()

    private var dir: File? = null
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit

    }

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@ScannerActivity.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@ScannerActivity.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@ScannerActivity.finish()
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        val temp = dir!!.listFiles()
        val files = temp ?: arrayOf()

        countView!!.text = (files.size + 1).toString()
        val f = File(dir, Statics.randString() + ".jpg")
        val acquireNextImage = it.acquireNextImage()
        val buffer = acquireNextImage.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(f)
            val bm: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            //quay anh
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(
                bm, 0, 0,
                bm.width, bm.height,
                matrix, true
            )
            bm.recycle()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)

            runOnUiThread {
                ImageLoader.loadImage(this, "file://${f.absolutePath}", camera_image)
                checkDone!!.visibility = View.VISIBLE
                countView!!.visibility = View.VISIBLE
               try {
                   startScan(f)
               }catch (ex:IllegalStateException){
                   SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).apply {
                       contentText = "Error! An error occurred. Please try again later"
                       confirmText = "Ok"
                       setConfirmClickListener {
                           it.cancel()
                           finish()
                       }
                           .show()
                   }
               }
            }

        } catch (e: IOException) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).apply {
                contentText = "Error! An error occurred. Please try again later"
                confirmText = "Ok"
                setConfirmClickListener {
                    it.cancel()
                    finish()
                }
                    .show()
            }
        } finally {
            acquireNextImage.close()
            output?.let { it2 ->
                try {
                    it2.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    private fun submit() {
        val intent = Intent(this, EditViewActivity::class.java)
//        val b = Bundle()
//        b.putParcelableArrayList("content", images)
//        intent.putExtras(b)
        startActivity(intent)
    }

    fun onScanFinish() {
        try {
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commit()
            currentFragment = null
        } catch (ex: Exception) {

        }
    }

    private fun startScan(m: File) {
        val tran = supportFragmentManager.beginTransaction()
        val frag = ScanFragment()
        val b = Bundle()
        b.putParcelable(ScanConstants.SELECTED_BITMAP, Uri.fromFile(m))
        b.putString(ScanConstants.SCAN_FILE, m.absolutePath)
        frag.arguments = b
        currentFragment = frag
        tran
            .replace(R.id.__main__, frag)
            .commit()
    }


    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {

            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dirname = application.getSharedPreferences("PDFtools", MODE_PRIVATE).getString("sessionName", "")
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        hideBottomUIMenu()
        setContentView(R.layout.activity_camera2)
        init()
        checkDone.setOnClickListener { submit() }
        loadCount()
        dir = File(filesDir, dirname)
        if (!dir!!.exists()) {
            println(dir!!.mkdir())
        } else {
            ParseFileUtils.cleanDirectory(dir)
        }
        onClickItem()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            if (permission == PackageManager.PERMISSION_GRANTED) {
                openCamera(textureView.width, textureView.height)
            }
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
        loadCount()
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    fun init() {
        supportActionBar?.hide()
        sizetype = intent.getIntExtra("sizetype", -1)
        textureView = findViewById(R.id.camera_texture)
    }



    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return

            val largest = Collections.max(
                Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                CompareSizesByArea()
            )
            imageReader = ImageReader.newInstance(
                largest.width, largest.height,
                ImageFormat.JPEG, 2
            ).apply {
                setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            }

            val displayRotation = windowManager.defaultDisplay.rotation

            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
            val swappedDimensions = areDimensionsSwapped(displayRotation)

            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

            previewSize = getMinPreSize(
                map.getOutputSizes(SurfaceTexture::class.java),
                width,
                height,
                maxPreviewHeight
            )

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(previewSize.width, previewSize.height)
            } else {
                textureView.setAspectRatio(previewSize.height, previewSize.width)
            }

            flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            return
        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        } catch (e: NullPointerException) {
            e.printStackTrace()
//            showToast("error")
        }

    }

    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
//                Logger.e("Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {

        setUpCameraOutputs(width, height)
        configureTransform(textureView.width, textureView.height)
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            doAsync {
                SystemClock.sleep(100)
                uiThread {
                    manager.openCamera(cameraId, stateCallback, backgroundHandler)
                }
            }
        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
//            Logger.e(e.toString())
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture

            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)

            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(
                Arrays.asList(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            setAutoFlash(previewRequestBuilder)

                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(
                                previewRequest,
                                captureCallback, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
//                            Logger.e(e.toString())
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
//                        showToast("Failed")
                    }
                }, null
            )
        } catch (e: Exception) {
//            Logger.e(e.toString())
        }

    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                viewHeight.toFloat() / previewSize.height,
                viewWidth.toFloat() / previewSize.width
            )
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    private fun lockFocus() {
        try {
            when (flashmode) {
                0 -> {
                    previewRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    previewRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF
                    )
                }
                1 -> {
                    previewRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                    )
                }

            }
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            state = STATE_WAITING_LOCK
            captureSession?.capture(
                previewRequestBuilder.build(),
                captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        }
    }

    private fun runPrecaptureSequence() {
        try {
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        }

    }

    private fun captureStillPicture() {
        try {
            if (cameraDevice == null) return
            val rotation = windowManager!!.defaultDisplay.rotation

            val captureBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            )?.apply {
                addTarget(imageReader?.surface!!)


                set(
                    CaptureRequest.JPEG_ORIENTATION,
                    (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360
                )

                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }?.also { setAutoFlash(it) }
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                captureBuilder?.build()?.let { capture(it, captureCallback, null) }
            }

        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        }

    }


    private fun unlockFocus() {
        try {
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                backgroundHandler
            )
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(
                previewRequest, captureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
//            Logger.e(e.toString())
        }

    }

    fun onClickItem() {
            camera_picture.setOnClickListener(object : OnSingleClickListener(){
                override fun onSingleClick(v: View?) {
                    lockFocus()
                }
            })
           camera_finsh.setOnClickListener(object : OnSingleClickListener(){
               override fun onSingleClick(v: View?) {
                   finish()
               }
           })
            camera_flash.setOnClickListener(object : OnSingleClickListener(){
                override fun onSingleClick(v: View?) {
                    switchFlashMode()
                }
            })
            camera_flip.setOnClickListener(object : OnSingleClickListener(){
                override fun onSingleClick(v: View?) {
                    lockFocus()
                }
            })

    }

    fun switchFlashMode() {
        when (flashmode) {
            0 -> {
                flashmode = 1
                camera_flash.setImageResource(R.drawable.ic_flash)
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                )
                try {
                    captureSession?.setRepeatingRequest(
                        previewRequestBuilder.build(),
                        captureCallback, backgroundHandler
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                    return
                }
            }
            1 -> {
                flashmode = 0
                camera_flash.setImageResource(R.drawable.ic_flash_off)
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                previewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                try {
                    captureSession?.setRepeatingRequest(
                        previewRequestBuilder.build(),
                        captureCallback, backgroundHandler
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                    return
                }
            }


        }
    }

    fun switchCamera() {
        if (cameraId == CAMERA_FRONT) {
            cameraId = CAMERA_BACK
            closeCamera()
            reopenCamera()
        } else if (cameraId == CAMERA_BACK) {
            cameraId = CAMERA_FRONT
            closeCamera()
            reopenCamera()
        }
    }

    fun reopenCamera() {
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    private fun getCloselyPreSize(
        sizeMap: Array<Size>,
        surfaceWidth: Int,
        surfaceHeight: Int
    ): Size {
        val ReqTmpWidth: Int
        val ReqTmpHeight: Int
        ReqTmpWidth = surfaceHeight
        ReqTmpHeight = surfaceWidth
        sizeMap
            .filter { it.width == ReqTmpWidth && it.height == ReqTmpHeight }
            .forEach { return it }

        val reqRatio = ReqTmpWidth.toFloat() / ReqTmpHeight
        var curRatio: Float
        var deltaRatio: Float
        var deltaRatioMin = java.lang.Float.MAX_VALUE
        var retSize: Size? = null
        for (size in sizeMap) {
            curRatio = size.width.toFloat() / size.height
            deltaRatio = Math.abs(reqRatio - curRatio)
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio
                retSize = size
            }
        }
        return retSize!!
    }


    fun getMinPreSize(
        sizeMap: Array<Size>,
        surfaceWidth: Int,
        surfaceHeight: Int,
        maxHeight: Int
    ): Size {
        val reqRatio = surfaceWidth.toFloat() / surfaceHeight
        var curRatio: Float
        val sizeList = java.util.ArrayList<Size>()
        var retSize: Size? = null
        for (size in sizeMap) {
            curRatio = size.height.toFloat() / size.width
            if (reqRatio == curRatio) {
                sizeList.add(size)
            }
        }

        if (sizeList.size != 0) {
            for (i in sizeList.indices.reversed()) {
                if (sizeList[i].width >= maxHeight) {
                    retSize = sizeList[i]
                    break
                }
            }

            if (retSize == null) {
                retSize = sizeList[sizeList.size - 1]
            }

        } else {
            retSize = getCloselyPreSize(sizeMap, surfaceWidth, surfaceHeight)
        }
        return retSize
    }


    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            setResult(0)
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_CAMERAACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    class CompareSizesByArea : Comparator<Size> {

        override fun compare(lhs: Size, rhs: Size) =
            Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

    }

    companion object {

        private val ORIENTATIONS = SparseIntArray()

        init {

            Loader.load()
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)

        }

        private val STATE_PREVIEW = 0
        private val STATE_WAITING_LOCK = 1
        private val STATE_WAITING_PRECAPTURE = 2
        private val STATE_WAITING_NON_PRECAPTURE = 3
        private val STATE_PICTURE_TAKEN = 4
        private val MAX_PREVIEW_WIDTH = 1080
        private val MAX_PREVIEW_HEIGHT = 1920
        private val CAMERA_CAMERAACTIVITY = 88

    }


    fun loadCount() {
        var i = 0
        try {
            i = dir!!.list().size
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        countView!!.text = i.toString()
        if (i == 0) {
//            nextB!!.visibility = View.INVISIBLE
            checkDone!!.visibility = View.INVISIBLE
            countView!!.visibility = View.INVISIBLE
        }else checkDone!!.visibility = View.VISIBLE
    }

    fun loadLastImage() {
        try {
            Glide.with(this)
                .load(Utils.getBitmap(this, Uri.fromFile(dir!!.listFiles()[dir!!.list().size - 1])))
                .thumbnail(0.2f)
                .into(camera_image)
        } catch (e: Exception) {
        }
    }


}