package jiung.fastcampus.aop.part2.chapte07

import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import java.util.*
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
) :View(context, attrs){

    var onRequestCurrentAmplitude: (() -> Int)? = null // type 은 빈 파라미터를 보내서 앰플리튜드로 전달받는 형태로 구성


    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND // Cap : 라인의 양 끝을 어떻게 표시할 것인가
    } // ALIAS : 곡선이 부드럽게 그려짐 Paint 에서, 각진것
    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    //var drawingAmplitudes : List<Int> = (0..10).map { Random.nextInt(Short.MAX_VALUE.toInt()) }
    private var drawingAmplitudes : List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0

    private val visualizeRepeatAction: Runnable = object : Runnable{
        override fun run() {
            // Amplitude, Draw
            if (isReplaying) {
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                replayingPosition++
            }

            invalidate() // 이거 호출하지 않으면 데이터는 추가되는데 뷰가 갱신이 안 됨

            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f
        var offsetX: Float = drawingWidth.toFloat()

        drawingAmplitudes
            .let{ amplitudes ->
                if (isReplaying){
                    amplitudes.takeLast(replayingPosition)
                } else {
                    amplitudes
                }
            }
            .forEach { amplitude ->
            val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

            offsetX -= LINE_SPACE
            if(offsetX < 0) return@forEach


            canvas.drawLine(
                offsetX,
                centerY - lineLength /2F,
                offsetX,
                centerY+lineLength /2F,
                amplitudePaint
            )
        }

    }

    fun startVisualizing(isReplaying: Boolean){
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }
    fun stopVisualizing(){
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    companion object{
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE = 15F
        private  const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat() //32767
        private const val ACTION_INTERVAL = 20L
    }

}