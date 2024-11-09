package com.example.randomizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.randomizer.ui.theme.RandomizerTheme

class MainActivity : ComponentActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RandomizerTheme {
                AppNavGraph()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        speechRecognizer.stopListening()
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent(Intent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(Intent.EXTRA_LANGUAGE_MODEL, "en-US")
            speechRecognizer.startListening(intent)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                1)
        }
    }

    private fun takePhoto() {
        // This could open the camera app or use CameraX to take a photo
        Toast.makeText(this, "Taking photo!", Toast.LENGTH_SHORT).show()

        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // Process the photo here
        }
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("next") { NextScreen() }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    Toast.makeText(
                        navController.context,
                        "Navigating to Next Screen!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate("next")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Tap anywhere to proceed")
        }
    }
}

@Composable
fun NextScreen() {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    // Start listening when the screen appears
    LaunchedEffect(Unit) {
        // Start speech recognition as soon as the screen appears
        startListening(speechRecognizer)
    }

    // Set up the recognition listener
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.contains("scan")) {
                takePhoto(context)
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    // Show UI for NextScreen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Say 'scan' to take a photo")
    }
}

private fun takePhoto(context: Context) {
    // This could open the camera app or use CameraX to take a photo
    Toast.makeText(context, "Taking photo!", Toast.LENGTH_SHORT).show()

    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
    context.startActivity(cameraIntent)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    RandomizerTheme {
        MainScreen(navController = rememberNavController())
    }
}
