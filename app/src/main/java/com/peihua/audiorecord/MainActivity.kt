package com.peihua.audiorecord

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peihua.audiorecord.ui.theme.AudioRecordTheme
import com.peihua8858.permissions.core.requestPermissions
import kotlinx.coroutines.launch
import java.io.File
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioRecordTheme {
                val isRecord = remember { mutableStateOf(true) }
                val isPlayPcm = remember { mutableStateOf(true) }
                val isPlayMp3 = remember { mutableStateOf(true) }
                val logs = remember { mutableStateListOf<String>() }
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val parentFile = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val pcmFile = File(parentFile, "test.pcm")
                val mp3File = File(parentFile, "test.mp3")
                AudioRecordManager.getInstance().setPcmFilePath(pcmFile.absolutePath)
                AudioRecordManager.getInstance().setFilePath(mp3File.absolutePath)
                AudioRecordManager.getInstance().setAddLog {
                    logs.add(0, it)
                }
                AudioRecordManager.getInstance().setUpdateStatus {
//                    logs.add(it)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            startActivity(Intent(context, Mp3AudioRecordActivity::class.java))
                        }) {
                            Text("录音二级页")
                        }
                        Button(onClick = {
                            dLog { "requestPermissions" }
                            requestPermissions(android.Manifest.permission.RECORD_AUDIO) {
                                onGranted {
                                    dLog { "onGranted>>>>" }
                                    val isStarting = isRecord.value
                                    isRecord.value = !isRecord.value
//                                    logs.clear()
                                    scope.launch {
                                        dLog { "startRecordPcm>>>>" }
                                        AudioRecordManager.getInstance()
                                            .setRecording(isStarting)
                                        thread {
                                            if (isStarting) {
                                                AudioRecordManager.getInstance()
                                                    .startRecordPcm()
                                            }
                                        }
                                    }
                                }
                                onDenied {
                                    dLog { "onDenied>>>>" }
                                    isRecord.value = true
                                }
                            }

                        }) {
                            Text(if (isRecord.value) "开始录音" else "停止录音")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(onClick = {
                            AudioRecordManager.getInstance().startPlayPcm()
                            isPlayPcm.value = !isPlayPcm.value
                        }) {
                            Text(if (isPlayPcm.value) "开始播放Pcm" else "停止播放Pcm")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(onClick = {
//                            logs.clear()
                            scope.launch {
                                AudioRecordManager.getInstance().convertPcmToMp3()
                            }
                        }) {
                            Text("Pcm转mp3")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(onClick = {
//                            logs.clear()
                            scope.launch {
                                AudioRecordManager.getInstance().convertPcmToWav()
                            }
                        }) {
                            Text("Pcm转wav")
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(onClick = {
                            AudioRecordManager.getInstance().startPlayMp3()
                            isPlayMp3.value = !isPlayMp3.value
                        }) {
                            Text(if (isPlayMp3.value) "开始播放Mp3" else "停止播放Mp3")
                        }
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0x99000000))
                                .padding(10.dp),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(10.dp)

                        ) {
                            items(logs.size) {
                                Text(logs[it])
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
    AudioRecordTheme {
        Greeting("Android")
    }
}