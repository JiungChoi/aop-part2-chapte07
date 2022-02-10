package jiung.fastcampus.aop.part2.chapte07

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById(R.id.soundVisualizeView)
    }

    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }

    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }
    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.recordTimeTextView)
    }
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val requiredPermissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var state: State = State.BRFORE_RECORDING
        set(value) { //state optional
            field = value // 실제 프로퍼티에 값을 넣어준다.
            resetButton.isEnabled = (value == State.AFTER_RECORDING) || (value == State.ON_PLAYING)
            recordButton.updateIconWithState(value) // 이미지를 변환시킨다.
        }

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initView()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted) {
            finish()
        }
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initView() {
        recordButton.updateIconWithState(state)
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // 마이크 지정
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // 컨테이너 포맷(아웃풋 포맷) 지정
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // 인코더 설정
            setOutputFile(recordingFilePath) // 녹음된 오디오를 압축해서 저장 할 아웃풋 파일 지정
            prepare()
        }
        recorder?.start()
        recordTimeTextView.startCountUp()
        soundVisualizerView.startVisualizing(false)
        state = State.ON_RECORDING
    }

    private fun stopRecording() {
        recorder?.run {
            stop() //stop
            release() //메모리해제
        }
        recorder = null //메모리해제 후 null로 초기화
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun startPlaying() {
        player = MediaPlayer()
            .apply {
                setDataSource(recordingFilePath)
                prepare()
            }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        recordTimeTextView.startCountUp()
        state = State.ON_PLAYING
    }

    private fun bindViews() {
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0 // 값이 null 일 경우 0을 리턴
        }

        resetButton.setOnClickListener {
            stopPlaying()
            state = State.BRFORE_RECORDING
        }
        recordButton.setOnClickListener {
            when (state) {
                State.BRFORE_RECORDING -> {
                    startRecording()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING -> {
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }
    }

    private fun initVariables() {
        state = State.BRFORE_RECORDING
    }

    private fun stopPlaying() {
        player?.release()
        recordTimeTextView.stopCountUp()
        player = null
        soundVisualizerView.stopVisualizing()
        state = State.AFTER_RECORDING
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }


}