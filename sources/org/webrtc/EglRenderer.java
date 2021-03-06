package org.webrtc;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.telegram.messenger.FileLog;
import org.webrtc.EglBase;
import org.webrtc.GlGenericDrawer;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;
import org.webrtc.VideoSink;

public class EglRenderer implements VideoSink {
    private static final long LOG_INTERVAL_SEC = 4;
    private static final String TAG = "EglRenderer";
    private final GlTextureFrameBuffer bitmapTextureFramebuffer;
    private final Matrix drawMatrix;
    private RendererCommon.GlDrawer drawer;
    private EglBase eglBase;
    private final EglSurfaceCreation eglSurfaceBackgroundCreationRunnable;
    private final EglSurfaceCreation eglSurfaceCreationRunnable;
    private volatile ErrorCallback errorCallback;
    public boolean firstFrameRendered;
    private final Object fpsReductionLock;
    private final VideoFrameDrawer frameDrawer;
    private final ArrayList<FrameListenerAndParams> frameListeners;
    private final Object frameLock;
    private int framesDropped;
    private int framesReceived;
    private int framesRendered;
    private final Object handlerLock;
    private float layoutAspectRatio;
    private final Object layoutLock;
    private long minRenderPeriodNs;
    private boolean mirrorHorizontally;
    private boolean mirrorVertically;
    protected final String name;
    private long nextFrameTimeNs;
    private VideoFrame pendingFrame;
    private long renderSwapBufferTimeNs;
    private Handler renderThreadHandler;
    private long renderTimeNs;
    private int rotation;
    private boolean usePresentationTimeStamp;

    public interface ErrorCallback {
        void onGlOutOfMemory();
    }

    public interface FrameListener {
        void onFrame(Bitmap bitmap);
    }

    protected void onFirstFrameRendered() {
    }

    @Override
    public void setParentSink(VideoSink videoSink) {
        VideoSink.CC.$default$setParentSink(this, videoSink);
    }

    public static class FrameListenerAndParams {
        public final boolean applyFpsReduction;
        public final RendererCommon.GlDrawer drawer;
        public final FrameListener listener;
        public final float scale;

        public FrameListenerAndParams(FrameListener frameListener, float f, RendererCommon.GlDrawer glDrawer, boolean z) {
            this.listener = frameListener;
            this.scale = f;
            this.drawer = glDrawer;
            this.applyFpsReduction = z;
        }
    }

    public class EglSurfaceCreation implements Runnable {
        private final boolean background;
        private Object surface;

        public EglSurfaceCreation(boolean z) {
            this.background = z;
        }

        public synchronized void setSurface(Object obj) {
            this.surface = obj;
        }

        @Override
        public synchronized void run() {
            throw new UnsupportedOperationException("Method not decompiled: org.webrtc.EglRenderer.EglSurfaceCreation.run():void");
        }
    }

    public static class HandlerWithExceptionCallback extends Handler {
        private final Runnable exceptionCallback;

        public HandlerWithExceptionCallback(Looper looper, Runnable runnable) {
            super(looper);
            this.exceptionCallback = runnable;
        }

        @Override
        public void dispatchMessage(Message message) {
            try {
                super.dispatchMessage(message);
            } catch (Exception e) {
                Logging.e(EglRenderer.TAG, "Exception on EglRenderer thread", e);
                this.exceptionCallback.run();
                throw e;
            }
        }
    }

    public EglRenderer(String str) {
        this(str, new VideoFrameDrawer());
    }

    public EglRenderer(String str, VideoFrameDrawer videoFrameDrawer) {
        this.handlerLock = new Object();
        this.frameListeners = new ArrayList<>();
        this.fpsReductionLock = new Object();
        this.drawMatrix = new Matrix();
        this.frameLock = new Object();
        this.layoutLock = new Object();
        this.bitmapTextureFramebuffer = new GlTextureFrameBuffer(6408);
        this.eglSurfaceCreationRunnable = new EglSurfaceCreation(false);
        this.eglSurfaceBackgroundCreationRunnable = new EglSurfaceCreation(true);
        this.name = str;
        this.frameDrawer = videoFrameDrawer;
    }

