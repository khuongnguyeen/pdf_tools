package com.document.pdfscanner.activity

import android.content.Intent
import android.graphics.*
import android.hardware.Camera
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.document.pdfscanner.R
import com.document.pdfscanner.internal.Statics
import com.document.pdfscanner.internal.ZoomHandler
import com.document.pdfscanner.scanlibrary.Loader
import com.document.pdfscanner.scanlibrary.ScanConstants
import com.document.pdfscanner.scanlibrary.ScanFragment
import com.document.pdfscanner.ulti.Utils
import com.document.pdfscanner.view.SquareImageView
import com.parse.ParseFileUtils
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.File
import java.io.FileOutputStream

class Camera2Activity : AppCompatActivity(), View.OnClickListener,
    TextureView.SurfaceTextureListener
{
    private var camera: Camera? = null
    private var shotBtn: SquareImageView? = null
    private var nextB: SquareImageView? = null
    private var checkDone: ImageView? = null
    private var settingsB: ImageButton? = null
    private var close: ImageButton? = null
    private var texture: TextureView? = null
    private var countView: TextView? = null
    private var count = 0
    private var flash = false
    private var zoomHandler: ZoomHandler? = null
    private var images = java.util.ArrayList<Uri>()
    private var currentFragment: Fragment? = null
    private var dir: File? = null
    private var surface: SurfaceTexture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val dirname = application.getSharedPreferences("PDFtools", MODE_PRIVATE).getString("sessionName", "")

        flash = getSharedPreferences("PDFtools", MODE_PRIVATE).getBoolean("flash", false)
        texture = findViewById(R.id.camView)
        texture?.surfaceTextureListener = this

        shotBtn = findViewById(R.id.shotBtn)
        shotBtn?.setOnClickListener(this)
        nextB = findViewById(R.id.NextB)
        checkDone = findViewById(R.id.checkDone)
        checkDone?.setOnClickListener(this)
        settingsB = findViewById(R.id.settings)
        close = findViewById(R.id.close)
        close!!.setOnClickListener { finish() }
        settingsB?.setOnClickListener(this)
        countView = findViewById(R.id.countView)
        loadCount()
        dir = File(filesDir, dirname)
        if (!dir!!.exists()) {
            println(dir!!.mkdir())
        } else {
            ParseFileUtils.cleanDirectory(dir)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        this.surface = surface
        openCamera()
        startCamera()
    }

    private fun setOrientationCamera(cameraInfo: Camera.CameraInfo) {
        //huong giao dien
        val rotation: Int = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360
        }
        //set huong cho camera
        camera?.setDisplayOrientation(result)
    }

    private fun stopCamera() {
        if (camera != null) {
            camera?.stopPreview()
            camera?.release()
            camera = null
        }
    }

    private fun startCamera() {
        if (camera == null || surface == null) {
            return
        }
        //lay thong tin camera
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo)
        setOrientationCamera(cameraInfo)

        val pr = camera!!.parameters
        val sizes = pr.supportedPreviewSizes
        sizes.sortBy { -it.height * it.width }

        pr.setPreviewSize(sizes[0].width, sizes[0].height)
        val h = if (sizes[0].width > sizes[0].height) sizes[0].width else
            sizes[0].height
        val w = if (sizes[0].width < sizes[0].height) sizes[0].width else
            sizes[0].height


        val wScreen = texture?.width!!
        val hScreen = texture?.height!!
        val hTT = wScreen * h / w
        val hBottom = hScreen - hTT
        if (include2.layoutParams.height<hBottom){
            include2.layoutParams.height = hBottom
            include2.requestLayout()
            camView.layoutParams.height = texture?.height!!
            camView.requestLayout()
        }
        //update size tt
        pr?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        settingsB!!.setImageResource(R.drawable.ic_non_flash)

        if (flash) {
            pr?.flashMode = Camera.Parameters.FLASH_MODE_ON
            settingsB!!.setImageResource(R.drawable.ic_flash_auto)
        }

        //set up chup anh
        val prPicture = pr.supportedPictureSizes
        var heightPr = prPicture[0].height
        var widthPr = prPicture[0].width
        for (i in prPicture){
            if (i.height == hTT && i.width == w){
                heightPr = i.height
                widthPr = i.width
            }
        }
        pr.setPictureSize(widthPr, heightPr)

        camera?.parameters = pr

        zoomHandler = ZoomHandler(camera, findViewById<View>(R.id.camView).context)
        //do du lieu lieu
        camera?.setPreviewTexture(surface)
        camera?.startPreview()
    }

    override fun onClick(view: View) {
        try {
            view.isEnabled = false
            Handler(Looper.myLooper()!!).postDelayed({
                view.isEnabled = true
            }, 2000)
        } catch (ex: Exception) {

        }
        when (view) {
            shotBtn -> {
                click()
            }
            checkDone -> {
                submit()
            }
            settingsB -> {
                flash = !flash
                if (flash) {
                    val parameters = camera!!.parameters
                    parameters.flashMode = Camera.Parameters.FLASH_MODE_ON
                    settingsB!!.setImageResource(R.drawable.ic_flash_auto)
                    camera!!.parameters = parameters
                } else {
                    val parameters = camera!!.parameters
                    parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                    settingsB!!.setImageResource(R.drawable.ic_non_flash)
                    camera!!.parameters = parameters
                }
                getSharedPreferences("PDFtools", MODE_PRIVATE).edit().putBoolean("flash", flash)
                    .apply()
            }
        }
    }

    private fun click() {
        val temp = dir!!.listFiles()
        val files = temp ?: arrayOf()
        try {
            countView!!.text = (files.size + 1).toString()
            camera!!.takePicture(
                {
                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(500)
                }
                , null, { data, k ->

                    run {
//                    data la mang byte
                        val bm: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                        //quay anh
                        val matrix = Matrix()
                        matrix.postRotate(90f)
                        val rotatedBitmap = Bitmap.createBitmap(
                            bm, 0, 0,
                            bm.width, bm.height,
                            matrix, true
                        )
                        bm.recycle()
                        val f = File(dir, Statics.randString() + ".jpg")
                        val out = FileOutputStream(f)
                        rotatedBitmap.compress(
                            Bitmap.CompressFormat.JPEG, 100,
                            out
                        )
                        count += 1
                        out.close()

                        stopCamera()
                        if (nextB!!.visibility == View.INVISIBLE) {
                            nextB!!.visibility = View.VISIBLE
                            checkDone!!.visibility = View.VISIBLE
                            countView!!.visibility = View.VISIBLE
                        }
                        startScan(f)
                        openCamera()
                    }


                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun submit() {
        val intent = Intent(this, EditViewActivity::class.java)
        val b = Bundle()
        b.putParcelableArrayList("content", images)
        intent.putExtras(b)
        startActivity(intent)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        camera?.stopPreview()
        camera?.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onBackPressed() {
        finish()
    }

    fun onScanFinish() {
        try {
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commit()
            currentFragment = null
        }catch (ex:Exception){

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

    private fun openCamera() {
        if (camera != null) {
            camera?.stopPreview()
            camera?.release()
        }
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        startCamera()
    }

    companion object {
        init {
            Loader.load()
        }
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
    }

    override fun onResume() {
        super.onResume()
        openCamera()
        loadCount()
    }

    fun loadLastImage() {
        try {
            Glide.with(this)
                .load(Utils.getBitmap(this, Uri.fromFile(dir!!.listFiles()[dir!!.list().size - 1])))
                .thumbnail(0.2f)
                .into(nextB!!)
        } catch (e: Exception) {
        }
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
            nextB!!.visibility = View.INVISIBLE
            checkDone!!.visibility = View.INVISIBLE
            countView!!.visibility = View.INVISIBLE
        }
    }
}
