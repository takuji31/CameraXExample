package dev.takuji31.camerax

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import dev.takuji31.camerax.ui.theme.CameraXExampleTheme

class MainActivity : ComponentActivity() {
    private var isPermissionGranted by mutableStateOf(false)
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in Manifest.permission.CAMERA && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                isPermissionGranted = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityResultLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        } else {
            isPermissionGranted = true
        }


        var controller = LifecycleCameraController(this)
        controller.bindToLifecycle(this)
        controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        setContent {
            CameraXExampleTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Box(modifier = Modifier.weight(4f)) {
                            if (isPermissionGranted) {
                                AndroidView(
                                    factory = { context ->
                                        PreviewView(context).apply {
                                            setController(controller)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    onRelease = { controller.unbind() }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(text = "Permission not granted")
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Button(onClick = {
                                val cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                controller.cameraSelector = cameraSelector
                            }) {
                                Text(text = "reverse")
                            }
                            Button(onClick = {
                                val values = ContentValues().apply {
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                }
                                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                                controller.takePicture(ImageCapture.OutputFileOptions.Builder(contentResolver.openOutputStream(imageUri!!)!!).build(), ContextCompat.getMainExecutor(this@MainActivity), object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        Toast.makeText(this@MainActivity, "Image captured", Toast.LENGTH_SHORT).show()

                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Toast.makeText(this@MainActivity, "Image capture failed", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }) {
                                Text(text = "Capture")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CameraXExampleTheme {
        Greeting("Android")
    }
}
