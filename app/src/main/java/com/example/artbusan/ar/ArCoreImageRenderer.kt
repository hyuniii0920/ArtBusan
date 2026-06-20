package com.example.artbusan.ar

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Camera
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ArCoreImageRenderer(
    private val displayRotationProvider: () -> Int,
    private val listener: Listener
) : GLSurfaceView.Renderer {

    interface Listener {
        fun onMarkerDetected(marker: ArMarker)
        fun onMarkerTrackingLost(marker: ArMarker)
        fun onArFrameError(error: Throwable)
    }

    @Volatile
    var session: Session? = null

    private val backgroundRenderer = CameraBackgroundRenderer()
    private val overlayRenderer = MarkerOverlayRenderer()
    private var viewportWidth = 0
    private var viewportHeight = 0
    private var currentImage: AugmentedImage? = null
    private var currentMarkerName: String? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        backgroundRenderer.createOnGlThread()
        overlayRenderer.createOnGlThread()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES20.glViewport(0, 0, width, height)
        session?.setDisplayGeometry(displayRotationProvider(), width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val activeSession = session ?: return

        try {
            if (viewportWidth > 0 && viewportHeight > 0) {
                activeSession.setDisplayGeometry(displayRotationProvider(), viewportWidth, viewportHeight)
            }
            activeSession.setCameraTextureName(backgroundRenderer.textureId)
            val frame = activeSession.update()
            backgroundRenderer.draw(frame)

            val camera = frame.camera
            updateCurrentMarker(frame.getUpdatedTrackables(AugmentedImage::class.java))
            currentImage?.let { image ->
                if (image.trackingState == TrackingState.TRACKING && camera.trackingState == TrackingState.TRACKING) {
                    overlayRenderer.draw(image, camera)
                } else {
                    notifyMarkerLostIfNeeded()
                }
            }
        } catch (error: CameraNotAvailableException) {
            listener.onArFrameError(error)
        } catch (error: RuntimeException) {
            listener.onArFrameError(error)
        }
    }

    fun resetTrackingState() {
        currentImage = null
        currentMarkerName = null
    }

    private fun updateCurrentMarker(updatedImages: Collection<AugmentedImage>) {
        updatedImages.forEach { image ->
            when (image.trackingState) {
                TrackingState.TRACKING -> {
                    val marker = ArMarkerRegistry.findByName(image.name) ?: return@forEach
                    currentImage = image
                    if (currentMarkerName != marker.name) {
                        currentMarkerName = marker.name
                        listener.onMarkerDetected(marker)
                    }
                }

                TrackingState.PAUSED,
                TrackingState.STOPPED -> {
                    if (currentMarkerName.equals(image.name, ignoreCase = true)) {
                        notifyMarkerLostIfNeeded()
                    }
                }
            }
        }
    }

    private fun notifyMarkerLostIfNeeded() {
        val marker = ArMarkerRegistry.findByName(currentMarkerName) ?: return
        currentImage = null
        currentMarkerName = null
        listener.onMarkerTrackingLost(marker)
    }
}

private class CameraBackgroundRenderer {

    var textureId: Int = 0
        private set

    private var program = 0
    private var positionAttribute = 0
    private var texCoordAttribute = 0
    private var textureUniform = 0

    private val quadVertices = floatBufferOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    private val transformedQuadTexCoords = floatBufferOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    fun createOnGlThread() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionAttribute = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordAttribute = GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureUniform = GLES20.glGetUniformLocation(program, "sTexture")
    }

    fun draw(frame: Frame) {
        if (textureId == 0 || program == 0) return

        if (frame.hasDisplayGeometryChanged()) {
            quadVertices.position(0)
            transformedQuadTexCoords.position(0)
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadVertices,
                Coordinates2d.TEXTURE_NORMALIZED,
                transformedQuadTexCoords
            )
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glUseProgram(program)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureUniform, 0)

        quadVertices.position(0)
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 0, quadVertices)
        GLES20.glEnableVertexAttribArray(positionAttribute)

        transformedQuadTexCoords.position(0)
        GLES20.glVertexAttribPointer(texCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, transformedQuadTexCoords)
        GLES20.glEnableVertexAttribArray(texCoordAttribute)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glDisableVertexAttribArray(texCoordAttribute)
        GLES20.glDepthMask(true)
    }

    private companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
              gl_Position = a_Position;
              v_TexCoord = a_TexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 v_TexCoord;
            uniform samplerExternalOES sTexture;
            void main() {
              gl_FragColor = texture2D(sTexture, v_TexCoord);
            }
        """
    }
}

private class MarkerOverlayRenderer {

    private var program = 0
    private var positionAttribute = 0
    private var mvpUniform = 0
    private var colorUniform = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    fun createOnGlThread() {
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionAttribute = GLES20.glGetAttribLocation(program, "a_Position")
        mvpUniform = GLES20.glGetUniformLocation(program, "u_MvpMatrix")
        colorUniform = GLES20.glGetUniformLocation(program, "u_Color")
    }

    fun draw(image: AugmentedImage, camera: Camera) {
        if (program == 0) return

        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
        camera.getViewMatrix(viewMatrix, 0)
        image.centerPose.toMatrix(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0.006f, 0f)
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        val width = image.extentX.takeIf { it > 0f } ?: 0.21f
        val height = image.extentZ.takeIf { it > 0f } ?: 0.30f
        val quad = floatBufferOf(
            -width / 2f, 0f, -height / 2f,
            width / 2f, 0f, -height / 2f,
            width / 2f, 0f, height / 2f,
            -width / 2f, 0f, height / 2f
        )
        val headerHeight = height * 0.22f
        val header = floatBufferOf(
            -width / 2f, 0f, -height / 2f,
            width / 2f, 0f, -height / 2f,
            width / 2f, 0f, -height / 2f + headerHeight,
            -width / 2f, 0f, -height / 2f + headerHeight
        )

        GLES20.glUseProgram(program)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0)

        drawBuffer(quad, GLES20.GL_TRIANGLE_FAN, floatArrayOf(0.03f, 0.14f, 0.24f, 0.38f))
        drawBuffer(header, GLES20.GL_TRIANGLE_FAN, floatArrayOf(0.0f, 0.7f, 1.0f, 0.52f))
        GLES20.glLineWidth(8f)
        drawBuffer(quad, GLES20.GL_LINE_LOOP, floatArrayOf(0.32f, 0.88f, 1.0f, 0.95f))

        GLES20.glDisableVertexAttribArray(positionAttribute)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private fun drawBuffer(buffer: FloatBuffer, mode: Int, color: FloatArray) {
        buffer.position(0)
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, buffer)
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glUniform4fv(colorUniform, 1, color, 0)
        GLES20.glDrawArrays(mode, 0, 4)
    }

    private companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 u_MvpMatrix;
            attribute vec4 a_Position;
            void main() {
              gl_Position = u_MvpMatrix * a_Position;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
              gl_FragColor = u_Color;
            }
        """
    }
}

private fun floatBufferOf(vararg values: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(values.size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(values)
            position(0)
        }
}

private fun createProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)
    return GLES20.glCreateProgram().also { program ->
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            error("Could not link OpenGL program: $log")
        }
    }
}

private fun loadShader(type: Int, shaderSource: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderSource.trimIndent())
        GLES20.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            error("Could not compile OpenGL shader: $log")
        }
    }
}

private const val FLOAT_SIZE_BYTES = 4
