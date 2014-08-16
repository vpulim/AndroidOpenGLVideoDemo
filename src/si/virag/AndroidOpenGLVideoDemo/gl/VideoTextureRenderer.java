package si.virag.AndroidOpenGLVideoDemo.gl;


import android.content.Context;
import android.graphics.*;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class VideoTextureRenderer extends TextureSurfaceRenderer implements SurfaceTexture.OnFrameAvailableListener
{
    private static final String hBlurVertexShaderCode =
                    "attribute vec4 vPosition;" +
                    "attribute vec4 vTexCoordinate;" +
                    "precision mediump float;" +
                    "uniform mat4 textureTransform;" +
                    "varying vec2 v_TexCoordinate;" +
                    "varying vec2 v_blurTexCoords[14];" +
                    "void main() {" +
                    "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
                    "   float delta = 1.0 / 2000.0;" +
                    "   gl_Position = vPosition;" +
                    "   v_blurTexCoords[ 0] = v_TexCoordinate + vec2(-delta*7.0, 0.0);" +
                    "   v_blurTexCoords[ 1] = v_TexCoordinate + vec2(-delta*6.0, 0.0);" +
                    "   v_blurTexCoords[ 2] = v_TexCoordinate + vec2(-delta*5.0, 0.0);" +
                    "   v_blurTexCoords[ 3] = v_TexCoordinate + vec2(-delta*4.0, 0.0);" +
                    "   v_blurTexCoords[ 4] = v_TexCoordinate + vec2(-delta*3.0, 0.0);" +
                    "   v_blurTexCoords[ 5] = v_TexCoordinate + vec2(-delta*2.0, 0.0);" +
                    "   v_blurTexCoords[ 6] = v_TexCoordinate + vec2(-delta, 0.0);" +
                    "   v_blurTexCoords[ 7] = v_TexCoordinate + vec2( delta, 0.0);" +
                    "   v_blurTexCoords[ 8] = v_TexCoordinate + vec2( delta*2.0, 0.0);" +
                    "   v_blurTexCoords[ 9] = v_TexCoordinate + vec2( delta*3.0, 0.0);" +
                    "   v_blurTexCoords[10] = v_TexCoordinate + vec2( delta*4.0, 0.0);" +
                    "   v_blurTexCoords[11] = v_TexCoordinate + vec2( delta*5.0, 0.0);" +
                    "   v_blurTexCoords[12] = v_TexCoordinate + vec2( delta*6.0, 0.0);" +
                    "   v_blurTexCoords[13] = v_TexCoordinate + vec2( delta*7.0, 0.0);" +
                    "}";

    private static final String vBlurVertexShaderCode =
                    "attribute vec4 vPosition;" +
                    "attribute vec4 vTexCoordinate;" +
                    "uniform mat4 textureTransform;" +
                    "varying vec2 v_TexCoordinate;" +
                    "varying vec2 v_blurTexCoords[14];" +
                    "void main() {" +
                    "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
                    "   float delta = 1.0 / 2000.0;" +
                    "   gl_Position = vPosition;" +
                    "   v_blurTexCoords[ 0] = v_TexCoordinate + vec2(0.0, -delta*7.0);" +
                    "   v_blurTexCoords[ 1] = v_TexCoordinate + vec2(0.0, -delta*6.0);" +
                    "   v_blurTexCoords[ 2] = v_TexCoordinate + vec2(0.0, -delta*5.0);" +
                    "   v_blurTexCoords[ 3] = v_TexCoordinate + vec2(0.0, -delta*4.0);" +
                    "   v_blurTexCoords[ 4] = v_TexCoordinate + vec2(0.0, -delta*3.0);" +
                    "   v_blurTexCoords[ 5] = v_TexCoordinate + vec2(0.0, -delta*2.0);" +
                    "   v_blurTexCoords[ 6] = v_TexCoordinate + vec2(0.0, -delta);" +
                    "   v_blurTexCoords[ 7] = v_TexCoordinate + vec2(0.0,  delta);" +
                    "   v_blurTexCoords[ 8] = v_TexCoordinate + vec2(0.0,  delta*2.0);" +
                    "   v_blurTexCoords[ 9] = v_TexCoordinate + vec2(0.0,  delta*3.0);" +
                    "   v_blurTexCoords[10] = v_TexCoordinate + vec2(0.0,  delta*4.0);" +
                    "   v_blurTexCoords[11] = v_TexCoordinate + vec2(0.0,  delta*5.0);" +
                    "   v_blurTexCoords[12] = v_TexCoordinate + vec2(0.0,  delta*6.0);" +
                    "   v_blurTexCoords[13] = v_TexCoordinate + vec2(0.0,  delta*7.0);" +
                    "}";

    private static final String blurFragmentShaderCodeOES =
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "varying vec2 v_blurTexCoords[14];" +
                    "void main () {" +
                    "    vec4 color = texture2D(texture, v_blurTexCoords[ 0])*0.0044299121055113265;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 1])*0.00895781211794;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 2])*0.0215963866053;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 3])*0.0443683338718;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 4])*0.0776744219933;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 5])*0.115876621105;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 6])*0.147308056121;" +
                    "    color += texture2D(texture, v_TexCoordinate)    *0.159576912161;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 7])*0.147308056121;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 8])*0.115876621105;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 9])*0.0776744219933;" +
                    "    color += texture2D(texture, v_blurTexCoords[10])*0.0443683338718;" +
                    "    color += texture2D(texture, v_blurTexCoords[11])*0.0215963866053;" +
                    "    color += texture2D(texture, v_blurTexCoords[12])*0.00895781211794;" +
                    "    color += texture2D(texture, v_blurTexCoords[13])*0.0044299121055113265;" +
                    "    gl_FragColor = color;" +
                    "}";

    private static final String blurFragmentShaderCode =
                    "precision mediump float;" +
                    "uniform sampler2D texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "varying vec2 v_blurTexCoords[14];" +
                    "void main () {" +
                    "    vec4 color = texture2D(texture, v_blurTexCoords[ 0])*0.0044299121055113265;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 1])*0.00895781211794;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 2])*0.0215963866053;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 3])*0.0443683338718;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 4])*0.0776744219933;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 5])*0.115876621105;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 6])*0.147308056121;" +
                    "    color += texture2D(texture, v_TexCoordinate)    *0.159576912161;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 7])*0.147308056121;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 8])*0.115876621105;" +
                    "    color += texture2D(texture, v_blurTexCoords[ 9])*0.0776744219933;" +
                    "    color += texture2D(texture, v_blurTexCoords[10])*0.0443683338718;" +
                    "    color += texture2D(texture, v_blurTexCoords[11])*0.0215963866053;" +
                    "    color += texture2D(texture, v_blurTexCoords[12])*0.00895781211794;" +
                    "    color += texture2D(texture, v_blurTexCoords[13])*0.0044299121055113265;" +
                    "    gl_FragColor = color;" +
                    "}";


    private static final String vertexShaderCode =
                    "attribute vec4 vPosition;" +
                    "attribute vec4 vTexCoordinate;" +
                    "uniform mat4 textureTransform;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "   v_TexCoordinate = (textureTransform * vTexCoordinate).xy;" +
                    "   gl_Position = vPosition;" +
                    "}";

    private static final String fragmentShaderCode =
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main () {" +
                    "    vec4 color = texture2D(texture, v_TexCoordinate);" +
                    "    gl_FragColor = color;" +
                    "}";

    private static final String fragmentShaderCode2 =
                    "precision mediump float;" +
                    "uniform sampler2D texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main () {" +
                    "    vec4 color = texture2D(texture, v_TexCoordinate);" +
                    "    gl_FragColor = color;" +
                    "}";


    private static float squareSize = 1.0f;
    private static float squareCoords[] = { -squareSize,  squareSize, 0.0f,   // top left
                                            -squareSize, -squareSize, 0.0f,   // bottom left
                                             squareSize, -squareSize, 0.0f,   // bottom right
                                             squareSize,  squareSize, 0.0f }; // top right
    private static float squareCoords2[] = { -squareSize,  squareSize, 0.0f,   // top left
            -squareSize, -squareSize, 0.0f,   // bottom left
            squareSize, -squareSize, 0.0f,   // bottom right
            squareSize,  squareSize, 0.0f }; // top right

    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};
    private static short BLUR_TEX_SIZE = 128;
    private static short BLUR_TIMES = 20;

    private Context ctx;

    // Texture to be shown in backgrund
    private FloatBuffer textureBuffer;
    private FloatBuffer textureBuffer2;
    private float textureCoords[] = { 0.0f, 1.0f, 0.0f, 1.0f,
                                      0.0f, 0.0f, 0.0f, 1.0f,
                                      1.0f, 0.0f, 0.0f, 1.0f,
                                      1.0f, 1.0f, 0.0f, 1.0f };
    private float textureCoords2[] = { 0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.0f, 0.0f, 1.0f,
            0.5f, 1.0f, 0.0f, 1.0f };
    private int[] textures = new int[3];
    private int[] fbo = new int[1];
    private int[] fboTexture = new int[1];

    private int hBlurVertexShaderHandle;
    private int vBlurVertexShaderHandle;
    private int blurFragmentShaderHandle;
    private int blurFragmentShaderHandleOES;
    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    private int fragmentShaderHandle2;
    private int hBlurShaderProgram;
    private int hBlurShaderProgramOES;
    private int vBlurShaderProgram;
    private int shaderProgram;
    private int shaderProgram2;
    private FloatBuffer vertexBuffer;
    private FloatBuffer vertexBuffer2;
    private ShortBuffer drawListBuffer;

    private SurfaceTexture videoTexture;
    private float[] videoTextureTransform;
    private boolean frameAvailable = false;

    private int videoWidth;
    private int videoHeight;
    private boolean adjustViewport = false;
    private boolean adjustViewport2 = false;

    public VideoTextureRenderer(Context context, SurfaceTexture texture, int width, int height)
    {
        super(texture, width, height);
        this.ctx = context;
        videoTextureTransform = new float[16];
    }

    private void loadShaders()
    {
        hBlurVertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(hBlurVertexShaderHandle, hBlurVertexShaderCode);
        GLES20.glCompileShader(hBlurVertexShaderHandle);
        checkGlError("hBlur vertex shader compile");

        vBlurVertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vBlurVertexShaderHandle, vBlurVertexShaderCode);
        GLES20.glCompileShader(vBlurVertexShaderHandle);
        checkGlError("vBlur vertex shader compile");

        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);
        checkGlError("Vertex shader compile");

        blurFragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(blurFragmentShaderHandle, blurFragmentShaderCode);
        GLES20.glCompileShader(blurFragmentShaderHandle);
        checkGlError("Blur pixel shader compile");

        blurFragmentShaderHandleOES = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(blurFragmentShaderHandleOES, blurFragmentShaderCodeOES);
        GLES20.glCompileShader(blurFragmentShaderHandleOES);
        checkGlError("Blur pixel shader compile OES");

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderHandle);
        checkGlError("Pixel shader compile");

        fragmentShaderHandle2 = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle2, fragmentShaderCode2);
        GLES20.glCompileShader(fragmentShaderHandle2);
        checkGlError("Pixel shader compile");

        hBlurShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(hBlurShaderProgram, hBlurVertexShaderHandle);
        GLES20.glAttachShader(hBlurShaderProgram, blurFragmentShaderHandle);
        GLES20.glLinkProgram(hBlurShaderProgram);
        checkGlError("hBlur shader program compile");

        hBlurShaderProgramOES = GLES20.glCreateProgram();
        GLES20.glAttachShader(hBlurShaderProgramOES, hBlurVertexShaderHandle);
        GLES20.glAttachShader(hBlurShaderProgramOES, blurFragmentShaderHandleOES);
        GLES20.glLinkProgram(hBlurShaderProgramOES);
        checkGlError("hBlur shader program compile");

        vBlurShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(vBlurShaderProgram, vBlurVertexShaderHandle);
        GLES20.glAttachShader(vBlurShaderProgram, blurFragmentShaderHandle);
        GLES20.glLinkProgram(vBlurShaderProgram);
        checkGlError("vBlur shader program compile");

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
        GLES20.glLinkProgram(shaderProgram);
        checkGlError("Shader program compile");

        shaderProgram2 = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram2, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgram2, fragmentShaderHandle2);
        GLES20.glLinkProgram(shaderProgram2);
        checkGlError("Shader program compile");


        int[] status = new int[1];
        GLES20.glGetProgramiv(hBlurShaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(hBlurShaderProgram);
            Log.e("SurfaceTest", "Error while linking program:\n" + error);
        }
        GLES20.glGetProgramiv(hBlurShaderProgramOES, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(hBlurShaderProgramOES);
            Log.e("SurfaceTest", "Error while linking program:\n" + error);
        }
        GLES20.glGetProgramiv(vBlurShaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(vBlurShaderProgram);
            Log.e("SurfaceTest", "Error while linking program:\n" + error);
        }
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(shaderProgram);
            Log.e("SurfaceTest", "Error while linking program:\n" + error);
        }
        GLES20.glGetProgramiv(shaderProgram2, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(shaderProgram2);
            Log.e("SurfaceTest", "Error while linking program 2:\n" + error);
        }

    }


    private void setupVertexBuffer()
    {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder. length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        ByteBuffer bb2 = ByteBuffer.allocateDirect(squareCoords2.length * 4);
        bb2.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        vertexBuffer2 = bb2.asFloatBuffer();
        vertexBuffer2.put(squareCoords2);
        vertexBuffer2.position(0);
    }


    private void setupTexture(Context context)
    {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        ByteBuffer texturebb2 = ByteBuffer.allocateDirect(textureCoords2.length * 4);
        texturebb2.order(ByteOrder.nativeOrder());

        textureBuffer2 = texturebb2.asFloatBuffer();
        textureBuffer2.put(textureCoords2);
        textureBuffer2.position(0);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(3, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        // Generate RTT objects
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, BLUR_TEX_SIZE, BLUR_TEX_SIZE, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, BLUR_TEX_SIZE, BLUR_TEX_SIZE, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);



        GLES20.glGenFramebuffers(1, fbo, 0);

        videoTexture = new SurfaceTexture(textures[0]);
        videoTexture.setOnFrameAvailableListener(this);
    }

    private boolean blurFrame(boolean useOESTexture)
    {
        // ------ H BLUR -------
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[1], 0);

        adjustViewport2();

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }

        if (useOESTexture) {
            GLES20.glUseProgram(hBlurShaderProgramOES);
        }
        else {
            GLES20.glUseProgram(hBlurShaderProgram);
        }
        int textureParamHandle = GLES20.glGetUniformLocation(hBlurShaderProgram, "texture");
        int textureCoordinateHandle = GLES20.glGetAttribLocation(hBlurShaderProgram, "vTexCoordinate");
        int positionHandle = GLES20.glGetAttribLocation(hBlurShaderProgram, "vPosition");
        int textureTranformHandle = GLES20.glGetUniformLocation(hBlurShaderProgram, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer);

        if (!useOESTexture) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        }
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // ------ V BLUR -------

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[2], 0);

        status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }

        GLES20.glUseProgram(vBlurShaderProgram);
        textureParamHandle = GLES20.glGetUniformLocation(vBlurShaderProgram, "texture");
        textureCoordinateHandle = GLES20.glGetAttribLocation(vBlurShaderProgram, "vTexCoordinate");
        positionHandle = GLES20.glGetAttribLocation(vBlurShaderProgram, "vPosition");
        textureTranformHandle = GLES20.glGetUniformLocation(vBlurShaderProgram, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        return true;
    }

    @Override
    protected boolean draw()
    {
        synchronized (this)
        {
            if (frameAvailable)
            {
                videoTexture.updateTexImage();
                videoTexture.getTransformMatrix(videoTextureTransform);
                frameAvailable = false;
            }
            else
            {
                return false;
            }

        }


        blurFrame(true);
        for (int i=0; i < BLUR_TIMES; i++) {
            blurFrame(false);
        }

        // Bind the default framebuffer (to render to the screen) - indicated by '0'
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        adjustViewport();

        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(shaderProgram);
        int textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture");
        int textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate");
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        int textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer);
        GLES20.glUniform1i(textureParamHandle, 0);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);


        adjustViewportBlur(-400);

        GLES20.glUseProgram(shaderProgram2);
        textureParamHandle = GLES20.glGetUniformLocation(shaderProgram2, "texture");
        textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram2, "vTexCoordinate");
        positionHandle = GLES20.glGetAttribLocation(shaderProgram2, "vPosition");
        textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram2, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glUniform1i(textureParamHandle, 0);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer2);
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);


        return true;
    }

    private void adjustViewport()
    {
        float surfaceAspect = height / (float)width;
        float videoAspect = videoHeight / (float)videoWidth;

        if (surfaceAspect > videoAspect)
        {
            float heightRatio = height / (float)videoHeight;
            int newWidth = (int)(videoWidth * heightRatio);
            int xOffset = (newWidth - width) / 2;
            GLES20.glViewport(-xOffset, 0, newWidth, height);
        }
        else
        {
            float widthRatio = width / (float)videoWidth;
            int newHeight = (int)(videoHeight * widthRatio);
            int yOffset = (newHeight - height) / 2;
            GLES20.glViewport(0, -yOffset, width, newHeight);
        }

        adjustViewport = false;
    }

    private void adjustViewportBlur(int left)
    {
        float surfaceAspect = height / (float)width;
        float videoAspect = videoHeight / (float)videoWidth;

        if (surfaceAspect > videoAspect)
        {
            float heightRatio = height / (float)videoHeight;
            int newWidth = (int)(videoWidth * heightRatio);
            int xOffset = (newWidth - width) / 2;
            GLES20.glViewport(-xOffset, 0, newWidth/2, height);
        }
        else
        {
            float widthRatio = width / (float)videoWidth;
            int newHeight = (int)(videoHeight * widthRatio);
            int yOffset = (newHeight - height) / 2;
            GLES20.glViewport(0, -yOffset, width/2, newHeight);
        }
    }

    private void adjustViewport2()
    {
        GLES20.glViewport(0, 0, BLUR_TEX_SIZE, BLUR_TEX_SIZE);

        adjustViewport2 = false;
    }

    @Override
    protected void initGLComponents()
    {
        setupVertexBuffer();
        setupTexture(ctx);
        loadShaders();
    }

    @Override
    protected void deinitGLComponents()
    {
        GLES20.glDeleteTextures(1, textures, 0);
        GLES20.glDeleteProgram(shaderProgram);
        videoTexture.release();
        videoTexture.setOnFrameAvailableListener(null);
    }

    public void setVideoSize(int width, int height)
    {
        this.videoWidth = width;
        this.videoHeight = height;
        adjustViewport = true;
        adjustViewport2 = true;
    }

    public void checkGlError(String op)
    {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    public SurfaceTexture getVideoTexture()
    {
        return videoTexture;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        synchronized (this)
        {
            frameAvailable = true;
        }
    }
}
