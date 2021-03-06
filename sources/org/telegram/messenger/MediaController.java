package org.telegram.messenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.TextureView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.gms.internal.mlkit_language_id.zzdp$$ExternalSyntheticBackport0;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$BotInlineResult;
import org.telegram.tgnet.TLRPC$Document;
import org.telegram.tgnet.TLRPC$DocumentAttribute;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$InputDocument;
import org.telegram.tgnet.TLRPC$MessageEntity;
import org.telegram.tgnet.TLRPC$Photo;
import org.telegram.tgnet.TLRPC$PhotoSize;
import org.telegram.tgnet.TLRPC$TL_document;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC$TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC$TL_encryptedChat;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.tgnet.TLRPC$TL_messages_messages;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.tgnet.TLRPC$messages_Messages;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.Point;
import org.telegram.ui.Components.VideoPlayer;
import org.telegram.ui.PhotoViewer;
import org.webrtc.MediaStreamTrack;

public class MediaController implements AudioManager.OnAudioFocusChangeListener, NotificationCenter.NotificationCenterDelegate, SensorEventListener {
    private static final int AUDIO_FOCUSED = 2;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    public static final String AUIDO_MIME_TYPE = "audio/mp4a-latm";
    private static volatile MediaController Instance = null;
    public static final int VIDEO_BITRATE_1080 = 6800000;
    public static final int VIDEO_BITRATE_360 = 750000;
    public static final int VIDEO_BITRATE_480 = 1000000;
    public static final int VIDEO_BITRATE_720 = 2621440;
    public static final String VIDEO_MIME_TYPE = "video/avc";
    private static final float VOLUME_DUCK = 0.2f;
    private static final float VOLUME_NORMAL = 1.0f;
    public static AlbumEntry allMediaAlbumEntry;
    public static ArrayList<AlbumEntry> allMediaAlbums;
    public static ArrayList<AlbumEntry> allPhotoAlbums;
    public static AlbumEntry allPhotosAlbumEntry;
    public static AlbumEntry allVideosAlbumEntry;
    private static Runnable broadcastPhotosRunnable;
    private static final String[] projectionPhotos;
    private static final String[] projectionVideo;
    private static Runnable refreshGalleryRunnable;
    private static long volumeBarLastTimeShown;
    private Sensor accelerometerSensor;
    private boolean accelerometerVertical;
    private boolean allowStartRecord;
    private AudioInfo audioInfo;
    private AudioRecord audioRecorder;
    private float audioVolume;
    private ValueAnimator audioVolumeAnimator;
    private Activity baseActivity;
    private boolean callInProgress;
    private int countLess;
    private AspectRatioFrameLayout currentAspectRatioFrameLayout;
    private float currentAspectRatioFrameLayoutRatio;
    private boolean currentAspectRatioFrameLayoutReady;
    private int currentAspectRatioFrameLayoutRotation;
    private int currentPlaylistNum;
    private TextureView currentTextureView;
    private FrameLayout currentTextureViewContainer;
    private boolean downloadingCurrentMessage;
    private ExternalObserver externalObserver;
    private View feedbackView;
    private DispatchQueue fileEncodingQueue;
    private BaseFragment flagSecureFragment;
    private boolean forceLoopCurrentPlaylist;
    private MessageObject goingToShowMessageObject;
    private Sensor gravitySensor;
    private int hasAudioFocus;
    private boolean hasRecordAudioFocus;
    private boolean ignoreOnPause;
    private boolean ignoreProximity;
    private boolean inputFieldHasText;
    private InternalObserver internalObserver;
    private boolean isDrawingWasReady;
    private boolean isStreamingCurrentAudio;
    private int lastChatAccount;
    private long lastChatEnterTime;
    private long lastChatLeaveTime;
    private ArrayList<Long> lastChatVisibleMessages;
    private long lastMediaCheckTime;
    private int lastMessageId;
    private long lastSaveTime;
    private TLRPC$EncryptedChat lastSecretChat;
    private TLRPC$User lastUser;
    private Sensor linearSensor;
    private boolean loadingPlaylist;
    private String[] mediaProjections;
    private PipRoundVideoView pipRoundVideoView;
    private int pipSwitchingState;
    private boolean playMusicAgain;
    private int playerNum;
    private boolean playerWasReady;
    private MessageObject playingMessageObject;
    private int playlistClassGuid;
    private PlaylistGlobalSearchParams playlistGlobalSearchParams;
    private long playlistMergeDialogId;
    private float previousAccValue;
    private boolean proximityHasDifferentValues;
    private Sensor proximitySensor;
    private boolean proximityTouched;
    private PowerManager.WakeLock proximityWakeLock;
    private ChatActivity raiseChat;
    private boolean raiseToEarRecord;
    private int raisedToBack;
    private int raisedToTop;
    private int raisedToTopSign;
    private long recordDialogId;
    private DispatchQueue recordQueue;
    private MessageObject recordReplyingMsg;
    private MessageObject recordReplyingTopMsg;
    private Runnable recordStartRunnable;
    private long recordStartTime;
    private long recordTimeCount;
    private TLRPC$TL_document recordingAudio;
    private File recordingAudioFile;
    private int recordingCurrentAccount;
    private boolean resumeAudioOnFocusGain;
    private long samplesCount;
    private float seekToProgressPending;
    private int sendAfterDone;
    private boolean sendAfterDoneNotify;
    private int sendAfterDoneScheduleDate;
    private SensorManager sensorManager;
    private boolean sensorsStarted;
    private String shouldSavePositionForCurrentAudio;
    private int startObserverToken;
    private StopMediaObserverRunnable stopMediaObserverRunnable;
    private long timeSinceRaise;
    private boolean useFrontSpeaker;
    private VideoPlayer videoPlayer;
    private ArrayList<MessageObject> voiceMessagesPlaylist;
    private SparseArray<MessageObject> voiceMessagesPlaylistMap;
    private boolean voiceMessagesPlaylistUnread;
    AudioManager.OnAudioFocusChangeListener audioRecordFocusChangedListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public final void onAudioFocusChange(int i) {
            MediaController.this.lambda$new$0(i);
        }
    };
    private final Object videoConvertSync = new Object();
    private long lastTimestamp = 0;
    private float lastProximityValue = -100.0f;
    private float[] gravity = new float[3];
    private float[] gravityFast = new float[3];
    private float[] linearAcceleration = new float[3];
    private int audioFocus = 0;
    private ArrayList<VideoConvertMessage> videoConvertQueue = new ArrayList<>();
    private final Object videoQueueSync = new Object();
    private HashMap<String, MessageObject> generatingWaveform = new HashMap<>();
    private boolean isPaused = false;
    private VideoPlayer audioPlayer = null;
    private VideoPlayer emojiSoundPlayer = null;
    private int emojiSoundPlayerNum = 0;
    private float currentPlaybackSpeed = VOLUME_NORMAL;
    private float currentMusicPlaybackSpeed = VOLUME_NORMAL;
    private float fastPlaybackSpeed = VOLUME_NORMAL;
    private float fastMusicPlaybackSpeed = VOLUME_NORMAL;
    private long lastProgress = 0;
    private Timer progressTimer = null;
    private final Object progressTimerSync = new Object();
    private ArrayList<MessageObject> playlist = new ArrayList<>();
    private HashMap<Integer, MessageObject> playlistMap = new HashMap<>();
    private ArrayList<MessageObject> shuffledPlaylist = new ArrayList<>();
    private boolean[] playlistEndReached = {false, false};
    private int[] playlistMaxId = {ConnectionsManager.DEFAULT_DATACENTER_ID, ConnectionsManager.DEFAULT_DATACENTER_ID};
    private Runnable setLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (MediaController.this.playingMessageObject != null) {
                FileLoader.getInstance(MediaController.this.playingMessageObject.currentAccount).setLoadingVideo(MediaController.this.playingMessageObject.getDocument(), true, false);
            }
        }
    };
    private int recordingGuid = -1;
    private short[] recordSamples = new short[1024];
    private final Object sync = new Object();
    private ArrayList<ByteBuffer> recordBuffers = new ArrayList<>();
    public int recordBufferSize = 1280;
    public int sampleRate = 48000;
    private Runnable recordRunnable = new AnonymousClass2();
    private final ValueAnimator.AnimatorUpdateListener audioVolumeUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            MediaController.this.audioVolume = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            MediaController.this.setPlayerVolume();
        }
    };
    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);

    public static class AudioEntry {
        public String author;
        public int duration;
        public String genre;
        public long id;
        public MessageObject messageObject;
        public String path;
        public String title;
    }

    public static class SavedFilterState {
        public float blurAngle;
        public float blurExcludeBlurSize;
        public Point blurExcludePoint;
        public float blurExcludeSize;
        public int blurType;
        public float contrastValue;
        public PhotoFilterView.CurvesToolValue curvesToolValue = new PhotoFilterView.CurvesToolValue();
        public float enhanceValue;
        public float exposureValue;
        public float fadeValue;
        public float grainValue;
        public float highlightsValue;
        public float saturationValue;
        public float shadowsValue;
        public float sharpenValue;
        public float softenSkinValue;
        public int tintHighlightsColor;
        public int tintShadowsColor;
        public float vignetteValue;
        public float warmthValue;
    }

    public interface VideoConvertorListener {
        boolean checkConversionCanceled();

        void didWriteData(long j, float f);
    }

    private static int getVideoBitrateWithFactor(float f) {
        return (int) (f * 2000.0f * 1000.0f * 1.13f);
    }

    public static native int isOpusFile(String str);

    private static boolean isRecognizedFormat(int i) {
        if (i == 39 || i == 2130706688) {
            return true;
        }
        switch (i) {
            case R.styleable.MapAttrs_uiTiltGestures:
            case R.styleable.MapAttrs_uiZoomControls:
            case R.styleable.MapAttrs_uiZoomGestures:
                return true;
            default:
                return false;
        }
    }

    private native int startRecord(String str, int i);

    private native void stopRecord();

    public native int writeFrame(ByteBuffer byteBuffer, int i);

    public native byte[] getWaveform(String str);

    public native byte[] getWaveform2(short[] sArr, int i);

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    static long access$1614(MediaController mediaController, long j) {
        long j2 = mediaController.recordTimeCount + j;
        mediaController.recordTimeCount = j2;
        return j2;
    }

    public boolean isBuffering() {
        VideoPlayer videoPlayer = this.audioPlayer;
        if (videoPlayer != null) {
            return videoPlayer.isBuffering();
        }
        return false;
    }

    private static class AudioBuffer {
        ByteBuffer buffer;
        byte[] bufferBytes;
        int finished;
        long pcmOffset;
        int size;

        public AudioBuffer(int i) {
            this.buffer = ByteBuffer.allocateDirect(i);
            this.bufferBytes = new byte[i];
        }
    }

    static {
        String[] strArr = new String[9];
        strArr[0] = "_id";
        strArr[1] = "bucket_id";
        strArr[2] = "bucket_display_name";
        strArr[3] = "_data";
        int i = Build.VERSION.SDK_INT;
        String str = "date_modified";
        strArr[4] = i > 28 ? str : "datetaken";
        strArr[5] = "orientation";
        strArr[6] = "width";
        strArr[7] = "height";
        strArr[8] = "_size";
        projectionPhotos = strArr;
        String[] strArr2 = new String[9];
        strArr2[0] = "_id";
        strArr2[1] = "bucket_id";
        strArr2[2] = "bucket_display_name";
        strArr2[3] = "_data";
        if (i <= 28) {
            str = "datetaken";
        }
        strArr2[4] = str;
        strArr2[5] = "duration";
        strArr2[6] = "width";
        strArr2[7] = "height";
        strArr2[8] = "_size";
        projectionVideo = strArr2;
        allMediaAlbums = new ArrayList<>();
        allPhotoAlbums = new ArrayList<>();
    }

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<>();
        public SparseArray<PhotoEntry> photosByIds = new SparseArray<>();
        public boolean videoOnly;

        public AlbumEntry(int i, String str, PhotoEntry photoEntry) {
            this.bucketId = i;
            this.bucketName = str;
            this.coverPhoto = photoEntry;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            this.photos.add(photoEntry);
            this.photosByIds.put(photoEntry.imageId, photoEntry);
        }
    }

    public static class CropState {
        public float cropPx;
        public float cropPy;
        public float cropRotate;
        public boolean freeform;
        public int height;
        public boolean initied;
        public float lockedAspectRatio;
        public Matrix matrix;
        public boolean mirrored;
        public float scale;
        public float stateScale;
        public int transformHeight;
        public int transformRotation;
        public int transformWidth;
        public int width;
        public float cropScale = MediaController.VOLUME_NORMAL;
        public float cropPw = MediaController.VOLUME_NORMAL;
        public float cropPh = MediaController.VOLUME_NORMAL;

        public CropState clone() {
            CropState cropState = new CropState();
            cropState.cropPx = this.cropPx;
            cropState.cropPy = this.cropPy;
            cropState.cropScale = this.cropScale;
            cropState.cropRotate = this.cropRotate;
            cropState.cropPw = this.cropPw;
            cropState.cropPh = this.cropPh;
            cropState.transformWidth = this.transformWidth;
            cropState.transformHeight = this.transformHeight;
            cropState.transformRotation = this.transformRotation;
            cropState.mirrored = this.mirrored;
            cropState.stateScale = this.stateScale;
            cropState.scale = this.scale;
            cropState.matrix = this.matrix;
            cropState.width = this.width;
            cropState.height = this.height;
            cropState.freeform = this.freeform;
            cropState.lockedAspectRatio = this.lockedAspectRatio;
            cropState.initied = this.initied;
            return cropState;
        }
    }

    public static class MediaEditState {
        public long averageDuration;
        public CharSequence caption;
        public CropState cropState;
        public ArrayList<VideoEditedInfo.MediaEntity> croppedMediaEntities;
        public String croppedPaintPath;
        public VideoEditedInfo editedInfo;
        public ArrayList<TLRPC$MessageEntity> entities;
        public String filterPath;
        public String fullPaintPath;
        public String imagePath;
        public boolean isCropped;
        public boolean isFiltered;
        public boolean isPainted;
        public ArrayList<VideoEditedInfo.MediaEntity> mediaEntities;
        public String paintPath;
        public SavedFilterState savedFilterState;
        public ArrayList<TLRPC$InputDocument> stickers;
        public String thumbPath;
        public int ttl;

        public String getPath() {
            return null;
        }

        public void reset() {
            this.caption = null;
            this.thumbPath = null;
            this.filterPath = null;
            this.imagePath = null;
            this.paintPath = null;
            this.croppedPaintPath = null;
            this.isFiltered = false;
            this.isPainted = false;
            this.isCropped = false;
            this.ttl = 0;
            this.mediaEntities = null;
            this.editedInfo = null;
            this.entities = null;
            this.savedFilterState = null;
            this.stickers = null;
            this.cropState = null;
        }

        public void copyFrom(MediaEditState mediaEditState) {
            this.caption = mediaEditState.caption;
            this.thumbPath = mediaEditState.thumbPath;
            this.imagePath = mediaEditState.imagePath;
            this.filterPath = mediaEditState.filterPath;
            this.paintPath = mediaEditState.paintPath;
            this.croppedPaintPath = mediaEditState.croppedPaintPath;
            this.fullPaintPath = mediaEditState.fullPaintPath;
            this.entities = mediaEditState.entities;
            this.savedFilterState = mediaEditState.savedFilterState;
            this.mediaEntities = mediaEditState.mediaEntities;
            this.croppedMediaEntities = mediaEditState.croppedMediaEntities;
            this.stickers = mediaEditState.stickers;
            this.editedInfo = mediaEditState.editedInfo;
            this.averageDuration = mediaEditState.averageDuration;
            this.isFiltered = mediaEditState.isFiltered;
            this.isPainted = mediaEditState.isPainted;
            this.isCropped = mediaEditState.isCropped;
            this.ttl = mediaEditState.ttl;
            this.cropState = mediaEditState.cropState;
        }
    }

    public static class PhotoEntry extends MediaEditState {
        public int bucketId;
        public boolean canDeleteAfter;
        public long dateTaken;
        public int duration;
        public int height;
        public int imageId;
        public boolean isMuted;
        public boolean isVideo;
        public int orientation;
        public String path;
        public long size;
        public int width;

        public PhotoEntry(int i, int i2, long j, String str, int i3, boolean z, int i4, int i5, long j2) {
            this.bucketId = i;
            this.imageId = i2;
            this.dateTaken = j;
            this.path = str;
            this.width = i4;
            this.height = i5;
            this.size = j2;
            if (z) {
                this.duration = i3;
            } else {
                this.orientation = i3;
            }
            this.isVideo = z;
        }

        @Override
        public String getPath() {
            return this.path;
        }

        @Override
        public void reset() {
            if (this.isVideo && this.filterPath != null) {
                new File(this.filterPath).delete();
                this.filterPath = null;
            }
            super.reset();
        }
    }

    public static class SearchImage extends MediaEditState {
        public CharSequence caption;
        public int date;
        public TLRPC$Document document;
        public int height;
        public String id;
        public String imageUrl;
        public TLRPC$BotInlineResult inlineResult;
        public HashMap<String, String> params;
        public TLRPC$Photo photo;
        public TLRPC$PhotoSize photoSize;
        public int size;
        public TLRPC$PhotoSize thumbPhotoSize;
        public String thumbUrl;
        public int type;
        public int width;

        @Override
        public String getPath() {
            if (this.photoSize != null) {
                return FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(this.photoSize, true).getAbsolutePath();
            }
            if (this.document != null) {
                return FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(this.document, true).getAbsolutePath();
            }
            return ImageLoader.getHttpFilePath(this.imageUrl, "jpg").getAbsolutePath();
        }

        @Override
        public void reset() {
            super.reset();
        }

        public String getAttachName() {
            TLRPC$PhotoSize tLRPC$PhotoSize = this.photoSize;
            if (tLRPC$PhotoSize != null) {
                return FileLoader.getAttachFileName(tLRPC$PhotoSize);
            }
            TLRPC$Document tLRPC$Document = this.document;
            if (tLRPC$Document != null) {
                return FileLoader.getAttachFileName(tLRPC$Document);
            }
            return Utilities.MD5(this.imageUrl) + "." + ImageLoader.getHttpUrlExtension(this.imageUrl, "jpg");
        }

        public String getPathToAttach() {
            if (this.photoSize != null) {
                return FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(this.photoSize, true).getAbsolutePath();
            }
            if (this.document != null) {
                return FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(this.document, true).getAbsolutePath();
            }
            return this.imageUrl;
        }
    }

    public void lambda$new$0(int i) {
        if (i != 1) {
            this.hasRecordAudioFocus = false;
        }
    }

    public static class VideoConvertMessage {
        public int currentAccount;
        public MessageObject messageObject;
        public VideoEditedInfo videoEditedInfo;

        public VideoConvertMessage(MessageObject messageObject, VideoEditedInfo videoEditedInfo) {
            this.messageObject = messageObject;
            this.currentAccount = messageObject.currentAccount;
            this.videoEditedInfo = videoEditedInfo;
        }
    }

    public class AnonymousClass2 implements Runnable {
        AnonymousClass2() {
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.AnonymousClass2.run():void");
        }

        public void lambda$run$1(final ByteBuffer byteBuffer, boolean z) {
            int i;
            while (byteBuffer.hasRemaining()) {
                if (byteBuffer.remaining() > MediaController.this.fileBuffer.remaining()) {
                    i = byteBuffer.limit();
                    byteBuffer.limit(MediaController.this.fileBuffer.remaining() + byteBuffer.position());
                } else {
                    i = -1;
                }
                MediaController.this.fileBuffer.put(byteBuffer);
                if (MediaController.this.fileBuffer.position() == MediaController.this.fileBuffer.limit() || z) {
                    MediaController mediaController = MediaController.this;
                    if (mediaController.writeFrame(mediaController.fileBuffer, !z ? MediaController.this.fileBuffer.limit() : byteBuffer.position()) != 0) {
                        MediaController.this.fileBuffer.rewind();
                        MediaController mediaController2 = MediaController.this;
                        MediaController.access$1614(mediaController2, (mediaController2.fileBuffer.limit() / 2) / (MediaController.this.sampleRate / 1000));
                    }
                }
                if (i != -1) {
                    byteBuffer.limit(i);
                }
            }
            MediaController.this.recordQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    MediaController.AnonymousClass2.this.lambda$run$0(byteBuffer);
                }
            });
        }

        public void lambda$run$0(ByteBuffer byteBuffer) {
            MediaController.this.recordBuffers.add(byteBuffer);
        }

        public void lambda$run$2(double d) {
            NotificationCenter.getInstance(MediaController.this.recordingCurrentAccount).postNotificationName(NotificationCenter.recordProgressChanged, Integer.valueOf(MediaController.this.recordingGuid), Double.valueOf(d));
        }
    }

    public class InternalObserver extends ContentObserver {
        public InternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean z) {
            super.onChange(z);
            MediaController.this.processMediaObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
    }

    public class ExternalObserver extends ContentObserver {
        public ExternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean z) {
            super.onChange(z);
            MediaController.this.processMediaObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
    }

    public static class GalleryObserverInternal extends ContentObserver {
        public GalleryObserverInternal() {
            super(null);
        }

        private void scheduleReloadRunnable() {
            AndroidUtilities.runOnUIThread(MediaController.refreshGalleryRunnable = new Runnable() {
                @Override
                public final void run() {
                    MediaController.GalleryObserverInternal.this.lambda$scheduleReloadRunnable$0();
                }
            }, 2000L);
        }

        public void lambda$scheduleReloadRunnable$0() {
            if (PhotoViewer.getInstance().isVisible()) {
                scheduleReloadRunnable();
                return;
            }
            Runnable unused = MediaController.refreshGalleryRunnable = null;
            MediaController.loadGalleryPhotosAlbums(0);
        }

        @Override
        public void onChange(boolean z) {
            super.onChange(z);
            if (MediaController.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
            }
            scheduleReloadRunnable();
        }
    }

    public static class GalleryObserverExternal extends ContentObserver {
        public GalleryObserverExternal() {
            super(null);
        }

        @Override
        public void onChange(boolean z) {
            super.onChange(z);
            if (MediaController.refreshGalleryRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(MediaController.refreshGalleryRunnable);
            }
            AndroidUtilities.runOnUIThread(MediaController.refreshGalleryRunnable = MediaController$GalleryObserverExternal$$ExternalSyntheticLambda0.INSTANCE, 2000L);
        }

        public static void lambda$onChange$0() {
            Runnable unused = MediaController.refreshGalleryRunnable = null;
            MediaController.loadGalleryPhotosAlbums(0);
        }
    }

    public static void checkGallery() {
        AlbumEntry albumEntry;
        if (Build.VERSION.SDK_INT >= 24 && (albumEntry = allPhotosAlbumEntry) != null) {
            final int size = albumEntry.photos.size();
            Utilities.globalQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    MediaController.lambda$checkGallery$1(size);
                }
            }, 2000L);
        }
    }

    public static void lambda$checkGallery$1(int r13) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$checkGallery$1(int):void");
    }

    public final class StopMediaObserverRunnable implements Runnable {
        public int currentObserverToken;

        private StopMediaObserverRunnable() {
            this.currentObserverToken = 0;
        }

        @Override
        public void run() {
            if (this.currentObserverToken == MediaController.this.startObserverToken) {
                try {
                    if (MediaController.this.internalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.internalObserver);
                        MediaController.this.internalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
                try {
                    if (MediaController.this.externalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(MediaController.this.externalObserver);
                        MediaController.this.externalObserver = null;
                    }
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
            }
        }
    }

    public static MediaController getInstance() {
        MediaController mediaController = Instance;
        if (mediaController == null) {
            synchronized (MediaController.class) {
                mediaController = Instance;
                if (mediaController == null) {
                    mediaController = new MediaController();
                    Instance = mediaController;
                }
            }
        }
        return mediaController;
    }

    public MediaController() {
        DispatchQueue dispatchQueue = new DispatchQueue("recordQueue");
        this.recordQueue = dispatchQueue;
        dispatchQueue.setPriority(10);
        DispatchQueue dispatchQueue2 = new DispatchQueue("fileEncodingQueue");
        this.fileEncodingQueue = dispatchQueue2;
        dispatchQueue2.setPriority(10);
        this.recordQueue.postRunnable(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$new$2();
            }
        });
        Utilities.globalQueue.postRunnable(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$new$3();
            }
        });
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$new$4();
            }
        });
        String[] strArr = new String[7];
        strArr[0] = "_data";
        strArr[1] = "_display_name";
        strArr[2] = "bucket_display_name";
        strArr[3] = Build.VERSION.SDK_INT > 28 ? "date_modified" : "datetaken";
        strArr[4] = "title";
        strArr[5] = "width";
        strArr[6] = "height";
        this.mediaProjections = strArr;
        ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
        try {
            contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
        } catch (Exception e2) {
            FileLog.e(e2);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, new GalleryObserverExternal());
        } catch (Exception e3) {
            FileLog.e(e3);
        }
        try {
            contentResolver.registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, new GalleryObserverInternal());
        } catch (Exception e4) {
            FileLog.e(e4);
        }
    }

    public void lambda$new$2() {
        try {
            this.sampleRate = 48000;
            int minBufferSize = AudioRecord.getMinBufferSize(48000, 16, 2);
            if (minBufferSize <= 0) {
                minBufferSize = 1280;
            }
            this.recordBufferSize = minBufferSize;
            for (int i = 0; i < 5; i++) {
                ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.recordBufferSize);
                allocateDirect.order(ByteOrder.nativeOrder());
                this.recordBuffers.add(allocateDirect);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void lambda$new$3() {
        try {
            this.currentPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("playbackSpeed", VOLUME_NORMAL);
            this.currentMusicPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("musicPlaybackSpeed", VOLUME_NORMAL);
            this.fastPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("fastPlaybackSpeed", 1.8f);
            this.fastMusicPlaybackSpeed = MessagesController.getGlobalMainSettings().getFloat("fastMusicPlaybackSpeed", 1.8f);
            SensorManager sensorManager = (SensorManager) ApplicationLoader.applicationContext.getSystemService("sensor");
            this.sensorManager = sensorManager;
            this.linearSensor = sensorManager.getDefaultSensor(10);
            Sensor defaultSensor = this.sensorManager.getDefaultSensor(9);
            this.gravitySensor = defaultSensor;
            if (this.linearSensor == null || defaultSensor == null) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("gravity or linear sensor not found");
                }
                this.accelerometerSensor = this.sensorManager.getDefaultSensor(1);
                this.linearSensor = null;
                this.gravitySensor = null;
            }
            this.proximitySensor = this.sensorManager.getDefaultSensor(8);
            this.proximityWakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(32, "telegram:proximity_lock");
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            AnonymousClass4 r1 = new AnonymousClass4();
            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService("phone");
            if (telephonyManager != null) {
                telephonyManager.listen(r1, 32);
            }
        } catch (Exception e2) {
            FileLog.e(e2);
        }
    }

    public class AnonymousClass4 extends PhoneStateListener {
        AnonymousClass4() {
        }

        @Override
        public void onCallStateChanged(final int i, String str) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.AnonymousClass4.this.lambda$onCallStateChanged$0(i);
                }
            });
        }

        public void lambda$onCallStateChanged$0(int i) {
            if (i == 1) {
                MediaController mediaController = MediaController.this;
                if (mediaController.isPlayingMessage(mediaController.playingMessageObject) && !MediaController.this.isMessagePaused()) {
                    MediaController mediaController2 = MediaController.this;
                    mediaController2.lambda$startAudioAgain$7(mediaController2.playingMessageObject);
                } else if (!(MediaController.this.recordStartRunnable == null && MediaController.this.recordingAudio == null)) {
                    MediaController.this.stopRecording(2, false, 0);
                }
                EmbedBottomSheet embedBottomSheet = EmbedBottomSheet.getInstance();
                if (embedBottomSheet != null) {
                    embedBottomSheet.pause();
                }
                MediaController.this.callInProgress = true;
            } else if (i == 0) {
                MediaController.this.callInProgress = false;
            } else if (i == 2) {
                EmbedBottomSheet embedBottomSheet2 = EmbedBottomSheet.getInstance();
                if (embedBottomSheet2 != null) {
                    embedBottomSheet2.pause();
                }
                MediaController.this.callInProgress = true;
            }
        }
    }

    public void lambda$new$4() {
        for (int i = 0; i < 4; i++) {
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.fileLoaded);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.httpFileDidLoad);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.didReceiveNewMessages);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagesDeleted);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.removeAllMessagesFromDialog);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.musicDidLoad);
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.mediaDidLoad);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.playerDidStartPlaying);
        }
    }

    @Override
    public void onAudioFocusChange(final int i) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$onAudioFocusChange$5(i);
            }
        });
    }

    public void lambda$onAudioFocusChange$5(int i) {
        if (i == -1) {
            if (isPlayingMessage(getPlayingMessageObject()) && !isMessagePaused()) {
                lambda$startAudioAgain$7(this.playingMessageObject);
            }
            this.hasAudioFocus = 0;
            this.audioFocus = 0;
        } else if (i == 1) {
            this.audioFocus = 2;
            if (this.resumeAudioOnFocusGain) {
                this.resumeAudioOnFocusGain = false;
                if (isPlayingMessage(getPlayingMessageObject()) && isMessagePaused()) {
                    playMessage(getPlayingMessageObject());
                }
            }
        } else if (i == -3) {
            this.audioFocus = 1;
        } else if (i == -2) {
            this.audioFocus = 0;
            if (isPlayingMessage(getPlayingMessageObject()) && !isMessagePaused()) {
                lambda$startAudioAgain$7(this.playingMessageObject);
                this.resumeAudioOnFocusGain = true;
            }
        }
        setPlayerVolume();
    }

    public void setPlayerVolume() {
        try {
            float f = this.audioFocus != 1 ? VOLUME_NORMAL : VOLUME_DUCK;
            VideoPlayer videoPlayer = this.audioPlayer;
            if (videoPlayer != null) {
                videoPlayer.setVolume(f * this.audioVolume);
            } else {
                VideoPlayer videoPlayer2 = this.videoPlayer;
                if (videoPlayer2 != null) {
                    videoPlayer2.setVolume(f);
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public VideoPlayer getVideoPlayer() {
        return this.videoPlayer;
    }

    private void startProgressTimer(MessageObject messageObject) {
        synchronized (this.progressTimerSync) {
            Timer timer = this.progressTimer;
            if (timer != null) {
                try {
                    timer.cancel();
                    this.progressTimer = null;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            messageObject.getFileName();
            Timer timer2 = new Timer();
            this.progressTimer = timer2;
            timer2.schedule(new AnonymousClass5(messageObject), 0L, 17L);
        }
    }

    public class AnonymousClass5 extends TimerTask {
        final MessageObject val$currentPlayingMessageObject;

        AnonymousClass5(MessageObject messageObject) {
            this.val$currentPlayingMessageObject = messageObject;
        }

        @Override
        public void run() {
            synchronized (MediaController.this.sync) {
                final MessageObject messageObject = this.val$currentPlayingMessageObject;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.AnonymousClass5.this.lambda$run$1(messageObject);
                    }
                });
            }
        }

        public void lambda$run$1(MessageObject messageObject) {
            long j;
            long j2;
            final float f;
            float f2;
            if (!(MediaController.this.audioPlayer == null && MediaController.this.videoPlayer == null) && !MediaController.this.isPaused) {
                try {
                    if (MediaController.this.videoPlayer != null) {
                        j2 = MediaController.this.videoPlayer.getDuration();
                        j = MediaController.this.videoPlayer.getCurrentPosition();
                        if (j >= 0 && j2 > 0) {
                            float f3 = (float) j2;
                            f2 = ((float) MediaController.this.videoPlayer.getBufferedPosition()) / f3;
                            f = ((float) j) / f3;
                            if (f >= MediaController.VOLUME_NORMAL) {
                                return;
                            }
                        }
                        return;
                    }
                    j2 = MediaController.this.audioPlayer.getDuration();
                    j = MediaController.this.audioPlayer.getCurrentPosition();
                    float f4 = j2 >= 0 ? ((float) j) / ((float) j2) : 0.0f;
                    f2 = ((float) MediaController.this.audioPlayer.getBufferedPosition()) / ((float) j2);
                    if (j2 != -9223372036854775807L && j >= 0 && MediaController.this.seekToProgressPending == 0.0f) {
                        f = f4;
                    }
                    return;
                    MediaController.this.lastProgress = j;
                    messageObject.audioPlayerDuration = (int) (j2 / 1000);
                    messageObject.audioProgress = f;
                    messageObject.audioProgressSec = (int) (MediaController.this.lastProgress / 1000);
                    messageObject.bufferedProgress = f2;
                    if (f >= 0.0f && MediaController.this.shouldSavePositionForCurrentAudio != null && SystemClock.elapsedRealtime() - MediaController.this.lastSaveTime >= 1000) {
                        final String str = MediaController.this.shouldSavePositionForCurrentAudio;
                        MediaController.this.lastSaveTime = SystemClock.elapsedRealtime();
                        Utilities.globalQueue.postRunnable(new Runnable() {
                            @Override
                            public final void run() {
                                MediaController.AnonymousClass5.lambda$run$0(str, f);
                            }
                        });
                    }
                    NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(messageObject.getId()), Float.valueOf(f));
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }

        public static void lambda$run$0(String str, float f) {
            ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).edit().putFloat(str, f).commit();
        }
    }

    private void stopProgressTimer() {
        synchronized (this.progressTimerSync) {
            Timer timer = this.progressTimer;
            if (timer != null) {
                try {
                    timer.cancel();
                    this.progressTimer = null;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
    }

    public void cleanup() {
        cleanupPlayer(true, true);
        this.audioInfo = null;
        this.playMusicAgain = false;
        for (int i = 0; i < 4; i++) {
            DownloadController.getInstance(i).cleanup();
        }
        this.videoConvertQueue.clear();
        this.generatingWaveform.clear();
        this.voiceMessagesPlaylist = null;
        this.voiceMessagesPlaylistMap = null;
        clearPlaylist();
        cancelVideoConvert(null);
    }

    private void clearPlaylist() {
        this.playlist.clear();
        this.playlistMap.clear();
        this.shuffledPlaylist.clear();
        this.playlistClassGuid = 0;
        boolean[] zArr = this.playlistEndReached;
        zArr[1] = false;
        zArr[0] = false;
        this.playlistMergeDialogId = 0L;
        int[] iArr = this.playlistMaxId;
        iArr[1] = Integer.MAX_VALUE;
        iArr[0] = Integer.MAX_VALUE;
        this.loadingPlaylist = false;
        this.playlistGlobalSearchParams = null;
    }

    public void startMediaObserver() {
        ApplicationLoader.applicationHandler.removeCallbacks(this.stopMediaObserverRunnable);
        this.startObserverToken++;
        try {
            if (this.internalObserver == null) {
                ContentResolver contentResolver = ApplicationLoader.applicationContext.getContentResolver();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ExternalObserver externalObserver = new ExternalObserver();
                this.externalObserver = externalObserver;
                contentResolver.registerContentObserver(uri, false, externalObserver);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            if (this.externalObserver == null) {
                ContentResolver contentResolver2 = ApplicationLoader.applicationContext.getContentResolver();
                Uri uri2 = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                InternalObserver internalObserver = new InternalObserver();
                this.internalObserver = internalObserver;
                contentResolver2.registerContentObserver(uri2, false, internalObserver);
            }
        } catch (Exception e2) {
            FileLog.e(e2);
        }
    }

    public void stopMediaObserver() {
        if (this.stopMediaObserverRunnable == null) {
            this.stopMediaObserverRunnable = new StopMediaObserverRunnable();
        }
        this.stopMediaObserverRunnable.currentObserverToken = this.startObserverToken;
        ApplicationLoader.applicationHandler.postDelayed(this.stopMediaObserverRunnable, 5000L);
    }

    public void processMediaObserver(android.net.Uri r14) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.processMediaObserver(android.net.Uri):void");
    }

    public void lambda$processMediaObserver$6(ArrayList arrayList) {
        NotificationCenter.getInstance(this.lastChatAccount).postNotificationName(NotificationCenter.screenshotTook, new Object[0]);
        checkScreenshots(arrayList);
    }

    private void checkScreenshots(ArrayList<Long> arrayList) {
        if (arrayList != null && !arrayList.isEmpty() && this.lastChatEnterTime != 0) {
            if (this.lastUser != null || (this.lastSecretChat instanceof TLRPC$TL_encryptedChat)) {
                boolean z = false;
                for (int i = 0; i < arrayList.size(); i++) {
                    Long l = arrayList.get(i);
                    if ((this.lastMediaCheckTime == 0 || l.longValue() > this.lastMediaCheckTime) && l.longValue() >= this.lastChatEnterTime && (this.lastChatLeaveTime == 0 || l.longValue() <= this.lastChatLeaveTime + 2000)) {
                        this.lastMediaCheckTime = Math.max(this.lastMediaCheckTime, l.longValue());
                        z = true;
                    }
                }
                if (!z) {
                    return;
                }
                if (this.lastSecretChat != null) {
                    SecretChatHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastSecretChat, this.lastChatVisibleMessages, null);
                } else {
                    SendMessagesHelper.getInstance(this.lastChatAccount).sendScreenshotMessage(this.lastUser, this.lastMessageId, null);
                }
            }
        }
    }

    public void setLastVisibleMessageIds(int i, long j, long j2, TLRPC$User tLRPC$User, TLRPC$EncryptedChat tLRPC$EncryptedChat, ArrayList<Long> arrayList, int i2) {
        this.lastChatEnterTime = j;
        this.lastChatLeaveTime = j2;
        this.lastChatAccount = i;
        this.lastSecretChat = tLRPC$EncryptedChat;
        this.lastUser = tLRPC$User;
        this.lastMessageId = i2;
        this.lastChatVisibleMessages = arrayList;
    }

    @Override
    public void didReceivedNotification(int i, int i2, Object... objArr) {
        ArrayList<MessageObject> arrayList;
        int indexOf;
        int i3 = 0;
        if (i == NotificationCenter.fileLoaded || i == NotificationCenter.httpFileDidLoad) {
            String str = (String) objArr[0];
            MessageObject messageObject = this.playingMessageObject;
            if (messageObject != null && messageObject.currentAccount == i2 && FileLoader.getAttachFileName(messageObject.getDocument()).equals(str)) {
                if (this.downloadingCurrentMessage) {
                    this.playMusicAgain = true;
                    playMessage(this.playingMessageObject);
                } else if (this.audioInfo == null) {
                    try {
                        this.audioInfo = AudioInfo.getAudioInfo(FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(this.playingMessageObject.messageOwner));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            }
        } else if (i == NotificationCenter.messagesDeleted) {
            if (!((Boolean) objArr[2]).booleanValue()) {
                long longValue = ((Long) objArr[1]).longValue();
                ArrayList arrayList2 = (ArrayList) objArr[0];
                MessageObject messageObject2 = this.playingMessageObject;
                if (messageObject2 != null && longValue == messageObject2.messageOwner.peer_id.channel_id && arrayList2.contains(Integer.valueOf(messageObject2.getId()))) {
                    cleanupPlayer(true, true);
                }
                ArrayList<MessageObject> arrayList3 = this.voiceMessagesPlaylist;
                if (!(arrayList3 == null || arrayList3.isEmpty() || longValue != this.voiceMessagesPlaylist.get(0).messageOwner.peer_id.channel_id)) {
                    while (i3 < arrayList2.size()) {
                        Integer num = (Integer) arrayList2.get(i3);
                        MessageObject messageObject3 = this.voiceMessagesPlaylistMap.get(num.intValue());
                        this.voiceMessagesPlaylistMap.remove(num.intValue());
                        if (messageObject3 != null) {
                            this.voiceMessagesPlaylist.remove(messageObject3);
                        }
                        i3++;
                    }
                }
            }
        } else if (i == NotificationCenter.removeAllMessagesFromDialog) {
            long longValue2 = ((Long) objArr[0]).longValue();
            MessageObject messageObject4 = this.playingMessageObject;
            if (messageObject4 != null && messageObject4.getDialogId() == longValue2) {
                cleanupPlayer(false, true);
            }
        } else if (i == NotificationCenter.musicDidLoad) {
            long longValue3 = ((Long) objArr[0]).longValue();
            MessageObject messageObject5 = this.playingMessageObject;
            if (messageObject5 != null && messageObject5.isMusic() && this.playingMessageObject.getDialogId() == longValue3 && !this.playingMessageObject.scheduled) {
                this.playlist.addAll(0, (ArrayList) objArr[1]);
                this.playlist.addAll((ArrayList) objArr[2]);
                int size = this.playlist.size();
                for (int i4 = 0; i4 < size; i4++) {
                    MessageObject messageObject6 = this.playlist.get(i4);
                    this.playlistMap.put(Integer.valueOf(messageObject6.getId()), messageObject6);
                    int[] iArr = this.playlistMaxId;
                    iArr[0] = Math.min(iArr[0], messageObject6.getId());
                }
                sortPlaylist();
                if (SharedConfig.shuffleMusic) {
                    buildShuffledPlayList();
                } else {
                    MessageObject messageObject7 = this.playingMessageObject;
                    if (messageObject7 != null && (indexOf = this.playlist.indexOf(messageObject7)) >= 0) {
                        this.currentPlaylistNum = indexOf;
                    }
                }
                this.playlistClassGuid = ConnectionsManager.generateClassGuid();
            }
        } else if (i == NotificationCenter.mediaDidLoad) {
            if (((Integer) objArr[3]).intValue() == this.playlistClassGuid && this.playingMessageObject != null) {
                long longValue4 = ((Long) objArr[0]).longValue();
                ((Integer) objArr[4]).intValue();
                ArrayList arrayList4 = (ArrayList) objArr[2];
                DialogObject.isEncryptedDialog(longValue4);
                char c = longValue4 == this.playlistMergeDialogId ? (char) 1 : (char) 0;
                if (!arrayList4.isEmpty()) {
                    this.playlistEndReached[c] = ((Boolean) objArr[5]).booleanValue();
                }
                int i5 = 0;
                for (int i6 = 0; i6 < arrayList4.size(); i6++) {
                    MessageObject messageObject8 = (MessageObject) arrayList4.get(i6);
                    if (!this.playlistMap.containsKey(Integer.valueOf(messageObject8.getId()))) {
                        i5++;
                        this.playlist.add(0, messageObject8);
                        this.playlistMap.put(Integer.valueOf(messageObject8.getId()), messageObject8);
                        int[] iArr2 = this.playlistMaxId;
                        iArr2[c] = Math.min(iArr2[c], messageObject8.getId());
                    }
                }
                sortPlaylist();
                int indexOf2 = this.playlist.indexOf(this.playingMessageObject);
                if (indexOf2 >= 0) {
                    this.currentPlaylistNum = indexOf2;
                }
                this.loadingPlaylist = false;
                if (SharedConfig.shuffleMusic) {
                    buildShuffledPlayList();
                }
                if (i5 != 0) {
                    NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.moreMusicDidLoad, Integer.valueOf(i5));
                }
            }
        } else if (i == NotificationCenter.didReceiveNewMessages) {
            if (!((Boolean) objArr[2]).booleanValue() && (arrayList = this.voiceMessagesPlaylist) != null && !arrayList.isEmpty() && ((Long) objArr[0]).longValue() == this.voiceMessagesPlaylist.get(0).getDialogId()) {
                ArrayList arrayList5 = (ArrayList) objArr[1];
                while (i3 < arrayList5.size()) {
                    MessageObject messageObject9 = (MessageObject) arrayList5.get(i3);
                    if ((messageObject9.isVoice() || messageObject9.isRoundVideo()) && (!this.voiceMessagesPlaylistUnread || (messageObject9.isContentUnread() && !messageObject9.isOut()))) {
                        this.voiceMessagesPlaylist.add(messageObject9);
                        this.voiceMessagesPlaylistMap.put(messageObject9.getId(), messageObject9);
                    }
                    i3++;
                }
            }
        } else if (i == NotificationCenter.playerDidStartPlaying && !getInstance().isCurrentPlayer((VideoPlayer) objArr[0])) {
            getInstance().lambda$startAudioAgain$7(getInstance().getPlayingMessageObject());
        }
    }

    public boolean isRecordingAudio() {
        return (this.recordStartRunnable == null && this.recordingAudio == null) ? false : true;
    }

    private boolean isNearToSensor(float f) {
        return f < 5.0f && f != this.proximitySensor.getMaximumRange();
    }

    public boolean isRecordingOrListeningByProximity() {
        MessageObject messageObject;
        return this.proximityTouched && (isRecordingAudio() || ((messageObject = this.playingMessageObject) != null && (messageObject.isVoice() || this.playingMessageObject.isRoundVideo())));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        PowerManager.WakeLock wakeLock;
        PowerManager.WakeLock wakeLock2;
        PowerManager.WakeLock wakeLock3;
        PowerManager.WakeLock wakeLock4;
        PowerManager.WakeLock wakeLock5;
        int i;
        boolean z;
        double d;
        if (this.sensorsStarted && VoIPService.getSharedInstance() == null) {
            Sensor sensor = sensorEvent.sensor;
            if (sensor == this.proximitySensor) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("proximity changed to " + sensorEvent.values[0] + " max value = " + this.proximitySensor.getMaximumRange());
                }
                float f = this.lastProximityValue;
                if (f == -100.0f) {
                    this.lastProximityValue = sensorEvent.values[0];
                } else if (f != sensorEvent.values[0]) {
                    this.proximityHasDifferentValues = true;
                }
                if (this.proximityHasDifferentValues) {
                    this.proximityTouched = isNearToSensor(sensorEvent.values[0]);
                }
            } else if (sensor == this.accelerometerSensor) {
                long j = this.lastTimestamp;
                if (j == 0) {
                    d = 0.9800000190734863d;
                } else {
                    double d2 = sensorEvent.timestamp - j;
                    Double.isNaN(d2);
                    d = 1.0d / ((d2 / 1.0E9d) + 1.0d);
                }
                this.lastTimestamp = sensorEvent.timestamp;
                float[] fArr = this.gravity;
                double d3 = fArr[0];
                Double.isNaN(d3);
                double d4 = 1.0d - d;
                float[] fArr2 = sensorEvent.values;
                double d5 = fArr2[0];
                Double.isNaN(d5);
                fArr[0] = (float) ((d3 * d) + (d5 * d4));
                double d6 = fArr[1];
                Double.isNaN(d6);
                double d7 = fArr2[1];
                Double.isNaN(d7);
                fArr[1] = (float) ((d6 * d) + (d7 * d4));
                double d8 = fArr[2];
                Double.isNaN(d8);
                double d9 = d * d8;
                double d10 = fArr2[2];
                Double.isNaN(d10);
                fArr[2] = (float) (d9 + (d4 * d10));
                float[] fArr3 = this.gravityFast;
                fArr3[0] = (fArr[0] * 0.8f) + (fArr2[0] * 0.19999999f);
                fArr3[1] = (fArr[1] * 0.8f) + (fArr2[1] * 0.19999999f);
                fArr3[2] = (fArr[2] * 0.8f) + (fArr2[2] * 0.19999999f);
                float[] fArr4 = this.linearAcceleration;
                fArr4[0] = fArr2[0] - fArr[0];
                fArr4[1] = fArr2[1] - fArr[1];
                fArr4[2] = fArr2[2] - fArr[2];
            } else if (sensor == this.linearSensor) {
                float[] fArr5 = this.linearAcceleration;
                float[] fArr6 = sensorEvent.values;
                fArr5[0] = fArr6[0];
                fArr5[1] = fArr6[1];
                fArr5[2] = fArr6[2];
            } else if (sensor == this.gravitySensor) {
                float[] fArr7 = this.gravityFast;
                float[] fArr8 = this.gravity;
                float[] fArr9 = sensorEvent.values;
                float f2 = fArr9[0];
                fArr8[0] = f2;
                fArr7[0] = f2;
                float f3 = fArr9[1];
                fArr8[1] = f3;
                fArr7[1] = f3;
                float f4 = fArr9[2];
                fArr8[2] = f4;
                fArr7[2] = f4;
            }
            Sensor sensor2 = sensorEvent.sensor;
            if (sensor2 == this.linearSensor || sensor2 == this.gravitySensor || sensor2 == this.accelerometerSensor) {
                float[] fArr10 = this.gravity;
                float f5 = fArr10[0];
                float[] fArr11 = this.linearAcceleration;
                float f6 = (f5 * fArr11[0]) + (fArr10[1] * fArr11[1]) + (fArr10[2] * fArr11[2]);
                int i2 = this.raisedToBack;
                if (i2 != 6 && ((f6 > 0.0f && this.previousAccValue > 0.0f) || (f6 < 0.0f && this.previousAccValue < 0.0f))) {
                    if (f6 > 0.0f) {
                        z = f6 > 15.0f;
                        i = 1;
                    } else {
                        z = f6 < -15.0f;
                        i = 2;
                    }
                    int i3 = this.raisedToTopSign;
                    if (i3 != 0 && i3 != i) {
                        int i4 = this.raisedToTop;
                        if (i4 != 6 || !z) {
                            if (!z) {
                                this.countLess++;
                            }
                            if (!(this.countLess != 10 && i4 == 6 && i2 == 0)) {
                                this.raisedToTop = 0;
                                this.raisedToTopSign = 0;
                                this.raisedToBack = 0;
                                this.countLess = 0;
                            }
                        } else if (i2 < 6) {
                            int i5 = i2 + 1;
                            this.raisedToBack = i5;
                            if (i5 == 6) {
                                this.raisedToTop = 0;
                                this.raisedToTopSign = 0;
                                this.countLess = 0;
                                this.timeSinceRaise = System.currentTimeMillis();
                                if (BuildVars.LOGS_ENABLED && BuildVars.DEBUG_PRIVATE_VERSION) {
                                    FileLog.d("motion detected");
                                }
                            }
                        }
                    } else if (z && i2 == 0 && (i3 == 0 || i3 == i)) {
                        int i6 = this.raisedToTop;
                        if (i6 < 6 && !this.proximityTouched) {
                            this.raisedToTopSign = i;
                            int i7 = i6 + 1;
                            this.raisedToTop = i7;
                            if (i7 == 6) {
                                this.countLess = 0;
                            }
                        }
                    } else {
                        if (!z) {
                            this.countLess++;
                        }
                        if (!(i3 == i && this.countLess != 10 && this.raisedToTop == 6 && i2 == 0)) {
                            this.raisedToBack = 0;
                            this.raisedToTop = 0;
                            this.raisedToTopSign = 0;
                            this.countLess = 0;
                        }
                    }
                }
                this.previousAccValue = f6;
                float[] fArr12 = this.gravityFast;
                this.accelerometerVertical = fArr12[1] > 2.5f && Math.abs(fArr12[2]) < 4.0f && Math.abs(this.gravityFast[0]) > 1.5f;
            }
            if (this.raisedToBack != 6 || !this.accelerometerVertical || !this.proximityTouched || NotificationsController.audioManager.isWiredHeadsetOn()) {
                boolean z2 = this.proximityTouched;
                if (z2) {
                    if (this.playingMessageObject != null && !ApplicationLoader.mainInterfacePaused && ((this.playingMessageObject.isVoice() || this.playingMessageObject.isRoundVideo()) && !this.useFrontSpeaker && !NotificationsController.audioManager.isWiredHeadsetOn())) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("start listen by proximity only");
                        }
                        if (this.proximityHasDifferentValues && (wakeLock3 = this.proximityWakeLock) != null && !wakeLock3.isHeld()) {
                            this.proximityWakeLock.acquire();
                        }
                        setUseFrontSpeaker(true);
                        startAudioAgain(false);
                        this.ignoreOnPause = true;
                    }
                } else if (!z2) {
                    if (this.raiseToEarRecord) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("stop record");
                        }
                        stopRecording(2, false, 0);
                        this.raiseToEarRecord = false;
                        this.ignoreOnPause = false;
                        if (this.proximityHasDifferentValues && (wakeLock2 = this.proximityWakeLock) != null && wakeLock2.isHeld()) {
                            this.proximityWakeLock.release();
                        }
                    } else if (this.useFrontSpeaker) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("stop listen");
                        }
                        this.useFrontSpeaker = false;
                        startAudioAgain(true);
                        this.ignoreOnPause = false;
                        if (this.proximityHasDifferentValues && (wakeLock = this.proximityWakeLock) != null && wakeLock.isHeld()) {
                            this.proximityWakeLock.release();
                        }
                    }
                }
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("sensor values reached");
                }
                if (this.playingMessageObject != null || this.recordStartRunnable != null || this.recordingAudio != null || PhotoViewer.getInstance().isVisible() || !ApplicationLoader.isScreenOn || this.inputFieldHasText || !this.allowStartRecord || this.raiseChat == null || this.callInProgress) {
                    MessageObject messageObject = this.playingMessageObject;
                    if (messageObject != null && ((messageObject.isVoice() || this.playingMessageObject.isRoundVideo()) && !this.useFrontSpeaker)) {
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("start listen");
                        }
                        if (this.proximityHasDifferentValues && (wakeLock4 = this.proximityWakeLock) != null && !wakeLock4.isHeld()) {
                            this.proximityWakeLock.acquire();
                        }
                        setUseFrontSpeaker(true);
                        startAudioAgain(false);
                        this.ignoreOnPause = true;
                    }
                } else if (!this.raiseToEarRecord) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("start record");
                    }
                    this.useFrontSpeaker = true;
                    if (!this.raiseChat.playFirstUnreadVoiceMessage()) {
                        this.raiseToEarRecord = true;
                        this.useFrontSpeaker = false;
                        startRecording(this.raiseChat.getCurrentAccount(), this.raiseChat.getDialogId(), null, this.raiseChat.getThreadMessage(), this.raiseChat.getClassGuid());
                    }
                    if (this.useFrontSpeaker) {
                        setUseFrontSpeaker(true);
                    }
                    this.ignoreOnPause = true;
                    if (this.proximityHasDifferentValues && (wakeLock5 = this.proximityWakeLock) != null && !wakeLock5.isHeld()) {
                        this.proximityWakeLock.acquire();
                    }
                }
                this.raisedToBack = 0;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
            }
            if (this.timeSinceRaise != 0 && this.raisedToBack == 6 && Math.abs(System.currentTimeMillis() - this.timeSinceRaise) > 1000) {
                this.raisedToBack = 0;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
                this.timeSinceRaise = 0L;
            }
        }
    }

    private void setUseFrontSpeaker(boolean z) {
        this.useFrontSpeaker = z;
        AudioManager audioManager = NotificationsController.audioManager;
        if (z) {
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(false);
            return;
        }
        audioManager.setSpeakerphoneOn(true);
    }

    public void startRecordingIfFromSpeaker() {
        ChatActivity chatActivity;
        if (this.useFrontSpeaker && (chatActivity = this.raiseChat) != null && this.allowStartRecord && SharedConfig.raiseToSpeak) {
            this.raiseToEarRecord = true;
            startRecording(chatActivity.getCurrentAccount(), this.raiseChat.getDialogId(), null, this.raiseChat.getThreadMessage(), this.raiseChat.getClassGuid());
            this.ignoreOnPause = true;
        }
    }

    private void startAudioAgain(boolean z) {
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null) {
            int i = 0;
            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.audioRouteChanged, Boolean.valueOf(this.useFrontSpeaker));
            VideoPlayer videoPlayer = this.videoPlayer;
            if (videoPlayer != null) {
                if (!this.useFrontSpeaker) {
                    i = 3;
                }
                videoPlayer.setStreamType(i);
                if (!z) {
                    if (this.videoPlayer.getCurrentPosition() < 1000) {
                        this.videoPlayer.seekTo(0L);
                    }
                    this.videoPlayer.play();
                    return;
                }
                lambda$startAudioAgain$7(this.playingMessageObject);
                return;
            }
            VideoPlayer videoPlayer2 = this.audioPlayer;
            boolean z2 = videoPlayer2 != null;
            final MessageObject messageObject2 = this.playingMessageObject;
            float f = messageObject2.audioProgress;
            int i2 = messageObject2.audioPlayerDuration;
            if (z || videoPlayer2 == null || !videoPlayer2.isPlaying() || i2 * f > VOLUME_NORMAL) {
                messageObject2.audioProgress = f;
            } else {
                messageObject2.audioProgress = 0.0f;
            }
            cleanupPlayer(false, true);
            playMessage(messageObject2);
            if (!z) {
                return;
            }
            if (z2) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.this.lambda$startAudioAgain$7(messageObject2);
                    }
                }, 100L);
            } else {
                lambda$startAudioAgain$7(messageObject2);
            }
        }
    }

    public void setInputFieldHasText(boolean z) {
        this.inputFieldHasText = z;
    }

    public void setAllowStartRecord(boolean z) {
        this.allowStartRecord = z;
    }

    public void startRaiseToEarSensors(ChatActivity chatActivity) {
        if (chatActivity == null) {
            return;
        }
        if ((this.accelerometerSensor != null || (this.gravitySensor != null && this.linearAcceleration != null)) && this.proximitySensor != null) {
            this.raiseChat = chatActivity;
            if (!SharedConfig.raiseToSpeak) {
                MessageObject messageObject = this.playingMessageObject;
                if (messageObject == null) {
                    return;
                }
                if (!messageObject.isVoice() && !this.playingMessageObject.isRoundVideo()) {
                    return;
                }
            }
            if (!this.sensorsStarted) {
                float[] fArr = this.gravity;
                fArr[2] = 0.0f;
                fArr[1] = 0.0f;
                fArr[0] = 0.0f;
                float[] fArr2 = this.linearAcceleration;
                fArr2[2] = 0.0f;
                fArr2[1] = 0.0f;
                fArr2[0] = 0.0f;
                float[] fArr3 = this.gravityFast;
                fArr3[2] = 0.0f;
                fArr3[1] = 0.0f;
                fArr3[0] = 0.0f;
                this.lastTimestamp = 0L;
                this.previousAccValue = 0.0f;
                this.raisedToTop = 0;
                this.raisedToTopSign = 0;
                this.countLess = 0;
                this.raisedToBack = 0;
                Utilities.globalQueue.postRunnable(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.this.lambda$startRaiseToEarSensors$8();
                    }
                });
                this.sensorsStarted = true;
            }
        }
    }

    public void lambda$startRaiseToEarSensors$8() {
        Sensor sensor = this.gravitySensor;
        if (sensor != null) {
            this.sensorManager.registerListener(this, sensor, 30000);
        }
        Sensor sensor2 = this.linearSensor;
        if (sensor2 != null) {
            this.sensorManager.registerListener(this, sensor2, 30000);
        }
        Sensor sensor3 = this.accelerometerSensor;
        if (sensor3 != null) {
            this.sensorManager.registerListener(this, sensor3, 30000);
        }
        this.sensorManager.registerListener(this, this.proximitySensor, 3);
    }

    public void stopRaiseToEarSensors(ChatActivity chatActivity, boolean z) {
        PowerManager.WakeLock wakeLock;
        if (this.ignoreOnPause) {
            this.ignoreOnPause = false;
            return;
        }
        stopRecording(z ? 2 : 0, false, 0);
        if (this.sensorsStarted && !this.ignoreOnPause) {
            if ((this.accelerometerSensor != null || (this.gravitySensor != null && this.linearAcceleration != null)) && this.proximitySensor != null && this.raiseChat == chatActivity) {
                this.raiseChat = null;
                this.sensorsStarted = false;
                this.accelerometerVertical = false;
                this.proximityTouched = false;
                this.raiseToEarRecord = false;
                this.useFrontSpeaker = false;
                Utilities.globalQueue.postRunnable(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.this.lambda$stopRaiseToEarSensors$9();
                    }
                });
                if (this.proximityHasDifferentValues && (wakeLock = this.proximityWakeLock) != null && wakeLock.isHeld()) {
                    this.proximityWakeLock.release();
                }
            }
        }
    }

    public void lambda$stopRaiseToEarSensors$9() {
        Sensor sensor = this.linearSensor;
        if (sensor != null) {
            this.sensorManager.unregisterListener(this, sensor);
        }
        Sensor sensor2 = this.gravitySensor;
        if (sensor2 != null) {
            this.sensorManager.unregisterListener(this, sensor2);
        }
        Sensor sensor3 = this.accelerometerSensor;
        if (sensor3 != null) {
            this.sensorManager.unregisterListener(this, sensor3);
        }
        this.sensorManager.unregisterListener(this, this.proximitySensor);
    }

    public void cleanupPlayer(boolean z, boolean z2) {
        cleanupPlayer(z, z2, false, false);
    }

    public void cleanupPlayer(boolean z, boolean z2, boolean z3, boolean z4) {
        PipRoundVideoView pipRoundVideoView;
        MessageObject messageObject;
        if (this.audioPlayer != null) {
            ValueAnimator valueAnimator = this.audioVolumeAnimator;
            if (valueAnimator != null) {
                valueAnimator.removeAllUpdateListeners();
                this.audioVolumeAnimator.cancel();
            }
            if (!this.audioPlayer.isPlaying() || (messageObject = this.playingMessageObject) == null || messageObject.isVoice()) {
                try {
                    this.audioPlayer.releasePlayer(true);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else {
                final VideoPlayer videoPlayer = this.audioPlayer;
                ValueAnimator ofFloat = ValueAnimator.ofFloat(this.audioVolume, 0.0f);
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                        MediaController.this.lambda$cleanupPlayer$10(videoPlayer, valueAnimator2);
                    }
                });
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        try {
                            videoPlayer.releasePlayer(true);
                        } catch (Exception e2) {
                            FileLog.e(e2);
                        }
                    }
                });
                ofFloat.setDuration(300L);
                ofFloat.start();
            }
            this.audioPlayer = null;
            Theme.unrefAudioVisualizeDrawable(this.playingMessageObject);
        } else {
            VideoPlayer videoPlayer2 = this.videoPlayer;
            if (videoPlayer2 != null) {
                this.currentAspectRatioFrameLayout = null;
                this.currentTextureViewContainer = null;
                this.currentAspectRatioFrameLayoutReady = false;
                this.isDrawingWasReady = false;
                this.currentTextureView = null;
                this.goingToShowMessageObject = null;
                if (z4) {
                    PhotoViewer.getInstance().injectVideoPlayer(this.videoPlayer);
                    MessageObject messageObject2 = this.playingMessageObject;
                    this.goingToShowMessageObject = messageObject2;
                    NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingGoingToStop, this.playingMessageObject, Boolean.TRUE);
                } else {
                    long currentPosition = videoPlayer2.getCurrentPosition();
                    MessageObject messageObject3 = this.playingMessageObject;
                    if (messageObject3 != null && messageObject3.isVideo() && currentPosition > 0) {
                        MessageObject messageObject4 = this.playingMessageObject;
                        messageObject4.audioProgressMs = (int) currentPosition;
                        NotificationCenter.getInstance(messageObject4.currentAccount).postNotificationName(NotificationCenter.messagePlayingGoingToStop, this.playingMessageObject, Boolean.FALSE);
                    }
                    this.videoPlayer.releasePlayer(true);
                    this.videoPlayer = null;
                }
                try {
                    this.baseActivity.getWindow().clearFlags(ConnectionsManager.RequestFlagNeedQuickAck);
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
                if (this.playingMessageObject != null && !z4) {
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(this.playingMessageObject.currentAccount).removeLoadingVideo(this.playingMessageObject.getDocument(), true, false);
                }
            }
        }
        stopProgressTimer();
        this.lastProgress = 0L;
        this.isPaused = false;
        if (!this.useFrontSpeaker && !SharedConfig.raiseToSpeak) {
            ChatActivity chatActivity = this.raiseChat;
            stopRaiseToEarSensors(chatActivity, false);
            this.raiseChat = chatActivity;
        }
        PowerManager.WakeLock wakeLock = this.proximityWakeLock;
        if (wakeLock != null && wakeLock.isHeld() && !this.proximityTouched) {
            this.proximityWakeLock.release();
        }
        MessageObject messageObject5 = this.playingMessageObject;
        if (messageObject5 != null) {
            if (this.downloadingCurrentMessage) {
                FileLoader.getInstance(messageObject5.currentAccount).cancelLoadFile(this.playingMessageObject.getDocument());
            }
            MessageObject messageObject6 = this.playingMessageObject;
            if (z) {
                messageObject6.resetPlayingProgress();
                NotificationCenter.getInstance(messageObject6.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
            }
            this.playingMessageObject = null;
            this.downloadingCurrentMessage = false;
            if (z) {
                NotificationsController.audioManager.abandonAudioFocus(this);
                this.hasAudioFocus = 0;
                int i = -1;
                ArrayList<MessageObject> arrayList = this.voiceMessagesPlaylist;
                if (arrayList != null) {
                    if (!z3 || (i = arrayList.indexOf(messageObject6)) < 0) {
                        this.voiceMessagesPlaylist = null;
                        this.voiceMessagesPlaylistMap = null;
                    } else {
                        this.voiceMessagesPlaylist.remove(i);
                        this.voiceMessagesPlaylistMap.remove(messageObject6.getId());
                        if (this.voiceMessagesPlaylist.isEmpty()) {
                            this.voiceMessagesPlaylist = null;
                            this.voiceMessagesPlaylistMap = null;
                        }
                    }
                }
                ArrayList<MessageObject> arrayList2 = this.voiceMessagesPlaylist;
                if (arrayList2 == null || i >= arrayList2.size()) {
                    if ((messageObject6.isVoice() || messageObject6.isRoundVideo()) && messageObject6.getId() != 0) {
                        startRecordingIfFromSpeaker();
                    }
                    NotificationCenter.getInstance(messageObject6.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidReset, Integer.valueOf(messageObject6.getId()), Boolean.valueOf(z2));
                    this.pipSwitchingState = 0;
                    PipRoundVideoView pipRoundVideoView2 = this.pipRoundVideoView;
                    if (pipRoundVideoView2 != null) {
                        pipRoundVideoView2.close(true);
                        this.pipRoundVideoView = null;
                    }
                } else {
                    MessageObject messageObject7 = this.voiceMessagesPlaylist.get(i);
                    playMessage(messageObject7);
                    if (!messageObject7.isRoundVideo() && (pipRoundVideoView = this.pipRoundVideoView) != null) {
                        pipRoundVideoView.close(true);
                        this.pipRoundVideoView = null;
                    }
                }
            }
            if (z2) {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
        }
    }

    public void lambda$cleanupPlayer$10(VideoPlayer videoPlayer, ValueAnimator valueAnimator) {
        videoPlayer.setVolume((this.audioFocus != 1 ? VOLUME_NORMAL : VOLUME_DUCK) * ((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public boolean isGoingToShowMessageObject(MessageObject messageObject) {
        return this.goingToShowMessageObject == messageObject;
    }

    public void resetGoingToShowMessageObject() {
        this.goingToShowMessageObject = null;
    }

    private boolean isSamePlayingMessage(MessageObject messageObject) {
        MessageObject messageObject2 = this.playingMessageObject;
        if (messageObject2 != null && messageObject2.getDialogId() == messageObject.getDialogId() && this.playingMessageObject.getId() == messageObject.getId()) {
            if ((this.playingMessageObject.eventId == 0) == (messageObject.eventId == 0)) {
                return true;
            }
        }
        return false;
    }

    public boolean seekToProgress(MessageObject messageObject, float f) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            try {
                VideoPlayer videoPlayer = this.audioPlayer;
                if (videoPlayer != null) {
                    long duration = videoPlayer.getDuration();
                    if (duration == -9223372036854775807L) {
                        this.seekToProgressPending = f;
                    } else {
                        this.playingMessageObject.audioProgress = f;
                        long j = (int) (((float) duration) * f);
                        this.audioPlayer.seekTo(j);
                        this.lastProgress = j;
                    }
                } else {
                    VideoPlayer videoPlayer2 = this.videoPlayer;
                    if (videoPlayer2 != null) {
                        videoPlayer2.seekTo(((float) videoPlayer2.getDuration()) * f);
                    }
                }
                NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidSeek, Integer.valueOf(this.playingMessageObject.getId()), Float.valueOf(f));
                return true;
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return false;
    }

    public long getDuration() {
        VideoPlayer videoPlayer = this.audioPlayer;
        if (videoPlayer == null) {
            return 0L;
        }
        return videoPlayer.getDuration();
    }

    public MessageObject getPlayingMessageObject() {
        return this.playingMessageObject;
    }

    public int getPlayingMessageObjectNum() {
        return this.currentPlaylistNum;
    }

    private void buildShuffledPlayList() {
        if (!this.playlist.isEmpty()) {
            ArrayList arrayList = new ArrayList(this.playlist);
            this.shuffledPlaylist.clear();
            MessageObject messageObject = this.playlist.get(this.currentPlaylistNum);
            arrayList.remove(this.currentPlaylistNum);
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                int nextInt = Utilities.random.nextInt(arrayList.size());
                this.shuffledPlaylist.add((MessageObject) arrayList.get(nextInt));
                arrayList.remove(nextInt);
            }
            this.shuffledPlaylist.add(messageObject);
            this.currentPlaylistNum = this.shuffledPlaylist.size() - 1;
        }
    }

    public void loadMoreMusic() {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.loadMoreMusic():void");
    }

    public void lambda$loadMoreMusic$12(final int i, final int i2, final TLObject tLObject, final TLRPC$TL_error tLRPC$TL_error) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$loadMoreMusic$11(i, tLRPC$TL_error, tLObject, i2);
            }
        });
    }

    public void lambda$loadMoreMusic$11(int i, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, int i2) {
        PlaylistGlobalSearchParams playlistGlobalSearchParams;
        if (this.playlistClassGuid == i && (playlistGlobalSearchParams = this.playlistGlobalSearchParams) != null && this.playingMessageObject != null && tLRPC$TL_error == null) {
            this.loadingPlaylist = false;
            TLRPC$messages_Messages tLRPC$messages_Messages = (TLRPC$messages_Messages) tLObject;
            playlistGlobalSearchParams.nextSearchRate = tLRPC$messages_Messages.next_rate;
            MessagesStorage.getInstance(i2).putUsersAndChats(tLRPC$messages_Messages.users, tLRPC$messages_Messages.chats, true, true);
            MessagesController.getInstance(i2).putUsers(tLRPC$messages_Messages.users, false);
            MessagesController.getInstance(i2).putChats(tLRPC$messages_Messages.chats, false);
            int size = tLRPC$messages_Messages.messages.size();
            int i3 = 0;
            for (int i4 = 0; i4 < size; i4++) {
                MessageObject messageObject = new MessageObject(i2, tLRPC$messages_Messages.messages.get(i4), false, true);
                if (!this.playlistMap.containsKey(Integer.valueOf(messageObject.getId()))) {
                    this.playlist.add(0, messageObject);
                    this.playlistMap.put(Integer.valueOf(messageObject.getId()), messageObject);
                    i3++;
                }
            }
            sortPlaylist();
            this.loadingPlaylist = false;
            this.playlistGlobalSearchParams.endReached = this.playlist.size() == this.playlistGlobalSearchParams.totalCount;
            if (SharedConfig.shuffleMusic) {
                buildShuffledPlayList();
            }
            if (i3 != 0) {
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.moreMusicDidLoad, Integer.valueOf(i3));
            }
        }
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j, PlaylistGlobalSearchParams playlistGlobalSearchParams) {
        return setPlaylist(arrayList, messageObject, j, true, playlistGlobalSearchParams);
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j) {
        return setPlaylist(arrayList, messageObject, j, true, null);
    }

    public boolean setPlaylist(ArrayList<MessageObject> arrayList, MessageObject messageObject, long j, boolean z, PlaylistGlobalSearchParams playlistGlobalSearchParams) {
        if (this.playingMessageObject == messageObject) {
            int indexOf = this.playlist.indexOf(messageObject);
            if (indexOf >= 0) {
                this.currentPlaylistNum = indexOf;
            }
            return playMessage(messageObject);
        }
        this.forceLoopCurrentPlaylist = !z;
        this.playlistMergeDialogId = j;
        this.playMusicAgain = !this.playlist.isEmpty();
        clearPlaylist();
        this.playlistGlobalSearchParams = playlistGlobalSearchParams;
        boolean z2 = false;
        if (!arrayList.isEmpty() && DialogObject.isEncryptedDialog(arrayList.get(0).getDialogId())) {
            z2 = true;
        }
        int i = ConnectionsManager.DEFAULT_DATACENTER_ID;
        int i2 = Integer.MIN_VALUE;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            MessageObject messageObject2 = arrayList.get(size);
            if (messageObject2.isMusic()) {
                int id = messageObject2.getId();
                if (id > 0 || z2) {
                    i = Math.min(i, id);
                    i2 = Math.max(i2, id);
                }
                this.playlist.add(messageObject2);
                this.playlistMap.put(Integer.valueOf(id), messageObject2);
            }
        }
        sortPlaylist();
        int indexOf2 = this.playlist.indexOf(messageObject);
        this.currentPlaylistNum = indexOf2;
        if (indexOf2 == -1) {
            clearPlaylist();
            this.currentPlaylistNum = this.playlist.size();
            this.playlist.add(messageObject);
            this.playlistMap.put(Integer.valueOf(messageObject.getId()), messageObject);
        }
        if (messageObject.isMusic() && !messageObject.scheduled) {
            if (SharedConfig.shuffleMusic) {
                buildShuffledPlayList();
            }
            if (z) {
                if (this.playlistGlobalSearchParams == null) {
                    MediaDataController.getInstance(messageObject.currentAccount).loadMusic(messageObject.getDialogId(), i, i2);
                } else {
                    this.playlistClassGuid = ConnectionsManager.generateClassGuid();
                }
            }
        }
        return playMessage(messageObject);
    }

    private void sortPlaylist() {
        Collections.sort(this.playlist, MediaController$$ExternalSyntheticLambda41.INSTANCE);
    }

    public static int lambda$sortPlaylist$13(MessageObject messageObject, MessageObject messageObject2) {
        int id = messageObject.getId();
        int id2 = messageObject2.getId();
        long j = messageObject.messageOwner.grouped_id;
        long j2 = messageObject2.messageOwner.grouped_id;
        if (id >= 0 || id2 >= 0) {
            if (j == 0 || j != j2) {
                return zzdp$$ExternalSyntheticBackport0.m(id, id2);
            }
            return zzdp$$ExternalSyntheticBackport0.m(id2, id);
        } else if (j == 0 || j != j2) {
            return zzdp$$ExternalSyntheticBackport0.m(id2, id);
        } else {
            return zzdp$$ExternalSyntheticBackport0.m(id, id2);
        }
    }

    public void playNextMessage() {
        playNextMessageWithoutOrder(false);
    }

    public boolean findMessageInPlaylistAndPlay(MessageObject messageObject) {
        int indexOf = this.playlist.indexOf(messageObject);
        if (indexOf == -1) {
            return playMessage(messageObject);
        }
        playMessageAtIndex(indexOf);
        return true;
    }

    public void playMessageAtIndex(int i) {
        int i2 = this.currentPlaylistNum;
        if (i2 >= 0 && i2 < this.playlist.size()) {
            this.currentPlaylistNum = i;
            this.playMusicAgain = true;
            MessageObject messageObject = this.playlist.get(i);
            if (this.playingMessageObject != null && !isSamePlayingMessage(messageObject)) {
                this.playingMessageObject.resetPlayingProgress();
            }
            playMessage(messageObject);
        }
    }

    public void playNextMessageWithoutOrder(boolean z) {
        boolean z2;
        int i;
        ArrayList<MessageObject> arrayList = SharedConfig.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        if (!z || (!((i = SharedConfig.repeatMode) == 2 || (i == 1 && arrayList.size() == 1)) || this.forceLoopCurrentPlaylist)) {
            if (SharedConfig.playOrderReversed) {
                int i2 = this.currentPlaylistNum + 1;
                this.currentPlaylistNum = i2;
                if (i2 >= arrayList.size()) {
                    this.currentPlaylistNum = 0;
                    z2 = true;
                }
                z2 = false;
            } else {
                int i3 = this.currentPlaylistNum - 1;
                this.currentPlaylistNum = i3;
                if (i3 < 0) {
                    this.currentPlaylistNum = arrayList.size() - 1;
                    z2 = true;
                }
                z2 = false;
            }
            if (!z2 || !z || SharedConfig.repeatMode != 0 || this.forceLoopCurrentPlaylist) {
                int i4 = this.currentPlaylistNum;
                if (i4 >= 0 && i4 < arrayList.size()) {
                    MessageObject messageObject = this.playingMessageObject;
                    if (messageObject != null) {
                        messageObject.resetPlayingProgress();
                    }
                    this.playMusicAgain = true;
                    playMessage(arrayList.get(this.currentPlaylistNum));
                    return;
                }
                return;
            }
            VideoPlayer videoPlayer = this.audioPlayer;
            if (videoPlayer != null || this.videoPlayer != null) {
                if (videoPlayer != null) {
                    try {
                        videoPlayer.releasePlayer(true);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    this.audioPlayer = null;
                    Theme.unrefAudioVisualizeDrawable(this.playingMessageObject);
                } else {
                    this.currentAspectRatioFrameLayout = null;
                    this.currentTextureViewContainer = null;
                    this.currentAspectRatioFrameLayoutReady = false;
                    this.currentTextureView = null;
                    this.videoPlayer.releasePlayer(true);
                    this.videoPlayer = null;
                    try {
                        this.baseActivity.getWindow().clearFlags(ConnectionsManager.RequestFlagNeedQuickAck);
                    } catch (Exception e2) {
                        FileLog.e(e2);
                    }
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(this.playingMessageObject.currentAccount).removeLoadingVideo(this.playingMessageObject.getDocument(), true, false);
                }
                stopProgressTimer();
                this.lastProgress = 0L;
                this.isPaused = true;
                MessageObject messageObject2 = this.playingMessageObject;
                messageObject2.audioProgress = 0.0f;
                messageObject2.audioProgressSec = 0;
                NotificationCenter.getInstance(messageObject2.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                return;
            }
            return;
        }
        cleanupPlayer(false, false);
        MessageObject messageObject3 = arrayList.get(this.currentPlaylistNum);
        messageObject3.audioProgress = 0.0f;
        messageObject3.audioProgressSec = 0;
        playMessage(messageObject3);
    }

    public void playPreviousMessage() {
        int i;
        ArrayList<MessageObject> arrayList = SharedConfig.shuffleMusic ? this.shuffledPlaylist : this.playlist;
        if (!arrayList.isEmpty() && (i = this.currentPlaylistNum) >= 0 && i < arrayList.size()) {
            MessageObject messageObject = arrayList.get(this.currentPlaylistNum);
            if (messageObject.audioProgressSec > 10) {
                seekToProgress(messageObject, 0.0f);
                return;
            }
            if (SharedConfig.playOrderReversed) {
                int i2 = this.currentPlaylistNum - 1;
                this.currentPlaylistNum = i2;
                if (i2 < 0) {
                    this.currentPlaylistNum = arrayList.size() - 1;
                }
            } else {
                int i3 = this.currentPlaylistNum + 1;
                this.currentPlaylistNum = i3;
                if (i3 >= arrayList.size()) {
                    this.currentPlaylistNum = 0;
                }
            }
            if (this.currentPlaylistNum < arrayList.size()) {
                this.playMusicAgain = true;
                playMessage(arrayList.get(this.currentPlaylistNum));
            }
        }
    }

    public void checkIsNextMediaFileDownloaded() {
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null && messageObject.isMusic()) {
            checkIsNextMusicFileDownloaded(this.playingMessageObject.currentAccount);
        }
    }

    private void checkIsNextVoiceFileDownloaded(int i) {
        ArrayList<MessageObject> arrayList = this.voiceMessagesPlaylist;
        if (arrayList != null && arrayList.size() >= 2) {
            MessageObject messageObject = this.voiceMessagesPlaylist.get(1);
            String str = messageObject.messageOwner.attachPath;
            File file = null;
            if (str != null && str.length() > 0) {
                File file2 = new File(messageObject.messageOwner.attachPath);
                if (file2.exists()) {
                    file = file2;
                }
            }
            File pathToMessage = file != null ? file : FileLoader.getInstance(i).getPathToMessage(messageObject.messageOwner);
            pathToMessage.exists();
            if (pathToMessage != file && !pathToMessage.exists()) {
                FileLoader.getInstance(i).loadFile(messageObject.getDocument(), messageObject, 0, 0);
            }
        }
    }

    private void checkIsNextMusicFileDownloaded(int i) {
        int i2;
        if (DownloadController.getInstance(i).canDownloadNextTrack()) {
            ArrayList<MessageObject> arrayList = SharedConfig.shuffleMusic ? this.shuffledPlaylist : this.playlist;
            if (arrayList != null && arrayList.size() >= 2) {
                if (SharedConfig.playOrderReversed) {
                    i2 = this.currentPlaylistNum + 1;
                    if (i2 >= arrayList.size()) {
                        i2 = 0;
                    }
                } else {
                    i2 = this.currentPlaylistNum - 1;
                    if (i2 < 0) {
                        i2 = arrayList.size() - 1;
                    }
                }
                if (i2 >= 0 && i2 < arrayList.size()) {
                    MessageObject messageObject = arrayList.get(i2);
                    File file = null;
                    if (!TextUtils.isEmpty(messageObject.messageOwner.attachPath)) {
                        File file2 = new File(messageObject.messageOwner.attachPath);
                        if (file2.exists()) {
                            file = file2;
                        }
                    }
                    File pathToMessage = file != null ? file : FileLoader.getInstance(i).getPathToMessage(messageObject.messageOwner);
                    pathToMessage.exists();
                    if (pathToMessage != file && !pathToMessage.exists() && messageObject.isMusic()) {
                        FileLoader.getInstance(i).loadFile(messageObject.getDocument(), messageObject, 0, 0);
                    }
                }
            }
        }
    }

    public void setVoiceMessagesPlaylist(ArrayList<MessageObject> arrayList, boolean z) {
        this.voiceMessagesPlaylist = arrayList;
        if (arrayList != null) {
            this.voiceMessagesPlaylistUnread = z;
            this.voiceMessagesPlaylistMap = new SparseArray<>();
            for (int i = 0; i < this.voiceMessagesPlaylist.size(); i++) {
                MessageObject messageObject = this.voiceMessagesPlaylist.get(i);
                this.voiceMessagesPlaylistMap.put(messageObject.getId(), messageObject);
            }
        }
    }

    private void checkAudioFocus(MessageObject messageObject) {
        int i;
        int i2;
        if (messageObject.isVoice() || messageObject.isRoundVideo()) {
            i = this.useFrontSpeaker ? 3 : 2;
        } else {
            i = 1;
        }
        if (this.hasAudioFocus != i) {
            this.hasAudioFocus = i;
            if (i == 3) {
                i2 = NotificationsController.audioManager.requestAudioFocus(this, 0, 1);
            } else {
                i2 = NotificationsController.audioManager.requestAudioFocus(this, 3, i == 2 ? 3 : 1);
            }
            if (i2 == 1) {
                this.audioFocus = 2;
            }
        }
    }

    public void setCurrentVideoVisible(boolean z) {
        AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
        if (aspectRatioFrameLayout != null) {
            if (z) {
                PipRoundVideoView pipRoundVideoView = this.pipRoundVideoView;
                if (pipRoundVideoView != null) {
                    this.pipSwitchingState = 2;
                    pipRoundVideoView.close(true);
                    this.pipRoundVideoView = null;
                    return;
                }
                if (aspectRatioFrameLayout.getParent() == null) {
                    this.currentTextureViewContainer.addView(this.currentAspectRatioFrameLayout);
                }
                this.videoPlayer.setTextureView(this.currentTextureView);
            } else if (aspectRatioFrameLayout.getParent() != null) {
                this.pipSwitchingState = 1;
                this.currentTextureViewContainer.removeView(this.currentAspectRatioFrameLayout);
            } else {
                if (this.pipRoundVideoView == null) {
                    try {
                        PipRoundVideoView pipRoundVideoView2 = new PipRoundVideoView();
                        this.pipRoundVideoView = pipRoundVideoView2;
                        pipRoundVideoView2.show(this.baseActivity, new Runnable() {
                            @Override
                            public final void run() {
                                MediaController.this.lambda$setCurrentVideoVisible$14();
                            }
                        });
                    } catch (Exception unused) {
                        this.pipRoundVideoView = null;
                    }
                }
                PipRoundVideoView pipRoundVideoView3 = this.pipRoundVideoView;
                if (pipRoundVideoView3 != null) {
                    this.videoPlayer.setTextureView(pipRoundVideoView3.getTextureView());
                }
            }
        }
    }

    public void lambda$setCurrentVideoVisible$14() {
        cleanupPlayer(true, true);
    }

    public void setTextureView(TextureView textureView, AspectRatioFrameLayout aspectRatioFrameLayout, FrameLayout frameLayout, boolean z) {
        if (textureView != null) {
            boolean z2 = true;
            if (!z && this.currentTextureView == textureView) {
                this.pipSwitchingState = 1;
                this.currentTextureView = null;
                this.currentAspectRatioFrameLayout = null;
                this.currentTextureViewContainer = null;
            } else if (this.videoPlayer != null && textureView != this.currentTextureView) {
                if (aspectRatioFrameLayout == null || !aspectRatioFrameLayout.isDrawingReady()) {
                    z2 = false;
                }
                this.isDrawingWasReady = z2;
                this.currentTextureView = textureView;
                PipRoundVideoView pipRoundVideoView = this.pipRoundVideoView;
                if (pipRoundVideoView != null) {
                    this.videoPlayer.setTextureView(pipRoundVideoView.getTextureView());
                } else {
                    this.videoPlayer.setTextureView(textureView);
                }
                this.currentAspectRatioFrameLayout = aspectRatioFrameLayout;
                this.currentTextureViewContainer = frameLayout;
                if (this.currentAspectRatioFrameLayoutReady && aspectRatioFrameLayout != null) {
                    aspectRatioFrameLayout.setAspectRatio(this.currentAspectRatioFrameLayoutRatio, this.currentAspectRatioFrameLayoutRotation);
                }
            }
        }
    }

    public void setBaseActivity(Activity activity, boolean z) {
        if (z) {
            this.baseActivity = activity;
        } else if (this.baseActivity == activity) {
            this.baseActivity = null;
        }
    }

    public void setFeedbackView(View view, boolean z) {
        if (z) {
            this.feedbackView = view;
        } else if (this.feedbackView == view) {
            this.feedbackView = null;
        }
    }

    public void setPlaybackSpeed(boolean z, float f) {
        if (z) {
            if (this.currentMusicPlaybackSpeed >= 6.0f && f == VOLUME_NORMAL && this.playingMessageObject != null) {
                this.audioPlayer.pause();
                final MessageObject messageObject = this.playingMessageObject;
                final float f2 = messageObject.audioProgress;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.this.lambda$setPlaybackSpeed$15(messageObject, f2);
                    }
                }, 50L);
            }
            this.currentMusicPlaybackSpeed = f;
            if (Math.abs(f - VOLUME_NORMAL) > 0.001f) {
                this.fastMusicPlaybackSpeed = f;
            }
        } else {
            this.currentPlaybackSpeed = f;
            if (Math.abs(f - VOLUME_NORMAL) > 0.001f) {
                this.fastPlaybackSpeed = f;
            }
        }
        VideoPlayer videoPlayer = this.audioPlayer;
        if (videoPlayer != null) {
            videoPlayer.setPlaybackSpeed(f);
        } else {
            VideoPlayer videoPlayer2 = this.videoPlayer;
            if (videoPlayer2 != null) {
                videoPlayer2.setPlaybackSpeed(f);
            }
        }
        MessagesController.getGlobalMainSettings().edit().putFloat(z ? "musicPlaybackSpeed" : "playbackSpeed", f).putFloat(z ? "fastMusicPlaybackSpeed" : "fastPlaybackSpeed", z ? this.fastMusicPlaybackSpeed : this.fastPlaybackSpeed).commit();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.messagePlayingSpeedChanged, new Object[0]);
    }

    public void lambda$setPlaybackSpeed$15(MessageObject messageObject, float f) {
        if (this.audioPlayer != null && this.playingMessageObject != null && !this.isPaused) {
            if (isSamePlayingMessage(messageObject)) {
                seekToProgress(this.playingMessageObject, f);
            }
            this.audioPlayer.play();
        }
    }

    public float getPlaybackSpeed(boolean z) {
        return z ? this.currentMusicPlaybackSpeed : this.currentPlaybackSpeed;
    }

    public float getFastPlaybackSpeed(boolean z) {
        return z ? this.fastMusicPlaybackSpeed : this.fastPlaybackSpeed;
    }

    public void updateVideoState(MessageObject messageObject, int[] iArr, boolean z, boolean z2, int i) {
        MessageObject messageObject2;
        if (this.videoPlayer != null) {
            if (i == 4 || i == 1) {
                try {
                    this.baseActivity.getWindow().clearFlags(ConnectionsManager.RequestFlagNeedQuickAck);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else {
                try {
                    this.baseActivity.getWindow().addFlags(ConnectionsManager.RequestFlagNeedQuickAck);
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
            }
            if (i == 3) {
                this.playerWasReady = true;
                MessageObject messageObject3 = this.playingMessageObject;
                if (messageObject3 != null && (messageObject3.isVideo() || this.playingMessageObject.isRoundVideo())) {
                    AndroidUtilities.cancelRunOnUIThread(this.setLoadingRunnable);
                    FileLoader.getInstance(messageObject.currentAccount).removeLoadingVideo(this.playingMessageObject.getDocument(), true, false);
                }
                this.currentAspectRatioFrameLayoutReady = true;
            } else if (i == 2) {
                if (z2 && (messageObject2 = this.playingMessageObject) != null) {
                    if (!messageObject2.isVideo() && !this.playingMessageObject.isRoundVideo()) {
                        return;
                    }
                    if (this.playerWasReady) {
                        this.setLoadingRunnable.run();
                    } else {
                        AndroidUtilities.runOnUIThread(this.setLoadingRunnable, 1000L);
                    }
                }
            } else if (this.videoPlayer.isPlaying() && i == 4) {
                if (!this.playingMessageObject.isVideo() || z || (iArr != null && iArr[0] >= 4)) {
                    cleanupPlayer(true, true, true, false);
                    return;
                }
                this.videoPlayer.seekTo(0L);
                if (iArr != null) {
                    iArr[0] = iArr[0] + 1;
                }
            }
        }
    }

    public void injectVideoPlayer(VideoPlayer videoPlayer, MessageObject messageObject) {
        if (videoPlayer != null && messageObject != null) {
            FileLoader.getInstance(messageObject.currentAccount).setLoadingVideoForPlayer(messageObject.getDocument(), true);
            this.playerWasReady = false;
            clearPlaylist();
            this.videoPlayer = videoPlayer;
            this.playingMessageObject = messageObject;
            int i = this.playerNum + 1;
            this.playerNum = i;
            videoPlayer.setDelegate(new AnonymousClass7(i, messageObject, null, true));
            this.currentAspectRatioFrameLayoutReady = false;
            TextureView textureView = this.currentTextureView;
            if (textureView != null) {
                this.videoPlayer.setTextureView(textureView);
            }
            checkAudioFocus(messageObject);
            setPlayerVolume();
            this.isPaused = false;
            this.lastProgress = 0L;
            this.playingMessageObject = messageObject;
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            startProgressTimer(this.playingMessageObject);
            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidStart, messageObject);
        }
    }

    public class AnonymousClass7 implements VideoPlayer.VideoPlayerDelegate {
        final boolean val$destroyAtEnd;
        final MessageObject val$messageObject;
        final int[] val$playCount;
        final int val$tag;

        @Override
        public void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
        }

        @Override
        public void onSeekFinished(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
        }

        @Override
        public void onSeekStarted(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        AnonymousClass7(int i, MessageObject messageObject, int[] iArr, boolean z) {
            this.val$tag = i;
            this.val$messageObject = messageObject;
            this.val$playCount = iArr;
            this.val$destroyAtEnd = z;
        }

        @Override
        public void onStateChanged(boolean z, int i) {
            if (this.val$tag == MediaController.this.playerNum) {
                MediaController.this.updateVideoState(this.val$messageObject, this.val$playCount, this.val$destroyAtEnd, z, i);
            }
        }

        @Override
        public void onError(VideoPlayer videoPlayer, Exception exc) {
            FileLog.e(exc);
        }

        @Override
        public void onVideoSizeChanged(int i, int i2, int i3, float f) {
            MediaController.this.currentAspectRatioFrameLayoutRotation = i3;
            if (!(i3 == 90 || i3 == 270)) {
                i2 = i;
                i = i2;
            }
            MediaController.this.currentAspectRatioFrameLayoutRatio = i == 0 ? MediaController.VOLUME_NORMAL : (i2 * f) / i;
            if (MediaController.this.currentAspectRatioFrameLayout != null) {
                MediaController.this.currentAspectRatioFrameLayout.setAspectRatio(MediaController.this.currentAspectRatioFrameLayoutRatio, MediaController.this.currentAspectRatioFrameLayoutRotation);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (MediaController.this.currentAspectRatioFrameLayout != null && !MediaController.this.currentAspectRatioFrameLayout.isDrawingReady()) {
                MediaController.this.isDrawingWasReady = true;
                MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                MediaController.this.currentTextureViewContainer.setTag(1);
            }
        }

        @Override
        public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
            if (MediaController.this.videoPlayer == null) {
                return false;
            }
            if (MediaController.this.pipSwitchingState == 2) {
                if (MediaController.this.currentAspectRatioFrameLayout != null) {
                    if (MediaController.this.isDrawingWasReady) {
                        MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                    }
                    if (MediaController.this.currentAspectRatioFrameLayout.getParent() == null) {
                        MediaController.this.currentTextureViewContainer.addView(MediaController.this.currentAspectRatioFrameLayout);
                    }
                    if (MediaController.this.currentTextureView.getSurfaceTexture() != surfaceTexture) {
                        MediaController.this.currentTextureView.setSurfaceTexture(surfaceTexture);
                    }
                    MediaController.this.videoPlayer.setTextureView(MediaController.this.currentTextureView);
                }
                MediaController.this.pipSwitchingState = 0;
                return true;
            } else if (MediaController.this.pipSwitchingState == 1) {
                if (MediaController.this.baseActivity != null) {
                    if (MediaController.this.pipRoundVideoView == null) {
                        try {
                            MediaController.this.pipRoundVideoView = new PipRoundVideoView();
                            MediaController.this.pipRoundVideoView.show(MediaController.this.baseActivity, new Runnable() {
                                @Override
                                public final void run() {
                                    MediaController.AnonymousClass7.this.lambda$onSurfaceDestroyed$0();
                                }
                            });
                        } catch (Exception unused) {
                            MediaController.this.pipRoundVideoView = null;
                        }
                    }
                    if (MediaController.this.pipRoundVideoView != null) {
                        if (MediaController.this.pipRoundVideoView.getTextureView().getSurfaceTexture() != surfaceTexture) {
                            MediaController.this.pipRoundVideoView.getTextureView().setSurfaceTexture(surfaceTexture);
                        }
                        MediaController.this.videoPlayer.setTextureView(MediaController.this.pipRoundVideoView.getTextureView());
                    }
                }
                MediaController.this.pipSwitchingState = 0;
                return true;
            } else if (!PhotoViewer.hasInstance() || !PhotoViewer.getInstance().isInjectingVideoPlayer()) {
                return false;
            } else {
                PhotoViewer.getInstance().injectVideoPlayerSurface(surfaceTexture);
                return true;
            }
        }

        public void lambda$onSurfaceDestroyed$0() {
            MediaController.this.cleanupPlayer(true, true);
        }
    }

    public void playEmojiSound(final AccountInstance accountInstance, String str, final MessagesController.EmojiSound emojiSound, final boolean z) {
        if (emojiSound != null) {
            Utilities.stageQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$playEmojiSound$18(emojiSound, accountInstance, z);
                }
            });
        }
    }

    public void lambda$playEmojiSound$18(MessagesController.EmojiSound emojiSound, final AccountInstance accountInstance, boolean z) {
        final TLRPC$TL_document tLRPC$TL_document = new TLRPC$TL_document();
        tLRPC$TL_document.access_hash = emojiSound.accessHash;
        tLRPC$TL_document.id = emojiSound.id;
        tLRPC$TL_document.mime_type = "sound/ogg";
        tLRPC$TL_document.file_reference = emojiSound.fileReference;
        tLRPC$TL_document.dc_id = accountInstance.getConnectionsManager().getCurrentDatacenterId();
        final File pathToAttach = FileLoader.getInstance(accountInstance.getCurrentAccount()).getPathToAttach(tLRPC$TL_document, true);
        if (!pathToAttach.exists()) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.lambda$playEmojiSound$17(AccountInstance.this, tLRPC$TL_document);
                }
            });
        } else if (!z) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$playEmojiSound$16(pathToAttach);
                }
            });
        }
    }

    public void lambda$playEmojiSound$16(File file) {
        try {
            int i = this.emojiSoundPlayerNum + 1;
            this.emojiSoundPlayerNum = i;
            VideoPlayer videoPlayer = this.emojiSoundPlayer;
            if (videoPlayer != null) {
                videoPlayer.releasePlayer(true);
            }
            VideoPlayer videoPlayer2 = new VideoPlayer(false);
            this.emojiSoundPlayer = videoPlayer2;
            videoPlayer2.setDelegate(new AnonymousClass8(i));
            this.emojiSoundPlayer.preparePlayer(Uri.fromFile(file), "other");
            this.emojiSoundPlayer.setStreamType(3);
            this.emojiSoundPlayer.play();
        } catch (Exception e) {
            FileLog.e(e);
            VideoPlayer videoPlayer3 = this.emojiSoundPlayer;
            if (videoPlayer3 != null) {
                videoPlayer3.releasePlayer(true);
                this.emojiSoundPlayer = null;
            }
        }
    }

    public class AnonymousClass8 implements VideoPlayer.VideoPlayerDelegate {
        final int val$tag;

        @Override
        public void onError(VideoPlayer videoPlayer, Exception exc) {
        }

        @Override
        public void onRenderedFirstFrame() {
        }

        @Override
        public void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
        }

        @Override
        public void onSeekFinished(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
        }

        @Override
        public void onSeekStarted(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
        }

        @Override
        public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        @Override
        public void onVideoSizeChanged(int i, int i2, int i3, float f) {
        }

        AnonymousClass8(int i) {
            this.val$tag = i;
        }

        @Override
        public void onStateChanged(boolean z, final int i) {
            final int i2 = this.val$tag;
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.AnonymousClass8.this.lambda$onStateChanged$0(i2, i);
                }
            });
        }

        public void lambda$onStateChanged$0(int i, int i2) {
            if (i == MediaController.this.emojiSoundPlayerNum && i2 == 4 && MediaController.this.emojiSoundPlayer != null) {
                try {
                    MediaController.this.emojiSoundPlayer.releasePlayer(true);
                    MediaController.this.emojiSoundPlayer = null;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
    }

    public static void lambda$playEmojiSound$17(AccountInstance accountInstance, TLRPC$Document tLRPC$Document) {
        accountInstance.getFileLoader().loadFile(tLRPC$Document, null, 1, 1);
    }

    public void checkVolumeBarUI() {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (Math.abs(currentTimeMillis - volumeBarLastTimeShown) >= 5000) {
                AudioManager audioManager = (AudioManager) ApplicationLoader.applicationContext.getSystemService(MediaStreamTrack.AUDIO_TRACK_KIND);
                int i = this.useFrontSpeaker ? 0 : 3;
                int streamVolume = audioManager.getStreamVolume(i);
                if (streamVolume == 0) {
                    audioManager.adjustStreamVolume(i, streamVolume, 1);
                    volumeBarLastTimeShown = currentTimeMillis;
                }
            }
        } catch (Exception unused) {
        }
    }

    public boolean playMessage(final MessageObject messageObject) {
        boolean z;
        File file;
        float f;
        PowerManager.WakeLock wakeLock;
        if (messageObject == null) {
            return false;
        }
        checkVolumeBarUI();
        if (!(this.audioPlayer == null && this.videoPlayer == null) && isSamePlayingMessage(messageObject)) {
            if (this.isPaused) {
                resumeAudio(messageObject);
            }
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            return true;
        }
        if (!messageObject.isOut() && messageObject.isContentUnread()) {
            MessagesController.getInstance(messageObject.currentAccount).markMessageContentAsRead(messageObject);
        }
        boolean z2 = this.playMusicAgain;
        boolean z3 = !z2;
        MessageObject messageObject2 = this.playingMessageObject;
        if (messageObject2 != null) {
            if (!z2) {
                messageObject2.resetPlayingProgress();
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
            }
            z3 = false;
        }
        cleanupPlayer(z3, false);
        this.shouldSavePositionForCurrentAudio = null;
        this.lastSaveTime = 0L;
        this.playMusicAgain = false;
        this.seekToProgressPending = 0.0f;
        String str = messageObject.messageOwner.attachPath;
        if (str == null || str.length() <= 0) {
            file = null;
            z = false;
        } else {
            file = new File(messageObject.messageOwner.attachPath);
            z = file.exists();
            if (!z) {
                file = null;
            }
        }
        final File pathToMessage = file != null ? file : FileLoader.getInstance(messageObject.currentAccount).getPathToMessage(messageObject.messageOwner);
        boolean z4 = SharedConfig.streamMedia && (messageObject.isMusic() || messageObject.isRoundVideo() || (messageObject.isVideo() && messageObject.canStreamVideo())) && !DialogObject.isEncryptedDialog(messageObject.getDialogId());
        if (pathToMessage == file || (z = pathToMessage.exists()) || z4) {
            boolean z5 = z;
            this.downloadingCurrentMessage = false;
            if (messageObject.isMusic()) {
                checkIsNextMusicFileDownloaded(messageObject.currentAccount);
            } else {
                checkIsNextVoiceFileDownloaded(messageObject.currentAccount);
            }
            AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
            if (aspectRatioFrameLayout != null) {
                this.isDrawingWasReady = false;
                aspectRatioFrameLayout.setDrawingReady(false);
            }
            boolean isVideo = messageObject.isVideo();
            if (messageObject.isRoundVideo() || isVideo) {
                FileLoader.getInstance(messageObject.currentAccount).setLoadingVideoForPlayer(messageObject.getDocument(), true);
                this.playerWasReady = false;
                boolean z6 = !isVideo || (messageObject.messageOwner.peer_id.channel_id == 0 && messageObject.audioProgress <= 0.1f);
                int[] iArr = (!isVideo || messageObject.getDuration() > 30) ? null : new int[]{1};
                clearPlaylist();
                VideoPlayer videoPlayer = new VideoPlayer();
                this.videoPlayer = videoPlayer;
                int i = this.playerNum + 1;
                this.playerNum = i;
                final File file2 = pathToMessage;
                videoPlayer.setDelegate(new AnonymousClass9(i, messageObject, iArr, z6));
                this.currentAspectRatioFrameLayoutReady = false;
                if (this.pipRoundVideoView != null || !MessagesController.getInstance(messageObject.currentAccount).isDialogVisible(messageObject.getDialogId(), messageObject.scheduled)) {
                    if (this.pipRoundVideoView == null) {
                        try {
                            PipRoundVideoView pipRoundVideoView = new PipRoundVideoView();
                            this.pipRoundVideoView = pipRoundVideoView;
                            pipRoundVideoView.show(this.baseActivity, new Runnable() {
                                @Override
                                public final void run() {
                                    MediaController.this.lambda$playMessage$19();
                                }
                            });
                        } catch (Exception unused) {
                            this.pipRoundVideoView = null;
                        }
                    }
                    PipRoundVideoView pipRoundVideoView2 = this.pipRoundVideoView;
                    if (pipRoundVideoView2 != null) {
                        this.videoPlayer.setTextureView(pipRoundVideoView2.getTextureView());
                    }
                } else {
                    TextureView textureView = this.currentTextureView;
                    if (textureView != null) {
                        this.videoPlayer.setTextureView(textureView);
                    }
                }
                if (z5) {
                    if (!messageObject.mediaExists && file2 != file) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public final void run() {
                                MediaController.lambda$playMessage$20(MessageObject.this, file2);
                            }
                        });
                    }
                    this.videoPlayer.preparePlayer(Uri.fromFile(file2), "other");
                } else {
                    try {
                        int fileReference = FileLoader.getInstance(messageObject.currentAccount).getFileReference(messageObject);
                        TLRPC$Document document = messageObject.getDocument();
                        StringBuilder sb = new StringBuilder();
                        sb.append("?account=");
                        sb.append(messageObject.currentAccount);
                        sb.append("&id=");
                        sb.append(document.id);
                        sb.append("&hash=");
                        sb.append(document.access_hash);
                        sb.append("&dc=");
                        sb.append(document.dc_id);
                        sb.append("&size=");
                        sb.append(document.size);
                        sb.append("&mime=");
                        sb.append(URLEncoder.encode(document.mime_type, "UTF-8"));
                        sb.append("&rid=");
                        sb.append(fileReference);
                        sb.append("&name=");
                        sb.append(URLEncoder.encode(FileLoader.getDocumentFileName(document), "UTF-8"));
                        sb.append("&reference=");
                        byte[] bArr = document.file_reference;
                        if (bArr == null) {
                            bArr = new byte[0];
                        }
                        sb.append(Utilities.bytesToHex(bArr));
                        this.videoPlayer.preparePlayer(Uri.parse("tg://" + messageObject.getFileName() + sb.toString()), "other");
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                if (messageObject.isRoundVideo()) {
                    this.videoPlayer.setStreamType(this.useFrontSpeaker ? 0 : 3);
                    if (Math.abs(this.currentPlaybackSpeed - VOLUME_NORMAL) > 0.001f) {
                        this.videoPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
                    }
                    float f2 = messageObject.forceSeekTo;
                    f = 0.0f;
                    if (f2 >= 0.0f) {
                        this.seekToProgressPending = f2;
                        messageObject.audioProgress = f2;
                        messageObject.forceSeekTo = -1.0f;
                    }
                } else {
                    f = 0.0f;
                    this.videoPlayer.setStreamType(3);
                }
            } else {
                PipRoundVideoView pipRoundVideoView3 = this.pipRoundVideoView;
                if (pipRoundVideoView3 != null) {
                    pipRoundVideoView3.close(true);
                    this.pipRoundVideoView = null;
                }
                try {
                    VideoPlayer videoPlayer2 = new VideoPlayer();
                    this.audioPlayer = videoPlayer2;
                    final int i2 = this.playerNum + 1;
                    this.playerNum = i2;
                    videoPlayer2.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
                        @Override
                        public void onError(VideoPlayer videoPlayer3, Exception exc) {
                        }

                        @Override
                        public void onRenderedFirstFrame() {
                        }

                        @Override
                        public void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
                        }

                        @Override
                        public void onSeekFinished(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
                        }

                        @Override
                        public void onSeekStarted(AnalyticsListener.EventTime eventTime) {
                            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
                        }

                        @Override
                        public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                            return false;
                        }

                        @Override
                        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                        }

                        @Override
                        public void onVideoSizeChanged(int i3, int i4, int i5, float f3) {
                        }

                        @Override
                        public void onStateChanged(boolean z7, int i3) {
                            if (i2 == MediaController.this.playerNum) {
                                if (i3 == 4 || ((i3 == 1 || i3 == 2) && z7 && messageObject.audioProgress >= 0.999f)) {
                                    MessageObject messageObject3 = messageObject;
                                    messageObject3.audioProgress = MediaController.VOLUME_NORMAL;
                                    NotificationCenter.getInstance(messageObject3.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(messageObject.getId()), 0);
                                    if (MediaController.this.playlist.isEmpty() || (MediaController.this.playlist.size() <= 1 && messageObject.isVoice())) {
                                        MediaController.this.cleanupPlayer(true, true, messageObject.isVoice(), false);
                                    } else {
                                        MediaController.this.playNextMessageWithoutOrder(true);
                                    }
                                } else if (MediaController.this.audioPlayer != null && MediaController.this.seekToProgressPending != 0.0f) {
                                    if (i3 == 3 || i3 == 1) {
                                        long duration = (int) (((float) MediaController.this.audioPlayer.getDuration()) * MediaController.this.seekToProgressPending);
                                        MediaController.this.audioPlayer.seekTo(duration);
                                        MediaController.this.lastProgress = duration;
                                        MediaController.this.seekToProgressPending = 0.0f;
                                    }
                                }
                            }
                        }
                    });
                    this.audioPlayer.setAudioVisualizerDelegate(new VideoPlayer.AudioVisualizerDelegate() {
                        @Override
                        public void onVisualizerUpdate(boolean z7, boolean z8, float[] fArr) {
                            Theme.getCurrentAudiVisualizerDrawable().setWaveform(z7, z8, fArr);
                        }

                        @Override
                        public boolean needUpdate() {
                            return Theme.getCurrentAudiVisualizerDrawable().getParentView() != null;
                        }
                    });
                    if (z5) {
                        if (!messageObject.mediaExists && pathToMessage != file) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public final void run() {
                                    MediaController.lambda$playMessage$21(MessageObject.this, pathToMessage);
                                }
                            });
                        }
                        this.audioPlayer.preparePlayer(Uri.fromFile(pathToMessage), "other");
                        this.isStreamingCurrentAudio = false;
                    } else {
                        int fileReference2 = FileLoader.getInstance(messageObject.currentAccount).getFileReference(messageObject);
                        TLRPC$Document document2 = messageObject.getDocument();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("?account=");
                        sb2.append(messageObject.currentAccount);
                        sb2.append("&id=");
                        sb2.append(document2.id);
                        sb2.append("&hash=");
                        sb2.append(document2.access_hash);
                        sb2.append("&dc=");
                        sb2.append(document2.dc_id);
                        sb2.append("&size=");
                        sb2.append(document2.size);
                        sb2.append("&mime=");
                        sb2.append(URLEncoder.encode(document2.mime_type, "UTF-8"));
                        sb2.append("&rid=");
                        sb2.append(fileReference2);
                        sb2.append("&name=");
                        sb2.append(URLEncoder.encode(FileLoader.getDocumentFileName(document2), "UTF-8"));
                        sb2.append("&reference=");
                        byte[] bArr2 = document2.file_reference;
                        if (bArr2 == null) {
                            bArr2 = new byte[0];
                        }
                        sb2.append(Utilities.bytesToHex(bArr2));
                        this.audioPlayer.preparePlayer(Uri.parse("tg://" + messageObject.getFileName() + sb2.toString()), "other");
                        this.isStreamingCurrentAudio = true;
                    }
                    if (messageObject.isVoice()) {
                        String fileName = messageObject.getFileName();
                        if (fileName != null && messageObject.getDuration() >= 300) {
                            float f3 = ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).getFloat(fileName, -1.0f);
                            if (f3 > 0.0f && f3 < 0.99f) {
                                this.seekToProgressPending = f3;
                                messageObject.audioProgress = f3;
                            }
                            this.shouldSavePositionForCurrentAudio = fileName;
                        }
                        if (Math.abs(this.currentPlaybackSpeed - VOLUME_NORMAL) > 0.001f) {
                            this.audioPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
                        }
                        this.audioInfo = null;
                        clearPlaylist();
                    } else {
                        try {
                            this.audioInfo = AudioInfo.getAudioInfo(pathToMessage);
                        } catch (Exception e2) {
                            FileLog.e(e2);
                        }
                        String fileName2 = messageObject.getFileName();
                        if (!TextUtils.isEmpty(fileName2) && messageObject.getDuration() >= 600) {
                            float f4 = ApplicationLoader.applicationContext.getSharedPreferences("media_saved_pos", 0).getFloat(fileName2, -1.0f);
                            if (f4 > 0.0f && f4 < 0.999f) {
                                this.seekToProgressPending = f4;
                                messageObject.audioProgress = f4;
                            }
                            this.shouldSavePositionForCurrentAudio = fileName2;
                            if (Math.abs(this.currentMusicPlaybackSpeed - VOLUME_NORMAL) > 0.001f) {
                                this.audioPlayer.setPlaybackSpeed(this.currentMusicPlaybackSpeed);
                            }
                        }
                    }
                    float f5 = messageObject.forceSeekTo;
                    if (f5 >= 0.0f) {
                        this.seekToProgressPending = f5;
                        messageObject.audioProgress = f5;
                        messageObject.forceSeekTo = -1.0f;
                    }
                    this.audioPlayer.setStreamType(this.useFrontSpeaker ? 0 : 3);
                    this.audioPlayer.play();
                    if (!messageObject.isVoice()) {
                        ValueAnimator valueAnimator = this.audioVolumeAnimator;
                        if (valueAnimator != null) {
                            valueAnimator.removeAllListeners();
                            this.audioVolumeAnimator.cancel();
                        }
                        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.audioVolume, VOLUME_NORMAL);
                        this.audioVolumeAnimator = ofFloat;
                        ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                        this.audioVolumeAnimator.setDuration(300L);
                        this.audioVolumeAnimator.start();
                    } else {
                        this.audioVolume = VOLUME_NORMAL;
                        setPlayerVolume();
                    }
                    f = 0.0f;
                } catch (Exception e3) {
                    FileLog.e(e3);
                    NotificationCenter notificationCenter = NotificationCenter.getInstance(messageObject.currentAccount);
                    int i3 = NotificationCenter.messagePlayingPlayStateChanged;
                    Object[] objArr = new Object[1];
                    MessageObject messageObject3 = this.playingMessageObject;
                    objArr[0] = Integer.valueOf(messageObject3 != null ? messageObject3.getId() : 0);
                    notificationCenter.postNotificationName(i3, objArr);
                    VideoPlayer videoPlayer3 = this.audioPlayer;
                    if (videoPlayer3 != null) {
                        videoPlayer3.releasePlayer(true);
                        this.audioPlayer = null;
                        Theme.unrefAudioVisualizeDrawable(this.playingMessageObject);
                        this.isPaused = false;
                        this.playingMessageObject = null;
                        this.downloadingCurrentMessage = false;
                    }
                    return false;
                }
            }
            checkAudioFocus(messageObject);
            setPlayerVolume();
            this.isPaused = false;
            this.lastProgress = 0L;
            this.playingMessageObject = messageObject;
            if (!SharedConfig.raiseToSpeak) {
                startRaiseToEarSensors(this.raiseChat);
            }
            if (!ApplicationLoader.mainInterfacePaused && (wakeLock = this.proximityWakeLock) != null && !wakeLock.isHeld() && (this.playingMessageObject.isVoice() || this.playingMessageObject.isRoundVideo())) {
                this.proximityWakeLock.acquire();
            }
            startProgressTimer(this.playingMessageObject);
            NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingDidStart, messageObject);
            VideoPlayer videoPlayer4 = this.videoPlayer;
            if (videoPlayer4 != null) {
                try {
                    if (this.playingMessageObject.audioProgress != f) {
                        long duration = videoPlayer4.getDuration();
                        if (duration == -9223372036854775807L) {
                            duration = this.playingMessageObject.getDuration() * 1000;
                        }
                        MessageObject messageObject4 = this.playingMessageObject;
                        int i4 = (int) (((float) duration) * messageObject4.audioProgress);
                        int i5 = messageObject4.audioProgressMs;
                        if (i5 != 0) {
                            messageObject4.audioProgressMs = 0;
                            i4 = i5;
                        }
                        this.videoPlayer.seekTo(i4);
                    }
                } catch (Exception e4) {
                    MessageObject messageObject5 = this.playingMessageObject;
                    messageObject5.audioProgress = f;
                    messageObject5.audioProgressSec = 0;
                    NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
                    FileLog.e(e4);
                }
                this.videoPlayer.play();
            } else {
                VideoPlayer videoPlayer5 = this.audioPlayer;
                if (videoPlayer5 != null) {
                    try {
                        if (this.playingMessageObject.audioProgress != f) {
                            long duration2 = videoPlayer5.getDuration();
                            if (duration2 == -9223372036854775807L) {
                                duration2 = this.playingMessageObject.getDuration() * 1000;
                            }
                            this.audioPlayer.seekTo((int) (((float) duration2) * this.playingMessageObject.audioProgress));
                        }
                    } catch (Exception e5) {
                        this.playingMessageObject.resetPlayingProgress();
                        NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingProgressDidChanged, Integer.valueOf(this.playingMessageObject.getId()), 0);
                        FileLog.e(e5);
                    }
                }
            }
            MessageObject messageObject6 = this.playingMessageObject;
            if (messageObject6 == null || !messageObject6.isMusic()) {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                return true;
            }
            try {
                ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                return true;
            } catch (Throwable th) {
                FileLog.e(th);
                return true;
            }
        } else {
            FileLoader.getInstance(messageObject.currentAccount).loadFile(messageObject.getDocument(), messageObject, 0, 0);
            this.downloadingCurrentMessage = true;
            this.isPaused = false;
            this.lastProgress = 0L;
            this.audioInfo = null;
            this.playingMessageObject = messageObject;
            if (messageObject.isMusic()) {
                try {
                    ApplicationLoader.applicationContext.startService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
                } catch (Throwable th2) {
                    FileLog.e(th2);
                }
            } else {
                ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, MusicPlayerService.class));
            }
            NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
            return true;
        }
    }

    public class AnonymousClass9 implements VideoPlayer.VideoPlayerDelegate {
        final boolean val$destroyAtEnd;
        final MessageObject val$messageObject;
        final int[] val$playCount;
        final int val$tag;

        @Override
        public void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onRenderedFirstFrame(this, eventTime);
        }

        @Override
        public void onSeekFinished(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekFinished(this, eventTime);
        }

        @Override
        public void onSeekStarted(AnalyticsListener.EventTime eventTime) {
            VideoPlayer.VideoPlayerDelegate.CC.$default$onSeekStarted(this, eventTime);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        AnonymousClass9(int i, MessageObject messageObject, int[] iArr, boolean z) {
            this.val$tag = i;
            this.val$messageObject = messageObject;
            this.val$playCount = iArr;
            this.val$destroyAtEnd = z;
        }

        @Override
        public void onStateChanged(boolean z, int i) {
            if (this.val$tag == MediaController.this.playerNum) {
                MediaController.this.updateVideoState(this.val$messageObject, this.val$playCount, this.val$destroyAtEnd, z, i);
            }
        }

        @Override
        public void onError(VideoPlayer videoPlayer, Exception exc) {
            FileLog.e(exc);
        }

        @Override
        public void onVideoSizeChanged(int i, int i2, int i3, float f) {
            MediaController.this.currentAspectRatioFrameLayoutRotation = i3;
            if (!(i3 == 90 || i3 == 270)) {
                i2 = i;
                i = i2;
            }
            MediaController.this.currentAspectRatioFrameLayoutRatio = i == 0 ? MediaController.VOLUME_NORMAL : (i2 * f) / i;
            if (MediaController.this.currentAspectRatioFrameLayout != null) {
                MediaController.this.currentAspectRatioFrameLayout.setAspectRatio(MediaController.this.currentAspectRatioFrameLayoutRatio, MediaController.this.currentAspectRatioFrameLayoutRotation);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (MediaController.this.currentAspectRatioFrameLayout != null && !MediaController.this.currentAspectRatioFrameLayout.isDrawingReady()) {
                MediaController.this.isDrawingWasReady = true;
                MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                MediaController.this.currentTextureViewContainer.setTag(1);
            }
        }

        @Override
        public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
            if (MediaController.this.videoPlayer == null) {
                return false;
            }
            if (MediaController.this.pipSwitchingState == 2) {
                if (MediaController.this.currentAspectRatioFrameLayout != null) {
                    if (MediaController.this.isDrawingWasReady) {
                        MediaController.this.currentAspectRatioFrameLayout.setDrawingReady(true);
                    }
                    if (MediaController.this.currentAspectRatioFrameLayout.getParent() == null) {
                        MediaController.this.currentTextureViewContainer.addView(MediaController.this.currentAspectRatioFrameLayout);
                    }
                    if (MediaController.this.currentTextureView.getSurfaceTexture() != surfaceTexture) {
                        MediaController.this.currentTextureView.setSurfaceTexture(surfaceTexture);
                    }
                    MediaController.this.videoPlayer.setTextureView(MediaController.this.currentTextureView);
                }
                MediaController.this.pipSwitchingState = 0;
                return true;
            } else if (MediaController.this.pipSwitchingState == 1) {
                if (MediaController.this.baseActivity != null) {
                    if (MediaController.this.pipRoundVideoView == null) {
                        try {
                            MediaController.this.pipRoundVideoView = new PipRoundVideoView();
                            MediaController.this.pipRoundVideoView.show(MediaController.this.baseActivity, new Runnable() {
                                @Override
                                public final void run() {
                                    MediaController.AnonymousClass9.this.lambda$onSurfaceDestroyed$0();
                                }
                            });
                        } catch (Exception unused) {
                            MediaController.this.pipRoundVideoView = null;
                        }
                    }
                    if (MediaController.this.pipRoundVideoView != null) {
                        if (MediaController.this.pipRoundVideoView.getTextureView().getSurfaceTexture() != surfaceTexture) {
                            MediaController.this.pipRoundVideoView.getTextureView().setSurfaceTexture(surfaceTexture);
                        }
                        MediaController.this.videoPlayer.setTextureView(MediaController.this.pipRoundVideoView.getTextureView());
                    }
                }
                MediaController.this.pipSwitchingState = 0;
                return true;
            } else if (!PhotoViewer.hasInstance() || !PhotoViewer.getInstance().isInjectingVideoPlayer()) {
                return false;
            } else {
                PhotoViewer.getInstance().injectVideoPlayerSurface(surfaceTexture);
                return true;
            }
        }

        public void lambda$onSurfaceDestroyed$0() {
            MediaController.this.cleanupPlayer(true, true);
        }
    }

    public void lambda$playMessage$19() {
        cleanupPlayer(true, true);
    }

    public static void lambda$playMessage$20(MessageObject messageObject, File file) {
        NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.fileLoaded, FileLoader.getAttachFileName(messageObject.getDocument()), file);
    }

    public static void lambda$playMessage$21(MessageObject messageObject, File file) {
        NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.fileLoaded, FileLoader.getAttachFileName(messageObject.getDocument()), file);
    }

    public AudioInfo getAudioInfo() {
        return this.audioInfo;
    }

    public void setPlaybackOrderType(int i) {
        boolean z = SharedConfig.shuffleMusic;
        SharedConfig.setPlaybackOrderType(i);
        boolean z2 = SharedConfig.shuffleMusic;
        if (z == z2) {
            return;
        }
        if (z2) {
            buildShuffledPlayList();
            return;
        }
        MessageObject messageObject = this.playingMessageObject;
        if (messageObject != null) {
            int indexOf = this.playlist.indexOf(messageObject);
            this.currentPlaylistNum = indexOf;
            if (indexOf == -1) {
                clearPlaylist();
                cleanupPlayer(true, true);
            }
        }
    }

    public boolean isStreamingCurrentAudio() {
        return this.isStreamingCurrentAudio;
    }

    public boolean isCurrentPlayer(VideoPlayer videoPlayer) {
        return this.videoPlayer == videoPlayer || this.audioPlayer == videoPlayer;
    }

    public boolean lambda$startAudioAgain$7(MessageObject messageObject) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            stopProgressTimer();
            try {
                if (this.audioPlayer == null) {
                    VideoPlayer videoPlayer = this.videoPlayer;
                    if (videoPlayer != null) {
                        videoPlayer.pause();
                    }
                } else if (this.playingMessageObject.isVoice() || this.playingMessageObject.getDuration() * (VOLUME_NORMAL - this.playingMessageObject.audioProgress) <= 1000.0f) {
                    this.audioPlayer.pause();
                } else {
                    ValueAnimator valueAnimator = this.audioVolumeAnimator;
                    if (valueAnimator != null) {
                        valueAnimator.removeAllUpdateListeners();
                        this.audioVolumeAnimator.cancel();
                    }
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(VOLUME_NORMAL, 0.0f);
                    this.audioVolumeAnimator = ofFloat;
                    ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                    this.audioVolumeAnimator.setDuration(300L);
                    this.audioVolumeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (MediaController.this.audioPlayer != null) {
                                MediaController.this.audioPlayer.pause();
                            }
                        }
                    });
                    this.audioVolumeAnimator.start();
                }
                this.isPaused = true;
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                return true;
            } catch (Exception e) {
                FileLog.e(e);
                this.isPaused = false;
            }
        }
        return false;
    }

    private boolean resumeAudio(MessageObject messageObject) {
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || this.playingMessageObject == null || !isSamePlayingMessage(messageObject))) {
            try {
                startProgressTimer(this.playingMessageObject);
                ValueAnimator valueAnimator = this.audioVolumeAnimator;
                if (valueAnimator != null) {
                    valueAnimator.removeAllListeners();
                    this.audioVolumeAnimator.cancel();
                }
                if (!messageObject.isVoice()) {
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(this.audioVolume, VOLUME_NORMAL);
                    this.audioVolumeAnimator = ofFloat;
                    ofFloat.addUpdateListener(this.audioVolumeUpdateListener);
                    this.audioVolumeAnimator.setDuration(300L);
                    this.audioVolumeAnimator.start();
                } else {
                    this.audioVolume = VOLUME_NORMAL;
                    setPlayerVolume();
                }
                VideoPlayer videoPlayer = this.audioPlayer;
                if (videoPlayer != null) {
                    videoPlayer.play();
                } else {
                    VideoPlayer videoPlayer2 = this.videoPlayer;
                    if (videoPlayer2 != null) {
                        videoPlayer2.play();
                    }
                }
                checkAudioFocus(messageObject);
                this.isPaused = false;
                NotificationCenter.getInstance(this.playingMessageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, Integer.valueOf(this.playingMessageObject.getId()));
                return true;
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return false;
    }

    public boolean isVideoDrawingReady() {
        AspectRatioFrameLayout aspectRatioFrameLayout = this.currentAspectRatioFrameLayout;
        return aspectRatioFrameLayout != null && aspectRatioFrameLayout.isDrawingReady();
    }

    public ArrayList<MessageObject> getPlaylist() {
        return this.playlist;
    }

    public boolean isPlayingMessage(MessageObject messageObject) {
        MessageObject messageObject2;
        if (!((this.audioPlayer == null && this.videoPlayer == null) || messageObject == null || (messageObject2 = this.playingMessageObject) == null)) {
            long j = messageObject2.eventId;
            if (j != 0 && j == messageObject.eventId) {
                return !this.downloadingCurrentMessage;
            }
            if (isSamePlayingMessage(messageObject)) {
                return !this.downloadingCurrentMessage;
            }
        }
        return false;
    }

    public boolean isPlayingMessageAndReadyToDraw(MessageObject messageObject) {
        return this.isDrawingWasReady && isPlayingMessage(messageObject);
    }

    public boolean isMessagePaused() {
        return this.isPaused || this.downloadingCurrentMessage;
    }

    public boolean isDownloadingCurrentMessage() {
        return this.downloadingCurrentMessage;
    }

    public void setReplyingMessage(MessageObject messageObject, MessageObject messageObject2) {
        this.recordReplyingMsg = messageObject;
        this.recordReplyingTopMsg = messageObject2;
    }

    public void requestAudioFocus(boolean z) {
        if (z) {
            if (!this.hasRecordAudioFocus && SharedConfig.pauseMusicOnRecord && NotificationsController.audioManager.requestAudioFocus(this.audioRecordFocusChangedListener, 3, 2) == 1) {
                this.hasRecordAudioFocus = true;
            }
        } else if (this.hasRecordAudioFocus) {
            NotificationsController.audioManager.abandonAudioFocus(this.audioRecordFocusChangedListener);
            this.hasRecordAudioFocus = false;
        }
    }

    public void startRecording(final int i, final long j, final MessageObject messageObject, final MessageObject messageObject2, final int i2) {
        MessageObject messageObject3 = this.playingMessageObject;
        boolean z = messageObject3 != null && isPlayingMessage(messageObject3) && !isMessagePaused();
        requestAudioFocus(true);
        try {
            this.feedbackView.performHapticFeedback(3, 2);
        } catch (Exception unused) {
        }
        DispatchQueue dispatchQueue = this.recordQueue;
        Runnable mediaController$$ExternalSyntheticLambda21 = new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$startRecording$26(i, i2, j, messageObject, messageObject2);
            }
        };
        this.recordStartRunnable = mediaController$$ExternalSyntheticLambda21;
        dispatchQueue.postRunnable(mediaController$$ExternalSyntheticLambda21, z ? 500L : 50L);
    }

    public void lambda$startRecording$26(final int i, final int i2, long j, MessageObject messageObject, MessageObject messageObject2) {
        if (this.audioRecorder != null) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$startRecording$22(i, i2);
                }
            });
            return;
        }
        this.sendAfterDone = 0;
        TLRPC$TL_document tLRPC$TL_document = new TLRPC$TL_document();
        this.recordingAudio = tLRPC$TL_document;
        this.recordingGuid = i2;
        tLRPC$TL_document.file_reference = new byte[0];
        tLRPC$TL_document.dc_id = Integer.MIN_VALUE;
        tLRPC$TL_document.id = SharedConfig.getLastLocalId();
        this.recordingAudio.user_id = UserConfig.getInstance(i).getClientUserId();
        TLRPC$TL_document tLRPC$TL_document2 = this.recordingAudio;
        tLRPC$TL_document2.mime_type = "audio/ogg";
        tLRPC$TL_document2.file_reference = new byte[0];
        SharedConfig.saveConfig();
        File file = new File(FileLoader.getDirectory(4), FileLoader.getAttachFileName(this.recordingAudio));
        this.recordingAudioFile = file;
        try {
            if (startRecord(file.getAbsolutePath(), this.sampleRate) == 0) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.this.lambda$startRecording$23(i, i2);
                    }
                });
                return;
            }
            this.audioRecorder = new AudioRecord(0, this.sampleRate, 16, 2, this.recordBufferSize);
            this.recordStartTime = System.currentTimeMillis();
            this.recordTimeCount = 0L;
            this.samplesCount = 0L;
            this.recordDialogId = j;
            this.recordingCurrentAccount = i;
            this.recordReplyingMsg = messageObject;
            this.recordReplyingTopMsg = messageObject2;
            this.fileBuffer.rewind();
            this.audioRecorder.startRecording();
            this.recordQueue.postRunnable(this.recordRunnable);
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$startRecording$25(i, i2);
                }
            });
        } catch (Exception e) {
            FileLog.e(e);
            this.recordingAudio = null;
            stopRecord();
            this.recordingAudioFile.delete();
            this.recordingAudioFile = null;
            try {
                this.audioRecorder.release();
                this.audioRecorder = null;
            } catch (Exception e2) {
                FileLog.e(e2);
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$startRecording$24(i, i2);
                }
            });
        }
    }

    public void lambda$startRecording$22(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    public void lambda$startRecording$23(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    public void lambda$startRecording$24(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStartError, Integer.valueOf(i2));
    }

    public void lambda$startRecording$25(int i, int i2) {
        this.recordStartRunnable = null;
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.recordStarted, Integer.valueOf(i2), Boolean.TRUE);
    }

    public void generateWaveform(final MessageObject messageObject) {
        final String str = messageObject.getId() + "_" + messageObject.getDialogId();
        final String absolutePath = FileLoader.getInstance(messageObject.currentAccount).getPathToMessage(messageObject.messageOwner).getAbsolutePath();
        if (!this.generatingWaveform.containsKey(str)) {
            this.generatingWaveform.put(str, messageObject);
            Utilities.globalQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$generateWaveform$28(absolutePath, str, messageObject);
                }
            });
        }
    }

    public void lambda$generateWaveform$28(String str, final String str2, final MessageObject messageObject) {
        final byte[] waveform = getWaveform(str);
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$generateWaveform$27(str2, waveform, messageObject);
            }
        });
    }

    public void lambda$generateWaveform$27(String str, byte[] bArr, MessageObject messageObject) {
        MessageObject remove = this.generatingWaveform.remove(str);
        if (remove != null && bArr != null && remove.getDocument() != null) {
            int i = 0;
            while (true) {
                if (i >= remove.getDocument().attributes.size()) {
                    break;
                }
                TLRPC$DocumentAttribute tLRPC$DocumentAttribute = remove.getDocument().attributes.get(i);
                if (tLRPC$DocumentAttribute instanceof TLRPC$TL_documentAttributeAudio) {
                    tLRPC$DocumentAttribute.waveform = bArr;
                    tLRPC$DocumentAttribute.flags |= 4;
                    break;
                }
                i++;
            }
            TLRPC$TL_messages_messages tLRPC$TL_messages_messages = new TLRPC$TL_messages_messages();
            tLRPC$TL_messages_messages.messages.add(remove.messageOwner);
            MessagesStorage.getInstance(remove.currentAccount).putMessages((TLRPC$messages_Messages) tLRPC$TL_messages_messages, remove.getDialogId(), -1, 0, false, messageObject.scheduled);
            ArrayList arrayList = new ArrayList();
            arrayList.add(remove);
            NotificationCenter.getInstance(remove.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, Long.valueOf(remove.getDialogId()), arrayList);
        }
    }

    public void stopRecordingInternal(final int i, final boolean z, final int i2) {
        if (i != 0) {
            final TLRPC$TL_document tLRPC$TL_document = this.recordingAudio;
            final File file = this.recordingAudioFile;
            this.fileEncodingQueue.postRunnable(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$stopRecordingInternal$30(tLRPC$TL_document, file, i, z, i2);
                }
            });
        } else {
            File file2 = this.recordingAudioFile;
            if (file2 != null) {
                file2.delete();
            }
            requestAudioFocus(false);
        }
        try {
            AudioRecord audioRecord = this.audioRecorder;
            if (audioRecord != null) {
                audioRecord.release();
                this.audioRecorder = null;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        this.recordingAudio = null;
        this.recordingAudioFile = null;
    }

    public void lambda$stopRecordingInternal$30(final TLRPC$TL_document tLRPC$TL_document, final File file, final int i, final boolean z, final int i2) {
        stopRecord();
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$stopRecordingInternal$29(tLRPC$TL_document, file, i, z, i2);
            }
        });
    }

    public void lambda$stopRecordingInternal$29(TLRPC$TL_document tLRPC$TL_document, File file, int i, boolean z, int i2) {
        boolean z2;
        char c;
        tLRPC$TL_document.date = ConnectionsManager.getInstance(this.recordingCurrentAccount).getCurrentTime();
        tLRPC$TL_document.size = (int) file.length();
        TLRPC$TL_documentAttributeAudio tLRPC$TL_documentAttributeAudio = new TLRPC$TL_documentAttributeAudio();
        tLRPC$TL_documentAttributeAudio.voice = true;
        short[] sArr = this.recordSamples;
        byte[] waveform2 = getWaveform2(sArr, sArr.length);
        tLRPC$TL_documentAttributeAudio.waveform = waveform2;
        if (waveform2 != null) {
            tLRPC$TL_documentAttributeAudio.flags |= 4;
        }
        long j = this.recordTimeCount;
        tLRPC$TL_documentAttributeAudio.duration = (int) (j / 1000);
        tLRPC$TL_document.attributes.add(tLRPC$TL_documentAttributeAudio);
        if (j > 700) {
            if (i == 1) {
                c = 1;
                SendMessagesHelper.getInstance(this.recordingCurrentAccount).sendMessage(tLRPC$TL_document, null, file.getAbsolutePath(), this.recordDialogId, this.recordReplyingMsg, this.recordReplyingTopMsg, null, null, null, null, z, i2, 0, null, null);
            } else {
                c = 1;
            }
            NotificationCenter notificationCenter = NotificationCenter.getInstance(this.recordingCurrentAccount);
            int i3 = NotificationCenter.audioDidSent;
            Object[] objArr = new Object[3];
            z2 = false;
            objArr[0] = Integer.valueOf(this.recordingGuid);
            String str = null;
            objArr[c] = i == 2 ? tLRPC$TL_document : null;
            if (i == 2) {
                str = file.getAbsolutePath();
            }
            objArr[2] = str;
            notificationCenter.postNotificationName(i3, objArr);
        } else {
            z2 = false;
            NotificationCenter.getInstance(this.recordingCurrentAccount).postNotificationName(NotificationCenter.audioRecordTooShort, Integer.valueOf(this.recordingGuid), Boolean.FALSE, Integer.valueOf((int) j));
            file.delete();
        }
        requestAudioFocus(z2);
    }

    public void stopRecording(final int i, final boolean z, final int i2) {
        Runnable runnable = this.recordStartRunnable;
        if (runnable != null) {
            this.recordQueue.cancelRunnable(runnable);
            this.recordStartRunnable = null;
        }
        this.recordQueue.postRunnable(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$stopRecording$32(i, z, i2);
            }
        });
    }

    public void lambda$stopRecording$32(final int i, boolean z, int i2) {
        if (this.sendAfterDone == 3) {
            this.sendAfterDone = 0;
            stopRecordingInternal(i, z, i2);
            return;
        }
        AudioRecord audioRecord = this.audioRecorder;
        if (audioRecord != null) {
            try {
                this.sendAfterDone = i;
                this.sendAfterDoneNotify = z;
                this.sendAfterDoneScheduleDate = i2;
                audioRecord.stop();
            } catch (Exception e) {
                FileLog.e(e);
                File file = this.recordingAudioFile;
                if (file != null) {
                    file.delete();
                }
            }
            if (i == 0) {
                stopRecordingInternal(0, false, 0);
            }
            try {
                this.feedbackView.performHapticFeedback(3, 2);
            } catch (Exception unused) {
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.this.lambda$stopRecording$31(i);
                }
            });
        }
    }

    public void lambda$stopRecording$31(int i) {
        NotificationCenter notificationCenter = NotificationCenter.getInstance(this.recordingCurrentAccount);
        int i2 = NotificationCenter.recordStopped;
        Object[] objArr = new Object[2];
        int i3 = 0;
        objArr[0] = Integer.valueOf(this.recordingGuid);
        if (i == 2) {
            i3 = 1;
        }
        objArr[1] = Integer.valueOf(i3);
        notificationCenter.postNotificationName(i2, objArr);
    }

    public static class MediaLoader implements NotificationCenter.NotificationCenterDelegate {
        private boolean cancelled;
        private int copiedFiles;
        private AccountInstance currentAccount;
        private boolean finished;
        private float finishedProgress;
        private boolean isMusic;
        private HashMap<String, MessageObject> loadingMessageObjects = new HashMap<>();
        private ArrayList<MessageObject> messageObjects;
        private MessagesStorage.IntCallback onFinishRunnable;
        private AlertDialog progressDialog;
        private CountDownLatch waitingForFile;

        public MediaLoader(Context context, AccountInstance accountInstance, ArrayList<MessageObject> arrayList, MessagesStorage.IntCallback intCallback) {
            this.currentAccount = accountInstance;
            this.messageObjects = arrayList;
            this.onFinishRunnable = intCallback;
            this.isMusic = arrayList.get(0).isMusic();
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.fileLoaded);
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.fileLoadProgressChanged);
            this.currentAccount.getNotificationCenter().addObserver(this, NotificationCenter.fileLoadFailed);
            AlertDialog alertDialog = new AlertDialog(context, 2);
            this.progressDialog = alertDialog;
            alertDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setCancelable(true);
            this.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public final void onCancel(DialogInterface dialogInterface) {
                    MediaController.MediaLoader.this.lambda$new$0(dialogInterface);
                }
            });
        }

        public void lambda$new$0(DialogInterface dialogInterface) {
            this.cancelled = true;
        }

        public void start() {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.MediaLoader.this.lambda$start$1();
                }
            }, 250L);
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.MediaLoader.this.lambda$start$2();
                }
            }).start();
        }

        public void lambda$start$1() {
            if (!this.finished) {
                this.progressDialog.show();
            }
        }

        public void lambda$start$2() {
            File file;
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    int size = this.messageObjects.size();
                    for (int i = 0; i < size; i++) {
                        MessageObject messageObject = this.messageObjects.get(i);
                        String str = messageObject.messageOwner.attachPath;
                        String documentName = messageObject.getDocumentName();
                        if (str != null && str.length() > 0 && !new File(str).exists()) {
                            str = null;
                        }
                        if (str == null || str.length() == 0) {
                            str = FileLoader.getInstance(this.currentAccount.getCurrentAccount()).getPathToMessage(messageObject.messageOwner).toString();
                        }
                        File file2 = new File(str);
                        if (!file2.exists()) {
                            this.waitingForFile = new CountDownLatch(1);
                            addMessageToLoad(messageObject);
                            this.waitingForFile.await();
                        }
                        if (this.cancelled) {
                            break;
                        }
                        if (file2.exists()) {
                            MediaController.saveFileInternal(this.isMusic ? 3 : 2, file2, documentName);
                            this.copiedFiles++;
                        }
                    }
                } else {
                    if (this.isMusic) {
                        file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    } else {
                        file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    }
                    file.mkdir();
                    int size2 = this.messageObjects.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        MessageObject messageObject2 = this.messageObjects.get(i2);
                        String documentName2 = messageObject2.getDocumentName();
                        File file3 = new File(file, documentName2);
                        if (file3.exists()) {
                            int lastIndexOf = documentName2.lastIndexOf(46);
                            int i3 = 0;
                            while (true) {
                                if (i3 >= 10) {
                                    break;
                                }
                                File file4 = new File(file, lastIndexOf != -1 ? documentName2.substring(0, lastIndexOf) + "(" + (i3 + 1) + ")" + documentName2.substring(lastIndexOf) : documentName2 + "(" + (i3 + 1) + ")");
                                if (!file4.exists()) {
                                    file3 = file4;
                                    break;
                                } else {
                                    i3++;
                                    file3 = file4;
                                }
                            }
                        }
                        if (!file3.exists()) {
                            file3.createNewFile();
                        }
                        String str2 = messageObject2.messageOwner.attachPath;
                        if (str2 != null && str2.length() > 0 && !new File(str2).exists()) {
                            str2 = null;
                        }
                        if (str2 == null || str2.length() == 0) {
                            str2 = FileLoader.getInstance(this.currentAccount.getCurrentAccount()).getPathToMessage(messageObject2.messageOwner).toString();
                        }
                        File file5 = new File(str2);
                        if (!file5.exists()) {
                            this.waitingForFile = new CountDownLatch(1);
                            addMessageToLoad(messageObject2);
                            this.waitingForFile.await();
                        }
                        if (file5.exists()) {
                            copyFile(file5, file3, messageObject2.getMimeType());
                            this.copiedFiles++;
                        }
                    }
                }
                checkIfFinished();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        private void checkIfFinished() {
            if (this.loadingMessageObjects.isEmpty()) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public final void run() {
                        MediaController.MediaLoader.this.lambda$checkIfFinished$4();
                    }
                });
            }
        }

        public void lambda$checkIfFinished$4() {
            try {
                if (this.progressDialog.isShowing()) {
                    this.progressDialog.dismiss();
                } else {
                    this.finished = true;
                }
                if (this.onFinishRunnable != null) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public final void run() {
                            MediaController.MediaLoader.this.lambda$checkIfFinished$3();
                        }
                    });
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.fileLoaded);
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadProgressChanged);
            this.currentAccount.getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadFailed);
        }

        public void lambda$checkIfFinished$3() {
            this.onFinishRunnable.run(this.copiedFiles);
        }

        private void addMessageToLoad(final MessageObject messageObject) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.MediaLoader.this.lambda$addMessageToLoad$5(messageObject);
                }
            });
        }

        public void lambda$addMessageToLoad$5(MessageObject messageObject) {
            TLRPC$Document document = messageObject.getDocument();
            if (document != null) {
                this.loadingMessageObjects.put(FileLoader.getAttachFileName(document), messageObject);
                this.currentAccount.getFileLoader().loadFile(document, messageObject, 1, 0);
            }
        }

        private boolean copyFile(java.io.File r32, java.io.File r33, java.lang.String r34) {
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.MediaLoader.copyFile(java.io.File, java.io.File, java.lang.String):boolean");
        }

        public void lambda$copyFile$6() {
            try {
                this.progressDialog.dismiss();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        public void lambda$copyFile$7(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        public void lambda$copyFile$8(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        @Override
        public void didReceivedNotification(int i, int i2, Object... objArr) {
            if (i == NotificationCenter.fileLoaded || i == NotificationCenter.fileLoadFailed) {
                if (this.loadingMessageObjects.remove((String) objArr[0]) != null) {
                    this.waitingForFile.countDown();
                }
            } else if (i == NotificationCenter.fileLoadProgressChanged) {
                if (this.loadingMessageObjects.containsKey((String) objArr[0])) {
                    final int longValue = (int) (this.finishedProgress + (((((float) ((Long) objArr[1]).longValue()) / ((float) ((Long) objArr[2]).longValue())) / this.messageObjects.size()) * 100.0f));
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public final void run() {
                            MediaController.MediaLoader.this.lambda$didReceivedNotification$9(longValue);
                        }
                    });
                }
            }
        }

        public void lambda$didReceivedNotification$9(int i) {
            try {
                this.progressDialog.setProgress(i);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void saveFilesFromMessages(Context context, AccountInstance accountInstance, ArrayList<MessageObject> arrayList, MessagesStorage.IntCallback intCallback) {
        if (arrayList != null && !arrayList.isEmpty()) {
            new MediaLoader(context, accountInstance, arrayList, intCallback).start();
        }
    }

    public static void saveFile(String str, Context context, int i, String str2, String str3) {
        saveFile(str, context, i, str2, str3, null);
    }

    public static void saveFile(java.lang.String r13, android.content.Context r14, final int r15, final java.lang.String r16, final java.lang.String r17, final java.lang.Runnable r18) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.saveFile(java.lang.String, android.content.Context, int, java.lang.String, java.lang.String, java.lang.Runnable):void");
    }

    public static void lambda$saveFile$33(boolean[] zArr, DialogInterface dialogInterface) {
        zArr[0] = true;
    }

    public static void lambda$saveFile$34(boolean[] zArr, AlertDialog alertDialog) {
        if (!zArr[0]) {
            alertDialog.show();
        }
    }

    public static void lambda$saveFile$38(int r20, java.io.File r21, java.lang.String r22, final org.telegram.ui.ActionBar.AlertDialog r23, boolean[] r24, java.lang.String r25, java.lang.Runnable r26, final boolean[] r27) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$saveFile$38(int, java.io.File, java.lang.String, org.telegram.ui.ActionBar.AlertDialog, boolean[], java.lang.String, java.lang.Runnable, boolean[]):void");
    }

    public static void lambda$saveFile$35(AlertDialog alertDialog) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void lambda$saveFile$36(AlertDialog alertDialog, int i) {
        try {
            alertDialog.setProgress(i);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void lambda$saveFile$37(AlertDialog alertDialog, boolean[] zArr) {
        try {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            } else {
                zArr[0] = true;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean saveFileInternal(int i, File file, String str) {
        Uri uri;
        try {
            ContentValues contentValues = new ContentValues();
            String fileExtension = FileLoader.getFileExtension(file);
            String str2 = null;
            if (fileExtension != null) {
                str2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            }
            if ((i == 0 || i == 1) && str2 != null) {
                if (str2.startsWith("image")) {
                    i = 0;
                }
                if (str2.startsWith(MediaStreamTrack.VIDEO_TRACK_KIND)) {
                    i = 1;
                }
            }
            if (i == 0) {
                if (str == null) {
                    str = AndroidUtilities.generateFileName(0, fileExtension);
                }
                uri = MediaStore.Images.Media.getContentUri("external_primary");
                File file2 = new File(Environment.DIRECTORY_PICTURES, "Telegram");
                contentValues.put("relative_path", file2 + File.separator);
                contentValues.put("_display_name", str);
                contentValues.put("mime_type", str2);
            } else if (i == 1) {
                if (str == null) {
                    str = AndroidUtilities.generateFileName(1, fileExtension);
                }
                File file3 = new File(Environment.DIRECTORY_MOVIES, "Telegram");
                contentValues.put("relative_path", file3 + File.separator);
                uri = MediaStore.Video.Media.getContentUri("external_primary");
                contentValues.put("_display_name", str);
            } else if (i == 2) {
                if (str == null) {
                    str = file.getName();
                }
                File file4 = new File(Environment.DIRECTORY_DOWNLOADS, "Telegram");
                contentValues.put("relative_path", file4 + File.separator);
                uri = MediaStore.Downloads.getContentUri("external_primary");
                contentValues.put("_display_name", str);
            } else {
                if (str == null) {
                    str = file.getName();
                }
                File file5 = new File(Environment.DIRECTORY_MUSIC, "Telegram");
                contentValues.put("relative_path", file5 + File.separator);
                uri = MediaStore.Audio.Media.getContentUri("external_primary");
                contentValues.put("_display_name", str);
            }
            contentValues.put("mime_type", str2);
            Uri insert = ApplicationLoader.applicationContext.getContentResolver().insert(uri, contentValues);
            if (insert != null) {
                FileInputStream fileInputStream = new FileInputStream(file);
                AndroidUtilities.copyFile(fileInputStream, ApplicationLoader.applicationContext.getContentResolver().openOutputStream(insert));
                fileInputStream.close();
            }
            return true;
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }

    public static java.lang.String getStickerExt(android.net.Uri r8) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.getStickerExt(android.net.Uri):java.lang.String");
    }

    public static boolean isWebp(Uri uri) {
        InputStream inputStream = null;
        try {
            try {
                try {
                    inputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
                    byte[] bArr = new byte[12];
                    if (inputStream.read(bArr, 0, 12) == 12) {
                        String lowerCase = new String(bArr).toLowerCase();
                        if (lowerCase.startsWith("riff")) {
                            if (lowerCase.endsWith("webp")) {
                                try {
                                    inputStream.close();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                                return true;
                            }
                        }
                    }
                    inputStream.close();
                } catch (Exception e2) {
                    FileLog.e(e2);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (Exception e3) {
                FileLog.e(e3);
            }
            return false;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e4) {
                    FileLog.e(e4);
                }
            }
            throw th;
        }
    }

    public static boolean isGif(Uri uri) {
        InputStream inputStream = null;
        try {
            try {
                try {
                    inputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
                    byte[] bArr = new byte[3];
                    if (inputStream.read(bArr, 0, 3) == 3) {
                        if (new String(bArr).equalsIgnoreCase("gif")) {
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            return true;
                        }
                    }
                    inputStream.close();
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e2) {
                            FileLog.e(e2);
                        }
                    }
                    throw th;
                }
            } catch (Exception e3) {
                FileLog.e(e3);
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e4) {
            FileLog.e(e4);
        }
        return false;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) {
            return "";
        }
        String str = null;
        try {
            if (uri.getScheme().equals("content")) {
                try {
                    Cursor query = ApplicationLoader.applicationContext.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
                    if (query.moveToFirst()) {
                        str = query.getString(query.getColumnIndex("_display_name"));
                    }
                    query.close();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            if (str != null) {
                return str;
            }
            String path = uri.getPath();
            int lastIndexOf = path.lastIndexOf(47);
            return lastIndexOf != -1 ? path.substring(lastIndexOf + 1) : path;
        } catch (Exception e2) {
            FileLog.e(e2);
            return "";
        }
    }

    public static String copyFileToCache(Uri uri, String str) {
        return copyFileToCache(uri, str, -1L);
    }

    @android.annotation.SuppressLint({"DiscouragedPrivateApi"})
    public static java.lang.String copyFileToCache(android.net.Uri r13, java.lang.String r14, long r15) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.copyFileToCache(android.net.Uri, java.lang.String, long):java.lang.String");
    }

    public static void loadGalleryPhotosAlbums(final int i) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public final void run() {
                MediaController.lambda$loadGalleryPhotosAlbums$40(i);
            }
        });
        thread.setPriority(1);
        thread.start();
    }

    public static void lambda$loadGalleryPhotosAlbums$40(int r51) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.lambda$loadGalleryPhotosAlbums$40(int):void");
    }

    public static int lambda$loadGalleryPhotosAlbums$39(PhotoEntry photoEntry, PhotoEntry photoEntry2) {
        long j = photoEntry.dateTaken;
        long j2 = photoEntry2.dateTaken;
        if (j < j2) {
            return 1;
        }
        return j > j2 ? -1 : 0;
    }

    private static void broadcastNewPhotos(final int i, final ArrayList<AlbumEntry> arrayList, final ArrayList<AlbumEntry> arrayList2, final Integer num, final AlbumEntry albumEntry, final AlbumEntry albumEntry2, final AlbumEntry albumEntry3, int i2) {
        Runnable runnable = broadcastPhotosRunnable;
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        Runnable mediaController$$ExternalSyntheticLambda6 = new Runnable() {
            @Override
            public final void run() {
                MediaController.lambda$broadcastNewPhotos$41(i, arrayList, arrayList2, num, albumEntry, albumEntry2, albumEntry3);
            }
        };
        broadcastPhotosRunnable = mediaController$$ExternalSyntheticLambda6;
        AndroidUtilities.runOnUIThread(mediaController$$ExternalSyntheticLambda6, i2);
    }

    public static void lambda$broadcastNewPhotos$41(int i, ArrayList arrayList, ArrayList arrayList2, Integer num, AlbumEntry albumEntry, AlbumEntry albumEntry2, AlbumEntry albumEntry3) {
        if (PhotoViewer.getInstance().isVisible()) {
            broadcastNewPhotos(i, arrayList, arrayList2, num, albumEntry, albumEntry2, albumEntry3, 1000);
            return;
        }
        allMediaAlbums = arrayList;
        allPhotoAlbums = arrayList2;
        broadcastPhotosRunnable = null;
        allPhotosAlbumEntry = albumEntry2;
        allMediaAlbumEntry = albumEntry;
        allVideosAlbumEntry = albumEntry3;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.albumsDidLoad, Integer.valueOf(i), arrayList, arrayList2, num);
    }

    public void scheduleVideoConvert(MessageObject messageObject) {
        scheduleVideoConvert(messageObject, false);
    }

    public boolean scheduleVideoConvert(MessageObject messageObject, boolean z) {
        if (messageObject == null || messageObject.videoEditedInfo == null) {
            return false;
        }
        if (z && !this.videoConvertQueue.isEmpty()) {
            return false;
        }
        if (z) {
            new File(messageObject.messageOwner.attachPath).delete();
        }
        this.videoConvertQueue.add(new VideoConvertMessage(messageObject, messageObject.videoEditedInfo));
        if (this.videoConvertQueue.size() == 1) {
            startVideoConvertFromQueue();
        }
        return true;
    }

    public void cancelVideoConvert(MessageObject messageObject) {
        if (!(messageObject == null || this.videoConvertQueue.isEmpty())) {
            for (int i = 0; i < this.videoConvertQueue.size(); i++) {
                VideoConvertMessage videoConvertMessage = this.videoConvertQueue.get(i);
                MessageObject messageObject2 = videoConvertMessage.messageObject;
                if (messageObject2.equals(messageObject) && messageObject2.currentAccount == messageObject.currentAccount) {
                    if (i == 0) {
                        synchronized (this.videoConvertSync) {
                            videoConvertMessage.videoEditedInfo.canceled = true;
                        }
                        return;
                    } else {
                        this.videoConvertQueue.remove(i);
                        return;
                    }
                }
            }
        }
    }

    private boolean startVideoConvertFromQueue() {
        int i = 0;
        if (this.videoConvertQueue.isEmpty()) {
            return false;
        }
        VideoConvertMessage videoConvertMessage = this.videoConvertQueue.get(0);
        MessageObject messageObject = videoConvertMessage.messageObject;
        VideoEditedInfo videoEditedInfo = videoConvertMessage.videoEditedInfo;
        synchronized (this.videoConvertSync) {
            if (videoEditedInfo != null) {
                videoEditedInfo.canceled = false;
            }
        }
        Intent intent = new Intent(ApplicationLoader.applicationContext, VideoEncodingService.class);
        intent.putExtra("path", messageObject.messageOwner.attachPath);
        intent.putExtra("currentAccount", messageObject.currentAccount);
        if (messageObject.messageOwner.media.document != null) {
            while (true) {
                if (i >= messageObject.messageOwner.media.document.attributes.size()) {
                    break;
                } else if (messageObject.messageOwner.media.document.attributes.get(i) instanceof TLRPC$TL_documentAttributeAnimated) {
                    intent.putExtra("gif", true);
                    break;
                } else {
                    i++;
                }
            }
        }
        if (messageObject.getId() != 0) {
            try {
                ApplicationLoader.applicationContext.startService(intent);
            } catch (Throwable th) {
                FileLog.e(th);
            }
        }
        VideoConvertRunnable.runConversion(videoConvertMessage);
        return true;
    }

    @SuppressLint({"NewApi"})
    public static MediaCodecInfo selectCodec(String str) {
        int codecCount = MediaCodecList.getCodecCount();
        MediaCodecInfo mediaCodecInfo = null;
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                for (String str2 : codecInfoAt.getSupportedTypes()) {
                    if (str2.equalsIgnoreCase(str)) {
                        String name = codecInfoAt.getName();
                        if (name != null && (!name.equals("OMX.SEC.avc.enc") || name.equals("OMX.SEC.AVC.Encoder"))) {
                            return codecInfoAt;
                        }
                        mediaCodecInfo = codecInfoAt;
                    }
                }
                continue;
            }
        }
        return mediaCodecInfo;
    }

    @SuppressLint({"NewApi"})
    public static int selectColorFormat(MediaCodecInfo mediaCodecInfo, String str) {
        int i;
        MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(str);
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = capabilitiesForType.colorFormats;
            if (i2 >= iArr.length) {
                return i3;
            }
            i = iArr[i2];
            if (isRecognizedFormat(i)) {
                if (!mediaCodecInfo.getName().equals("OMX.SEC.AVC.Encoder") || i != 19) {
                    break;
                }
                i3 = i;
            }
            i2++;
        }
        return i;
    }

    public static int findTrack(MediaExtractor mediaExtractor, boolean z) {
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            String string = mediaExtractor.getTrackFormat(i).getString("mime");
            if (z) {
                if (string.startsWith("audio/")) {
                    return i;
                }
            } else if (string.startsWith("video/")) {
                return i;
            }
        }
        return -5;
    }

    public void didWriteData(final VideoConvertMessage videoConvertMessage, final File file, final boolean z, final long j, final long j2, final boolean z2, final float f) {
        VideoEditedInfo videoEditedInfo = videoConvertMessage.videoEditedInfo;
        final boolean z3 = videoEditedInfo.videoConvertFirstWrite;
        if (z3) {
            videoEditedInfo.videoConvertFirstWrite = false;
        }
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public final void run() {
                MediaController.this.lambda$didWriteData$42(z2, z, videoConvertMessage, file, f, j, z3, j2);
            }
        });
    }

    public void lambda$didWriteData$42(boolean z, boolean z2, VideoConvertMessage videoConvertMessage, File file, float f, long j, boolean z3, long j2) {
        if (z || z2) {
            synchronized (this.videoConvertSync) {
                videoConvertMessage.videoEditedInfo.canceled = false;
            }
            this.videoConvertQueue.remove(videoConvertMessage);
            startVideoConvertFromQueue();
        }
        if (z) {
            NotificationCenter.getInstance(videoConvertMessage.currentAccount).postNotificationName(NotificationCenter.filePreparingFailed, videoConvertMessage.messageObject, file.toString(), Float.valueOf(f), Long.valueOf(j));
            return;
        }
        if (z3) {
            NotificationCenter.getInstance(videoConvertMessage.currentAccount).postNotificationName(NotificationCenter.filePreparingStarted, videoConvertMessage.messageObject, file.toString(), Float.valueOf(f), Long.valueOf(j));
        }
        NotificationCenter notificationCenter = NotificationCenter.getInstance(videoConvertMessage.currentAccount);
        int i = NotificationCenter.fileNewChunkAvailable;
        Object[] objArr = new Object[6];
        objArr[0] = videoConvertMessage.messageObject;
        objArr[1] = file.toString();
        objArr[2] = Long.valueOf(j2);
        objArr[3] = Long.valueOf(z2 ? file.length() : 0L);
        objArr[4] = Float.valueOf(f);
        objArr[5] = Long.valueOf(j);
        notificationCenter.postNotificationName(i, objArr);
    }

    public void pauseByRewind() {
        VideoPlayer videoPlayer = this.audioPlayer;
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }

    public void resumeByRewind() {
        VideoPlayer videoPlayer = this.audioPlayer;
        if (videoPlayer != null && this.playingMessageObject != null && !this.isPaused) {
            if (videoPlayer.isBuffering()) {
                MessageObject messageObject = this.playingMessageObject;
                cleanupPlayer(false, false);
                playMessage(messageObject);
                return;
            }
            this.audioPlayer.play();
        }
    }

    public static class VideoConvertRunnable implements Runnable {
        private VideoConvertMessage convertMessage;

        private VideoConvertRunnable(VideoConvertMessage videoConvertMessage) {
            this.convertMessage = videoConvertMessage;
        }

        @Override
        public void run() {
            MediaController.getInstance().convertVideo(this.convertMessage);
        }

        public static void runConversion(final VideoConvertMessage videoConvertMessage) {
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    MediaController.VideoConvertRunnable.lambda$runConversion$0(MediaController.VideoConvertMessage.this);
                }
            }).start();
        }

        public static void lambda$runConversion$0(VideoConvertMessage videoConvertMessage) {
            try {
                Thread thread = new Thread(new VideoConvertRunnable(videoConvertMessage), "VideoConvertRunnable");
                thread.start();
                thread.join();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public boolean convertVideo(final org.telegram.messenger.MediaController.VideoConvertMessage r46) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.convertVideo(org.telegram.messenger.MediaController$VideoConvertMessage):boolean");
    }

    public static int getVideoBitrate(String str) {
        int i;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(str);
            i = Integer.parseInt(mediaMetadataRetriever.extractMetadata(20));
        } catch (Exception e) {
            FileLog.e(e);
            i = 0;
        }
        mediaMetadataRetriever.release();
        return i;
    }

    public static int makeVideoBitrate(int r5, int r6, int r7, int r8, int r9) {
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MediaController.makeVideoBitrate(int, int, int, int, int):int");
    }

    public static class PlaylistGlobalSearchParams {
        final long dialogId;
        public boolean endReached;
        final FiltersView.MediaFilterData filter;
        public int folderId;
        final long maxDate;
        final long minDate;
        public int nextSearchRate;
        final String query;
        public int totalCount;

        public PlaylistGlobalSearchParams(String str, long j, long j2, long j3, FiltersView.MediaFilterData mediaFilterData) {
            this.filter = mediaFilterData;
            this.query = str;
            this.dialogId = j;
            this.minDate = j2;
            this.maxDate = j3;
        }
    }

    public boolean currentPlaylistIsGlobalSearch() {
        return this.playlistGlobalSearchParams != null;
    }
}