    public void init(final EglBase.Context context, final int[] iArr, RendererCommon.GlDrawer glDrawer, boolean z) {
        synchronized (this.handlerLock) {
            if (this.renderThreadHandler == null) {
                logD("Initializing EglRenderer");
                this.drawer = glDrawer;
                this.usePresentationTimeStamp = z;
                this.firstFrameRendered = false;
                HandlerThread handlerThread = new HandlerThread(this.name + TAG);
                handlerThread.start();
                HandlerWithExceptionCallback handlerWithExceptionCallback = new HandlerWithExceptionCallback(handlerThread.getLooper(), new Runnable() {
                    @Override
                    public void run() {
                        synchronized (EglRenderer.this.handlerLock) {
                            EglRenderer.this.renderThreadHandler = null;
                        }
                    }
                });
                this.renderThreadHandler = handlerWithExceptionCallback;
                handlerWithExceptionCallback.post(new Runnable() {
                    @Override
                    public final void run() {
                        EglRenderer.this.lambda$init$0(context, iArr);
                    }
                });
                this.renderThreadHandler.post(this.eglSurfaceCreationRunnable);
            } else {
                throw new IllegalStateException(this.name + "Already initialized");
            }
        }
    }

    public void lambda$init$0(EglBase.Context context, int[] iArr) {
        if (context == null) {
            logD("EglBase10.create context");
            this.eglBase = EglBase.CC.createEgl10(iArr);
            return;
        }
        logD("EglBase.create shared context");
        this.eglBase = EglBase.CC.create(context, iArr);
    }

    public void init(EglBase.Context context, int[] iArr, RendererCommon.GlDrawer glDrawer) {
        init(context, iArr, glDrawer, false);
    }

    public void createEglSurface(Surface surface) {
        createEglSurfaceInternal(surface, false);
    }

    public void createEglSurface(SurfaceTexture surfaceTexture) {
        createEglSurfaceInternal(surfaceTexture, false);
    }

    public void createBackgroundSurface(SurfaceTexture surfaceTexture) {
        createEglSurfaceInternal(surfaceTexture, true);
    }

    private void createEglSurfaceInternal(Object obj, boolean z) {
        if (z) {
            this.eglSurfaceBackgroundCreationRunnable.setSurface(obj);
            synchronized (this.handlerLock) {
                Handler handler = this.renderThreadHandler;
                if (handler != null) {
                    handler.post(this.eglSurfaceBackgroundCreationRunnable);
                } else {
                    FileLog.d("can't create background surface. render thread is null");
                }
            }
            return;
        }
        this.eglSurfaceCreationRunnable.setSurface(obj);
        postToRenderThread(this.eglSurfaceCreationRunnable);
    }

    public void release() {
        logD("Releasing.");
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        synchronized (this.handlerLock) {
            Handler handler = this.renderThreadHandler;
            if (handler == null) {
                logD("Already released");
                return;
            }
            handler.postAtFrontOfQueue(new Runnable() {
                @Override
                public final void run() {
                    EglRenderer.this.lambda$release$1(countDownLatch);
                }
            });
            final Looper looper = this.renderThreadHandler.getLooper();
            this.renderThreadHandler.post(new Runnable() {
                @Override
                public final void run() {
                    EglRenderer.this.lambda$release$2(looper);
                }
            });
            this.renderThreadHandler = null;
            ThreadUtils.awaitUninterruptibly(countDownLatch);
            synchronized (this.frameLock) {
                VideoFrame videoFrame = this.pendingFrame;
                if (videoFrame != null) {
                    videoFrame.release();
                    this.pendingFrame = null;
                }
            }
            logD("Releasing done.");
        }
    }

    public void lambda$release$1(CountDownLatch countDownLatch) {
        synchronized (EglBase.lock) {
            GLES20.glUseProgram(0);
        }
        RendererCommon.GlDrawer glDrawer = this.drawer;
        if (glDrawer != null) {
            glDrawer.release();
            this.drawer = null;
        }
        this.frameDrawer.release();
        this.bitmapTextureFramebuffer.release();
        if (this.eglBase != null) {
            logD("eglBase detach and release.");
            this.eglBase.detachCurrent();
            this.eglBase.release();
            this.eglBase = null;
        }
        this.frameListeners.clear();
        countDownLatch.countDown();
    }

    public void lambda$release$2(Looper looper) {
        logD("Quitting render thread.");
        looper.quit();
    }

