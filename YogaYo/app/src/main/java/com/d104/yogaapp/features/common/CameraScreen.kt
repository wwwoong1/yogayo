package com.d104.yogaapp.features.common

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.d104.yogaapp.R
import timber.log.Timber
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.speech.tts.TextToSpeech
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.domain.model.YogaPose
import java.util.Locale


@Composable
fun YogaPlayScreen(
    isCountingDown: Boolean = false,
    isTutorial:Boolean = false,
    pose:YogaPose = YogaPose(0,"","",0, listOf(),"",0,""),
    timerProgress: Float,
    isPlaying: Boolean,
    onPause: () -> Unit,
    leftContent: @Composable () -> Unit,
    onSendResult: (YogaPose,Float,Float,Bitmap) -> Unit ={_,_,_,_->},
    onAccuracyUpdate:(Float,Float)->Unit = {_,_->}
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // TTS 인스턴스 생성
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    var currentDescriptionIndex by remember { mutableStateOf(-1) } // -1로 시작하면 아직 설명 시작 전
    var isTtsReady by remember { mutableStateOf(false) }

    // TTS 초기화
    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.getDefault()
                isTtsReady = true
            }
        }
    }


    // 컴포넌트 해제 시 TTS 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.stop()
            textToSpeech.value?.shutdown()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 메인 콘텐츠 영역 (가로 배치)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 왼쪽 콘텐츠 (GIF) - 40%
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .background(Color.White)
                    .paint(
                        painterResource(id = R.drawable.bg_double_border),
                        contentScale = ContentScale.FillBounds
                    )
                    .fillMaxHeight()
                    .padding(end=5.dp,bottom=16.dp)
            ) {
                leftContent()
            }
            Spacer(modifier = Modifier
                .weight(0.02f))

            // 오른쪽 콘텐츠 (카메라) - 60%
            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
            ) {
                // 카메라 프리뷰 - isPlaying 상태 전달
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isPlaying,
                    onSendResult = onSendResult,
                    pose = pose,
                    isCountingDown = isCountingDown,
                    onRessultFeedback = {accuracy,time,feedback->
                        onAccuracyUpdate(accuracy,time )
                        if (feedback.isNotEmpty() && isTtsReady&&isPlaying) {
                            textToSpeech.value?.let{textToSpeech->
                                if(!textToSpeech.isSpeaking){
                                textToSpeech.speak(
                                    feedback,
                                    TextToSpeech.QUEUE_ADD, // QUEUE_FLUSH 대신 QUEUE_ADD 사용
                                    null,
                                    "text_${System.currentTimeMillis()}" // 고유한 식별자 사용
                                )
                                    }

                            }
                        }
                    },
                    isReset = timerProgress==1f
                )

                // 일시정지/재생 버튼
                if(isPlaying&&!isCountingDown) {
                    IconButton(
                        onClick = onPause,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "일시정지",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                if(!isTutorial) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 타이머 프로그레스 바
                        LinearProgressIndicator(
                            progress = { timerProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(36.dp)),
                            color = Color(0xFF2196F3),
                            trackColor = Color(0x80FFFFFF) // 반투명 흰색
                        )

                        // 타이머 아이콘 (프로그레스 바 위에 겹치게)
                        Image(
                            painter = painterResource(id = R.drawable.img_timer),
                            contentDescription = "타이머",
                            modifier = Modifier
                                .size(54.dp)
                                .offset(x = (-6).dp, y = -4.dp), // 약간 왼쪽으로 이동
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    pose: YogaPose = YogaPose(0,"","",0, listOf(),"",0,""),
    onSendResult: (YogaPose,Float,Float, Bitmap) -> Unit={_,_,_,_->},
    viewModel: CameraViewModel = hiltViewModel(),
    isCountingDown: Boolean = false,
    onRessultFeedback: (Float,Float,String) -> Unit={_,_,_->},
    isReset:Boolean = false
) {
//    Timber.d("camera countDown ${isCountingDown}4")
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val accuracy by viewModel.displayAccuracy.collectAsState()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    // *** ViewModel의 Helper 준비 상태 관찰 ***
    val isHelperReady by viewModel.isHelperReady.collectAsState()
    // *** ViewModel의 결과 및 에러 관찰 (UI 표시에 사용) ***
    val poseResult by viewModel.poseResult.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val feedback by viewModel.feedback.collectAsState()

    // PreviewView 구성 (변경 없음)
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }


    // 바인딩 함수 (내부 조건 제거, isHelperReady로 외부에서 제어)
    val bindCamera = remember(viewModel.imageAnalyzer, cameraProvider, preview, imageCapture) {
        // imageAnalyzer 변경 외에도 provider, preview, capture 준비 시 재실행 필요할 수 있음
        {
            // isHelperReady 조건은 LaunchedEffect에서 처리하므로 여기선 제거
            if (!(cameraProvider == null || preview == null || imageCapture == null)) {
                try {
                    Timber.d("camera countDown ${isCountingDown}2")
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                    // ImageAnalysis 설정 (Analyzer는 ViewModel에서 가져옴)
                    imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480)) // 해상도 확인
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            // *** ViewModel의 Analyzer 사용 ***
                            setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                viewModel.imageAnalyzer
                            )
                            setAnalyzer(
                                viewModel.imageAnalyzerExecutor,
                                viewModel.imageAnalyzer
                            )


                        }

                    cameraProvider?.unbindAll()

                    Timber.d("Attempting to bind Preview, ImageCapture, ImageAnalysis...")
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis // ImageAnalysis 추가
                    )
                    Timber.d("Camera bound successfully.")
                } catch (e: Exception) {
                    Timber.e("카메라 바인딩 실패", e)
                    // ViewModel의 에러 상태 업데이트 또는 직접 처리
                    // viewModel.setError("Camera binding failed: ${e.message}")
                }


            }
        }
    }

    // 초기 카메라 설정 (변경 없음)
    DisposableEffect(Unit) {
        val cameraListener = Runnable {
            try {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                // imageCapture도 여기서 초기화하는 것이 좋음
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Provider, Preview, ImageCapture 준비 완료 후 바인딩 시도 (LaunchedEffect에서)
            } catch (e: Exception) {
                Timber.e("카메라 Provider/Preview/Capture 초기화 실패", e)
            }
        }
        cameraProviderFuture.addListener(cameraListener, ContextCompat.getMainExecutor(context))

        onDispose {
            Timber.d("CameraPreview onDispose triggered.")

            // *** ViewModel의 확인 함수를 먼저 호출하여 초기화 여부 확인 ***
            if (viewModel.isCurrentPoseInitialized()) {
                // currentPose가 초기화되었으므로 이제 안전하게 접근 가능
                Timber.d("currentPose is initialized. Proceeding with onSendResult logic.")

                val bitmapToSend = viewModel.bestResultBitmap ?: createPlaceholderBitmap(context)

                bitmapToSend?.let { bitmap ->
                    // 여기서 viewModel.currentPose 접근은 안전함
                    val accuracyToSend = if (viewModel.bestResultBitmap == null) 0f else viewModel.bestAccuracy
                    val timeToSend = if (viewModel.bestResultBitmap == null) 0f else Math.min(viewModel.remainingPoseTime, 20.0f)

                    Timber.d("Calling onSendResult with bitmap. Accuracy: $accuracyToSend, Time: $timeToSend")
                    onSendResult(
                        viewModel.currentPose, // 안전한 접근
                        accuracyToSend,
                        timeToSend,
                        bitmap
                    )
                } ?: run {
                    Timber.w("CameraPreview onDispose: No bitmap found even after checking initialization.")
                    // 비트맵이 없을 경우, 초기화된 currentPose와 함께 기본값으로 보낼지 결정
                    // onSendResult(viewModel.currentPose, 0f, 0f, createPlaceholderBitmap(context)!!) // <- Placeholder가 null 아님을 보장해야 함
                }

            } else {
                // currentPose가 초기화되지 않았음. 접근하면 안 됨.
                Timber.w("CameraPreview onDispose: currentPose was NOT initialized. Skipping related logic.")
                // 이 경우 onSendResult를 호출하지 않거나, Pose 정보 없이 기본값으로 호출?
                // val bitmapToSend = createPlaceholderBitmap(context)
                // bitmapToSend?.let { onSendResult(YogaPose.DEFAULT, 0f, 0f, it) } // YogaPose.DEFAULT 같은 기본값 필요
            }

            // 카메라 언바인드는 Pose 초기화 여부와 상관없이 항상 실행
            try {
                cameraProvider?.unbindAll()
                Timber.d("CameraPreview disposed and camera unbound.")
            } catch (e: Exception) {
                Timber.e(e, "Error during camera unbind in onDispose.")
            }
        }
    }

    LaunchedEffect(isReset) {
        if(isReset){
            viewModel.resetData()
        }
    }
    LaunchedEffect(isCountingDown) {
        Timber.d("isCountingDown!!!: ${isCountingDown}")
        viewModel.setAnalysisPaused(isCountingDown)
    }

    LaunchedEffect(pose) {

        viewModel.initPose(pose)
    }

    LaunchedEffect(accuracy,feedback) {
        //여기서 피드백 보내는거해야할듯
        onRessultFeedback(accuracy,viewModel.remainingPoseTime,feedback)

    }

    // *** isPlaying 및 isHelperReady 상태에 따른 처리 ***
    LaunchedEffect(isPlaying, isHelperReady, cameraProvider, preview, imageCapture) {
        if (cameraProvider != null && preview != null) { // 모든 UseCase 준비 확인
            if (isPlaying && isHelperReady) { // 재생 중이고 Helper 준비 완료 시
                Timber.d("isPlaying is true and Helper is ready. Binding camera with ImageAnalysis.")
                bindCamera() // 카메라 바인딩 (ImageAnalysis 포함)
            } else if (isPlaying && !isHelperReady) { // 재생 중이지만 Helper 준비 안됨
                Timber.d("isPlaying is true, but waiting for PoseLandmarkerHelper to be ready...")
                // 필요 시 로딩 표시
                cameraProvider?.unbindAll() // 분석 없이 프리뷰만 보려면 Preview만 바인딩
            } else { // isPlaying이 false일 때
                Timber.d("isPlaying is false. Unbinding all.")
                cameraProvider?.unbindAll() // 모든 바인딩 해제
            }
        } else {
            Timber.d("CameraProvider, Preview or ImageCapture not ready yet.")
        }
    }

    // UI 부분
    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // *** 에러 메시지 표시 (예시) ***
        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }

        // *** 포즈 결과 시각화 (예시) ***
        poseResult?.let { bundle ->
            // bundle.results.firstOrNull()?.landmarks() 사용하여 Canvas 등에 그리기
            // 예: DrawLandmarksOverlay(result = bundle.results.first())
        }

        // 로딩 표시 (예시)
        if (isPlaying && !isHelperReady && errorMessage == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

fun createPlaceholderBitmap(context: Context): Bitmap? {
    // 1. 리소스에서 이미지 로드 (추천)
    return try {
        BitmapFactory.decodeResource(context.resources, R.drawable.tmp) // 적절한 기본 이미지 리소스로 변경
    } catch (e: Exception) {
        Timber.e(e, "Failed to load placeholder bitmap from resources.")
        // 2. 간단한 색상 비트맵 생성 (리소스 없을 경우 대체)
        try {
            val width = 100
            val height = 100
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { color =  android.graphics.Color.GRAY} // 회색 배경
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            // 필요시 텍스트 추가
            // paint.color = Color.WHITE
            // paint.textSize = 20f
            // paint.textAlign = Paint.Align.CENTER
            // canvas.drawText("No Image", width / 2f, height / 2f, paint)
            bitmap
        } catch (e2: Exception) {
            Timber.e(e2, "Failed to create fallback placeholder bitmap.")
            null // 생성 실패 시 null 반환
        }
    }
}
// CameraViewModel에 imageAnalyzerExecutor 추가 필요
// @HiltViewModel class CameraViewModel ... {
//     val imageAnalyzerExecutor: ExecutorService = cameraExecutor // 또는 별도 생성
// }