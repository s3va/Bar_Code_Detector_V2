package tk.kvakva.barcodedetectorv2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import tk.kvakva.barcodedetectorv2.databinding.ActivityMainBinding
import java.io.IOException


private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    //private val baViewModel: BaViewModel by viewModels()
    private val baViewModel by viewModels<BaViewModel>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.xmlViewModel = baViewModel
        binding.lifecycleOwner = this

        /*if (allPermissionsGranted()) {
           // Log.i("oiu", "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg")
          //  startScan()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }*/
        var lockOb = false
        val observer = Observer<Boolean> {
            Log.i("obs", "----------- observer start")
            if (lockOb) {
                Log.e(
                    "Wawu",
                    "WWWWWWWWWWWWWEEEEEEEEEEEEEEE CATCH LOGK RACE CONDITION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                )
                return@Observer
            }
            lockOb = true
            if (it) {
                Log.i(
                    "asdfasdf",
                    "val observer = Observer<Boolean>{ IT TRUE ^^^^^^^^^^^^^^^^^^^^^^^^^^"
                )
                //binding.cameraPreview.visibility= View.GONE
                //binding.cameraPreview.visibility=View.VISIBLE
                //frameforsurfaceview.removeView(cameraPreview)

                loutconstr.removeView(cameraPreview)
                loutconstr.removeView(flashBttn)
                loutconstr.removeView(foldunfuldBttn)
                //frameforsurfaceview.addView(cameraPreview)
                loutconstr.addView(cameraPreview)
                loutconstr.addView(flashBttn)
                loutconstr.addView(foldunfuldBttn)

                binding.flashBttn.setOnLongClickListener {
                    val camera = getCmra(cameraSource)
                    Log.i("fffffffff","ffffffffff setOnLongClickListener -> $camera ffffffffffffffff")
                    if(camera != null) {
                        try {
                            val param = camera.parameters
                            Log.i(
                                "fffffffff",
                                "fffffffffffffffffffffff ${param.flashMode}  fffffff"
                            )
                            param.flashMode = when (param.flashMode) {
                                Camera.Parameters.FLASH_MODE_OFF -> Camera.Parameters.FLASH_MODE_TORCH
                                else -> Camera.Parameters.FLASH_MODE_OFF
                            }
                            camera.parameters = param
                            Log.i(
                                "fffffffff",
                                "fffffffffffffffffffffff ${param.flashMode}  fffffff"
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return@setOnLongClickListener  true
                }


                if(allPermissionsGranted()) {
                    startScan()
                } else {
                    ActivityCompat.requestPermissions(
                        this, REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }
            } else {
                Log.i(
                    "asdfasdf",
                    "val observer = Observer<Boolean>{ IT FALSE ^^^^^^^^^^^^^^^^^^^^^^^^^^"
                )
                if(this::cameraSource.isInitialized) {
                    cameraSource.release()
                    Log.i("MAct","release cammera")
                    if(barcodeDetector.isOperational)
                        barcodeDetector.release()
                } else
                    Log.i("MAct","stop alreadi stopped cammera")
            }
            Log.i("obs", "-------- obs end --------")
            lockOb = false
        }

        baViewModel.recrd.observe(this, observer)
    }

    private fun startScan() {
        Log.i(
            "asdfasdfasdfa",
            "!!!!!!!!!!!!!!!!!! aaaaaaaaaaaaaaaaaaaaaa !!!!!!!!! START SCAN !!!!!! STARTED!!!!!!!!!!!!!!!!!!!"
        )

        barcodeDetector = BarcodeDetector.Builder(this).build()

        cameraSource =
            CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1280, 720)
                .setAutoFocusEnabled(true)
                .build()

        cameraPreview.holder.addCallback(Callbackm())

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {

            override fun receiveDetections(p0: Detector.Detections<Barcode>?) {

                val qrCode = p0?.detectedItems
                if (qrCode?.size() != 0) {
                    textView.post {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                                VibrationEffect.createOneShot(100, 10)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(100)
                        }
                        val s = StringBuilder()
                        qrCode?.forEach { _, value ->
                            s.append(value.displayValue)
                            s.append("\n")
                        }
                        Log.i(
                            "detected",
                            "__________!!!!!!!!!!!!WWWWOOOEEEE $s WWWWOOOEEEE!!!!!!!!!!!______"
                        )
                        baViewModel.setTextQR(s.toString())
                        //textView.text = s.toString()
                        baViewModel.stopRecrd()
                    }
                }
            }

            override fun release() {
                Log.i(
                    "ASDASD",
                    "*************** _________  barcodeDetector.setProcessor(object : Detector.Processor<Barcode> { un release() RUNNING _________**********"
                )
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    inner class Callbackm : SurfaceHolder.Callback {
        /**
         * This is called immediately after any structural changes (format or
         * size) have been made to the surface.  You should at this point update
         * the imagery in the surface.  This method is always called at least
         * once, after [.surfaceCreated].
         *
         * @param holder The SurfaceHolder whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param width The new width of the surface.
         * @param height The new height of the surface.
         */
        override fun surfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            //                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        /**
         * This is called immediately before a surface is being destroyed. After
         * returning from this call, you should no longer try to access this
         * surface.  If you have a rendering thread that directly accesses
         * the surface, you must ensure that thread is no longer touching the
         * Surface before returning from this function.
         *
         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            cameraSource.stop()
        }

        /**
         * This is called immediately after the surface is first created.
         * Implementations of this should start up whatever rendering code
         * they desire.  Note that only one thread can ever draw into
         * a Surface], so you should not draw into the Surface here
         * if your normal rendering will be in another thread.
         *
         * @param holder The SurfaceHolder whose surface is being created.
         */
        override fun surfaceCreated(holder: SurfaceHolder?) {

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                } else {
                    // TODO("VERSION.SDK_INT < M")
                    false
                }
            ) return

            try {
                cameraSource.start(holder)
                Log.i("Cameranholder", "~~~~~~~~~~~~~~~~~~~~~~~~~~ CAMERA PREVIEW SIZE: ${cameraSource.previewSize} ~~~~~~~~~~~~~~~~~~~~~~~")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //cameraPreview.post { startScan() }
                //startScan()
                baViewModel.stopRecrd()
                baViewModel.recrdClick()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCmra(camSrc: CameraSource) : Camera? {
        val declFld = CameraSource::class.java.declaredFields

        declFld.forEach {
            if(it.type == Camera::class.java) {
                Log.i("ffffffffff","ffffffffffffffffffffffffffffff $it ffffff")
                it.isAccessible = true
                try {
                    val camRa: Camera? = it.get(camSrc) as Camera?
                    Log.i("ffffffffff","ffffffffffffffffffffffffffffff $camRa ffffff")
                    if(camRa != null)
                        return camRa
                    return null
                }catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                return@forEach
            }
        }
        return null
    }


}

class BaViewModel : ViewModel() {

    private val _recrd = MutableLiveData(true)
    val recrd: LiveData<Boolean> = _recrd

    private val _textQR = MutableLiveData("NoThing Scanned yet")
    val textQR: LiveData<String> = _textQR

    fun setTextQR(s: String){
        _textQR.value=s
    }

    private val _textMLine = MutableLiveData(false)
    val textMLine: LiveData<Boolean> = _textMLine

    fun setMLine(){
        _textMLine.value=(textMLine.value?:false).not()
    }

    init {
        Log.i("BaV", "INIT")
    }

    private fun startRecrd() {
        Log.i(
            "BaV",
            "private fun startRecrd() ************* _recrd.value = true was ${recrd.value}"
        )
        if (recrd.value == false)
            _recrd.value = true
        else
            Log.e("BaV", "private fun startRecrd() OOOOOGGGOOOGGGOO ")
    }

    fun stopRecrd() {
        Log.i("BaV", "fun stopRecrd() ************* _recrd.value = false was ${recrd.value}")
        if (recrd.value != false)
            _recrd.value = false
        else
            Log.e("BaV", "fun stopRecrd()  OOOOOGGGOOOGGGOO ")

    }

    fun recrdClick() {
        if (recrd.value == true)
            stopRecrd()
        else
            startRecrd()
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     *
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        Log.i("BaV", "!!!onCleared !!!")
    }
}