    public void printStackTrace() {
        synchronized (this.handlerLock) {
            Handler handler = this.renderThreadHandler;
            Thread thread = handler == null ? null : handler.getLooper().getThread();
            if (thread != null) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length > 0) {
                    logW("EglRenderer stack trace:");
                    for (StackTraceElement stackTraceElement : stackTrace) {
                        logW(stackTraceElement.toString());
                    }
                }
            }
        }
    }

    public void setMirror(boolean z) {
        logD("setMirrorHorizontally: " + z);
        synchronized (this.layoutLock) {
            this.mirrorHorizontally = z;
        }
    }

    public void setMirrorVertically(boolean z) {
        logD("setMirrorVertically: " + z);
        synchronized (this.layoutLock) {
            this.mirrorVertically = z;
        }
    }

    public void setLayoutAspectRatio(float f) {
        if (this.layoutAspectRatio != f) {
            synchronized (this.layoutLock) {
                this.layoutAspectRatio = f;
            }
        }
    }

    public void setFpsReduction(float f) {
        logD("setFpsReduction: " + f);
        synchronized (this.fpsReductionLock) {
            long j = this.minRenderPeriodNs;
            if (f <= 0.0f) {
                this.minRenderPeriodNs = Long.MAX_VALUE;
            } else {
                this.minRenderPeriodNs = ((float) TimeUnit.SECONDS.toNanos(1L)) / f;
            }
            if (this.minRenderPeriodNs != j) {
                this.nextFrameTimeNs = System.nanoTime();
            }
        }
    }

    public void disableFpsReduction() {
        setFpsReduction(Float.POSITIVE_INFINITY);
    }

    public void pauseVideo() {
        setFpsReduction(0.0f);
    }

    public void addFrameListener(FrameListener frameListener, float f) {
        addFrameListener(frameListener, f, null, false);
    }

    public void addFrameListener(FrameListener frameListener, float f, RendererCommon.GlDrawer glDrawer) {
        addFrameListener(frameListener, f, glDrawer, false);
    }

    public void addFrameListener(final FrameListener frameListener, final float f, final RendererCommon.GlDrawer glDrawer, final boolean z) {
        postToRenderThread(new Runnable() {
            @Override
            public final void run() {
                EglRenderer.this.lambda$addFrameListener$3(glDrawer, frameListener, f, z);
            }
        });
    }

    public void lambda$addFrameListener$3(RendererCommon.GlDrawer glDrawer, FrameListener frameListener, float f, boolean z) {
        if (glDrawer == null) {
            glDrawer = this.drawer;
        }
        this.frameListeners.add(new FrameListenerAndParams(frameListener, f, glDrawer, z));
    }

    public void removeFrameListener(final FrameListener frameListener) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        synchronized (this.handlerLock) {
            if (this.renderThreadHandler != null) {
                if (Thread.currentThread() != this.renderThreadHandler.getLooper().getThread()) {
                    postToRenderThread(new Runnable() {
                        @Override
                        public final void run() {
                            EglRenderer.this.lambda$removeFrameListener$4(countDownLatch, frameListener);
                        }
                    });
                    ThreadUtils.awaitUninterruptibly(countDownLatch);
                    return;
                }
                throw new RuntimeException("removeFrameListener must not be called on the render thread.");
            }
        }
    }

    public void lambda$removeFrameListener$4(CountDownLatch countDownLatch, FrameListener frameListener) {
        countDownLatch.countDown();
        Iterator<FrameListenerAndParams> it = this.frameListeners.iterator();
        while (it.hasNext()) {
            if (it.next().listener == frameListener) {
                it.remove();
            }
        }
    }

    public void setErrorCallback(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        synchronized (this.handlerLock) {
            if (this.renderThreadHandler == null) {
                logD("Dropping frame - Not initialized or already released.");
                return;
            }
            synchronized (this.frameLock) {
                VideoFrame videoFrame2 = this.pendingFrame;
                if (videoFrame2 != null) {
                    videoFrame2.release();
                }
                this.pendingFrame = videoFrame;
                videoFrame.retain();
                this.renderThreadHandler.post(new Runnable() {
                    @Override
                    public final void run() {
                        EglRenderer.this.renderFrameOnRenderThread();
                    }
                });
            }
        }
    }

    public void setRotation(int i) {
        synchronized (this.layoutLock) {
            this.rotation = i;
        }
    }

    public void releaseEglSurface(final Runnable runnable, final boolean z) {
        this.eglSurfaceCreationRunnable.setSurface(null);
        synchronized (this.handlerLock) {
            Handler handler = this.renderThreadHandler;
            if (handler != null) {
                handler.removeCallbacks(this.eglSurfaceCreationRunnable);
                this.renderThreadHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public final void run() {
                        EglRenderer.this.lambda$releaseEglSurface$5(z, runnable);
                    }
                });
            } else if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void lambda$releaseEglSurface$5(boolean z, Runnable runnable) {
        EglBase eglBase = this.eglBase;
        if (eglBase != null) {
            eglBase.detachCurrent();
            this.eglBase.releaseSurface(z);
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    private void postToRenderThread(Runnable runnable) {
        synchronized (this.handlerLock) {
            Handler handler = this.renderThreadHandler;
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }

    public void lambda$clearImage$6(float f, float f2, float f3, float f4) {
        EglBase eglBase = this.eglBase;
        if (eglBase != null && eglBase.hasSurface()) {
            logD("clearSurface");
            GLES20.glClearColor(f, f2, f3, f4);
            GLES20.glClear(16384);
            this.eglBase.swapBuffers(false);
        }
    }

    public void clearImage() {
        clearImage(0.0f, 0.0f, 0.0f, 0.0f);
        this.firstFrameRendered = false;
    }

    public void clearImage(final float f, final float f2, final float f3, final float f4) {
        synchronized (this.handlerLock) {
            Handler handler = this.renderThreadHandler;
            if (handler != null) {
                handler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public final void run() {
                        EglRenderer.this.lambda$clearImage$6(f, f2, f3, f4);
                    }
                });
            }
        }
    }

    public void getTexture(final GlGenericDrawer.TextureCallback textureCallback) {
        synchronized (this.handlerLock) {
            try {
                Handler handler = this.renderThreadHandler;
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public final void run() {
                            EglRenderer.this.lambda$getTexture$7(textureCallback);
                        }
                    });
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public void lambda$getTexture$7(GlGenericDrawer.TextureCallback textureCallback) {
        this.frameDrawer.getRenderBufferBitmap(this.drawer, this.rotation, textureCallback);
    }

    public void renderFrameOnRenderThread() {
        boolean z;
        float f;
        float f2;
        float f3;
        synchronized (this.frameLock) {
            VideoFrame videoFrame = this.pendingFrame;
            if (videoFrame != null) {
                this.pendingFrame = null;
                EglBase eglBase = this.eglBase;
                if (eglBase == null || !eglBase.hasSurface()) {
                    logD("Dropping frame - No surface");
                    return;
                }
                synchronized (this.fpsReductionLock) {
                    long j = this.minRenderPeriodNs;
                    if (j != Long.MAX_VALUE) {
                        if (j > 0) {
                            long nanoTime = System.nanoTime();
                            long j2 = this.nextFrameTimeNs;
                            if (nanoTime >= j2) {
                                long j3 = j2 + this.minRenderPeriodNs;
                                this.nextFrameTimeNs = j3;
                                this.nextFrameTimeNs = Math.max(j3, nanoTime);
                            }
                        }
                        z = true;
                    }
                    z = false;
                }
                System.nanoTime();
                boolean z2 = Math.abs(this.rotation) == 90 || Math.abs(this.rotation) == 270;
                float rotatedHeight = (z2 ? videoFrame.getRotatedHeight() : videoFrame.getRotatedWidth()) / (z2 ? videoFrame.getRotatedWidth() : videoFrame.getRotatedHeight());
                synchronized (this.layoutLock) {
                    f = this.layoutAspectRatio;
                    if (f == 0.0f) {
                        f = rotatedHeight;
                    }
                }
                float f4 = 1.0f;
                if (rotatedHeight > f) {
                    f2 = f / rotatedHeight;
                    f3 = 1.0f;
                } else {
                    f3 = rotatedHeight / f;
                    f2 = 1.0f;
                }
                this.drawMatrix.reset();
                this.drawMatrix.preTranslate(0.5f, 0.5f);
                this.drawMatrix.preRotate(this.rotation);
                Matrix matrix = this.drawMatrix;
                float f5 = this.mirrorHorizontally ? -1.0f : 1.0f;
                if (this.mirrorVertically) {
                    f4 = -1.0f;
                }
                matrix.preScale(f5, f4);
                this.drawMatrix.preScale(f2, f3);
                this.drawMatrix.preTranslate(-0.5f, -0.5f);
                try {
                    if (z) {
                        try {
                            this.frameDrawer.drawFrame(videoFrame, this.drawer, this.drawMatrix, 0, 0, this.eglBase.surfaceWidth(), this.eglBase.surfaceHeight(), z2, false);
                            if (this.eglBase.hasBackgroundSurface()) {
                                this.eglBase.makeBackgroundCurrent();
                                this.frameDrawer.drawFrame(videoFrame, this.drawer, this.drawMatrix, 0, 0, this.eglBase.surfaceWidth(), this.eglBase.surfaceHeight(), z2, true);
                                if (this.usePresentationTimeStamp) {
                                    this.eglBase.swapBuffers(videoFrame.getTimestampNs(), true);
                                } else {
                                    this.eglBase.swapBuffers(true);
                                }
                                this.eglBase.makeCurrent();
                            }
                            System.nanoTime();
                            if (this.usePresentationTimeStamp) {
                                this.eglBase.swapBuffers(videoFrame.getTimestampNs(), false);
                            } else {
                                this.eglBase.swapBuffers(false);
                            }
                            if (!this.firstFrameRendered) {
                                this.firstFrameRendered = true;
                                onFirstFrameRendered();
                            }
                        } catch (GlUtil.GlOutOfMemoryException e) {
                            logE("Error while drawing frame", e);
                            ErrorCallback errorCallback = this.errorCallback;
                            if (errorCallback != null) {
                                errorCallback.onGlOutOfMemory();
                            }
                            this.drawer.release();
                            this.frameDrawer.release();
                            this.bitmapTextureFramebuffer.release();
                        }
                    }
                    notifyCallbacks(videoFrame, z);
                } finally {
                    videoFrame.release();
                }
            }
        }
    }

    private void notifyCallbacks(VideoFrame videoFrame, boolean z) {
        if (!this.frameListeners.isEmpty()) {
            this.drawMatrix.reset();
            this.drawMatrix.preTranslate(0.5f, 0.5f);
            this.drawMatrix.preRotate(this.rotation);
            this.drawMatrix.preScale(this.mirrorHorizontally ? -1.0f : 1.0f, this.mirrorVertically ? -1.0f : 1.0f);
            this.drawMatrix.preScale(1.0f, -1.0f);
            this.drawMatrix.preTranslate(-0.5f, -0.5f);
            Iterator<FrameListenerAndParams> it = this.frameListeners.iterator();
            while (it.hasNext()) {
                FrameListenerAndParams next = it.next();
                if (z || !next.applyFpsReduction) {
                    it.remove();
                    int rotatedWidth = (int) (next.scale * videoFrame.getRotatedWidth());
                    int rotatedHeight = (int) (next.scale * videoFrame.getRotatedHeight());
                    if (rotatedWidth == 0 || rotatedHeight == 0) {
                        next.listener.onFrame(null);
                    } else {
                        this.bitmapTextureFramebuffer.setSize(rotatedWidth, rotatedHeight);
                        GLES20.glBindFramebuffer(36160, this.bitmapTextureFramebuffer.getFrameBufferId());
                        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.bitmapTextureFramebuffer.getTextureId(), 0);
                        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                        GLES20.glClear(16384);
                        this.frameDrawer.drawFrame(videoFrame, next.drawer, this.drawMatrix, 0, 0, rotatedWidth, rotatedHeight, false, false);
                        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(rotatedWidth * rotatedHeight * 4);
                        GLES20.glViewport(0, 0, rotatedWidth, rotatedHeight);
                        GLES20.glReadPixels(0, 0, rotatedWidth, rotatedHeight, 6408, 5121, allocateDirect);
                        GLES20.glBindFramebuffer(36160, 0);
                        GlUtil.checkNoGLES2Error("EglRenderer.notifyCallbacks");
                        Bitmap createBitmap = Bitmap.createBitmap(rotatedWidth, rotatedHeight, Bitmap.Config.ARGB_8888);
                        createBitmap.copyPixelsFromBuffer(allocateDirect);
                        next.listener.onFrame(createBitmap);
                    }
                }
            }
        }
    }

    private void logE(String str, Throwable th) {
        Logging.e(TAG, this.name + str, th);
    }

    private void logD(String str) {
        Logging.d(TAG, this.name + str);
    }

    private void logW(String str) {
        Logging.w(TAG, this.name + str);
    }
}
