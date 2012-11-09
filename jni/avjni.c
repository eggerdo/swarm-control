#include <jni.h>
#include <assert.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG "avjni.c"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}

//a short description of the native interface for decoding of a stream
//please note that for this sample a simple media file is used
//for other use-cases one would use a network stream, this is not shown in this sample

//nativeOpenFrom...() acquires and initialises resources necessary for both audio and video
//calling nativeOpenAudio() or nativeOpenVideo() when nativeOpenFrom...() wasn't called results in an error
//a call to nativeOpenFrom...() - including an unsuccessful one - must be matched by a call to nativeClose()
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenFromFile(JNIEnv* env, jobject thiz, jstring mediafile);
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenFromURL(JNIEnv* env, jobject thiz, jstring url, jstring format);

//nativeClose() cleans up any outstanding resources, including audio &&/|| video when still opened
//a call to nativeOpen() - including an unsuccessful one - must be matched by a call to nativeClose()
JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeClose(JNIEnv* env, jobject thiz);

//nativeOpenAudio() acquires and initialises resources specific to audio
//a call to nativeOpenAudio() - including an unsuccessful one - must be matched by a call to nativeCloseAudio() (or nativeClose())
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenAudio(JNIEnv* env, jobject thiz, jbyteArray audioframe, jintArray audioframelength);

//nativeCloseAudio() cleans up any outstanding audio resources (only audio)
//it is not normally used (nativeClose() is usually used instead), unless the intention is to close audio exclusively
//a call to nativeOpenAudio() - including an unsuccessful one - must be matched by a call to nativeCloseAudio() (or nativeClose())
JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeCloseAudio(JNIEnv* env, jobject thiz);

//nativeOpenVideo() acquires and initialises resources specific to video
//a call to nativeOpenVideo() - including an unsuccessful one - must be matched by a call to nativeCloseVideo() (or nativeClose())
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenVideo(JNIEnv* env, jobject thiz, jobject bitmap);

//nativeCloseVideo() cleans up any outstanding video resources (only video)
//it is not normally used (as nativeClose() is usually used instead), unless the intention is to close video exclusively
//a call to nativeOpenVideo() - including an unsuccessful one - must be matched by a call to nativeCloseVideo() (or nativeClose())
JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeCloseVideo(JNIEnv* env, jobject thiz);

//nativeDecodeFrameFrom...() reads a frame (from ...) and decodes it (either audio or video depending on the frame)
//the return value indicates whether an audio or video (or none) data has been processed:
//return value of:
//AUDIO_DATA_ID - an audio frame was decoded and can now be accessed for playback
//VIDEO_DATA_ID - a video frame was decoded and nativeUpdateBitmap() can now be called (canvas must be locked)
//0 - no suitable frame has been found
//<0 - an error has occured - retry or abort depending on your specific needs
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeDecodeFrame(JNIEnv* env, jobject thiz);

//nativeUpdateBitmap() can only be called if the last call to nativeDecodeFrameFrom...() returned VIDEO_DATA_ID
//the canvas must be locked before calling this function
JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeUpdateBitmap(JNIEnv* env, jobject thiz);

#define SUCCESS	 				0
#define NOTHING_FOUND 			-1
#define READ_FRAME_FAILED		-2
#define NO_FRAME_DECODED		-3
#define INVALID_ARGUMENTS		-4
#define OPEN_INPUT_FAILED		-5
#define STREAM_INFO_FAILED		-6
#define NOT_CLOSED				-7
#define ALLOC_FRAME_FAILED		-8
#define BITMAP_REF_FAILED		-9
#define BITMAP_INFO_FAILED		-10
#define VIDEO_STREAM_NOT_FOUND	-11
#define DECODER_NOT_FOUND		-12
#define CODEC_OPEN_FAILED		-13
#define CODEC_DIMENSION_ERROR	-14
#define FILL_PICTURE_FAILED		-15
#define GET_SWCONTEXT_FAILED	-16
#define SCALE_FAILED			-17

AVFormatContext*    gFormatCtx;

//video
AVCodecContext*     gVideoCodecCtx;
int                 gVideoStreamIdx;
AVFrame*            gVideoFrame;
jobject             gBitmapRef;
AndroidBitmapInfo   gAbi;
struct SwsContext*  gSwsContext;

void initialise(void) {
	gFormatCtx = NULL;

	//video
	gVideoCodecCtx = NULL;
	gVideoStreamIdx = -1;
	gVideoFrame = NULL;
	gBitmapRef = NULL;
	gSwsContext = NULL;
	memset(&gAbi, 0, sizeof(gAbi));
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    LOGI(3, "JNI_OnLoad()");

    initialise();

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{

}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenFromURL(JNIEnv* env, jobject thiz, jstring url, jstring format)
{
	LOGI(3, "nativeOpenFromURL()");

    initialise();

    avcodec_register_all();
    av_register_all();
	avformat_network_init();

	const char* mUrl = (*env)->GetStringUTFChars(env, url, 0);
	if (mUrl == 0)
	{
		LOGE(1, "failed to retrieve url");
		return INVALID_ARGUMENTS;
	}

	LOGI(3, "opening %s", mUrl);

	const char* mFormat = (*env)->GetStringUTFChars(env, format, 0);
	if (mFormat == 0)
	{
		LOGE(1, "failed to retrieve format");
		return INVALID_ARGUMENTS;
	}

	// for network streams we have to define the expected format otherwise
	// avformat_open_input() will fail
    AVInputFormat *inputFormat = av_find_input_format(mFormat);

    int result = avformat_open_input(&gFormatCtx, mUrl, inputFormat, NULL);

    (*env)->ReleaseStringUTFChars(env, url, mUrl); //always release the java string reference
    (*env)->ReleaseStringUTFChars(env, format, mFormat); //always release the java string reference

    if (result != 0)
    {
    	LOGE(1, "avformat_open_input() failed");
        return OPEN_INPUT_FAILED;
    }

//    if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
//    {
//    	LOGE(1, "av_find_stream_info() failed");
//        return -3;
//    }

    return SUCCESS;
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenFromFile(JNIEnv* env, jobject thiz, jstring mediafile)
{
	LOGI(3, "nativeOpenFromFile()");

    avcodec_register_all();
    av_register_all();

    const char* mfileName = (*env)->GetStringUTFChars(env, mediafile, 0);
    if (mfileName == 0)
    {
    	LOGE(1, "failed to retrieve media file name");
        return INVALID_ARGUMENTS;
    }

    LOGI(3, "opening %s", mfileName);

    int result = avformat_open_input(&gFormatCtx, mfileName, NULL, NULL);

    (*env)->ReleaseStringUTFChars(env, mediafile, mfileName); //always release the java string reference

    if (result != 0)
    {
    	LOGE(1, "avformat_open_input() failed");
        return OPEN_INPUT_FAILED;
    }

    if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
    {
    	LOGE(1, "av_find_stream_info() failed");
        return STREAM_INFO_FAILED;
    }

    return SUCCESS;
}

JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeClose(JNIEnv* env, jobject thiz)
{
	LOGI(3, "nativeClose()");

    Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeCloseVideo(env, thiz);

    if (gFormatCtx)
    {
    	avformat_close_input(&gFormatCtx);
        gFormatCtx = NULL;
    }
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenVideo(JNIEnv* env, jobject thiz, jobject bitmap)
	{
    LOGI(3, "nativeOpenVideo()");

    if (gVideoFrame)
    {
        LOGE(1, "call nativeCloseVideo() before calling this function");
        return NOT_CLOSED;
    }

    if ((*env)->IsSameObject(env, bitmap, NULL))
    {
        LOGE(1, "invalid arguments");
        return INVALID_ARGUMENTS;
    }

    gVideoFrame = avcodec_alloc_frame();
    if (gVideoFrame == 0)
    {
        LOGE(1, "avcodec_alloc_frame() failed");
        return ALLOC_FRAME_FAILED;
    }

    gBitmapRef = (*env)->NewGlobalRef(env, bitmap); //lock the bitmap preventing the garbage collector from destructing it
    if (gBitmapRef == NULL)
    {
        LOGE(1, "NewGlobalRef() failed");
        return BITMAP_REF_FAILED;
    }

    if (AndroidBitmap_getInfo(env, gBitmapRef, &gAbi) != 0)
    {
        LOGE(1, "AndroidBitmap_getInfo() failed");
        return BITMAP_INFO_FAILED;
    }

    LOGI(3, "bitmap width: %d", gAbi.width);
    LOGI(3, "bitmap height: %d", gAbi.height);
    LOGI(3, "bitmap stride: %d", gAbi.stride);
    LOGI(3, "bitmap format: %d", gAbi.format);
    LOGI(3, "bitmap flags: %d", gAbi.flags);

    int i;
    int videoStreamIdx = -1;
    for (i = 0; i < gFormatCtx->nb_streams && videoStreamIdx == -1; ++i)
        if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
            videoStreamIdx = i;

    if (videoStreamIdx == -1)
    {
        LOGE(1, "video stream not found");
        return VIDEO_STREAM_NOT_FOUND;
    }

    gVideoCodecCtx = gFormatCtx->streams[videoStreamIdx]->codec;
    AVCodec* videoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
    if (!videoCodec)
    {
        LOGE(1, "avcodec_find_decoder() failed to find decoder");
        return DECODER_NOT_FOUND;
    }

    if (videoCodec->capabilities & CODEC_CAP_TRUNCATED)
    	gVideoCodecCtx->flags |= CODEC_FLAG_TRUNCATED;

    if (avcodec_open2(gVideoCodecCtx, videoCodec, NULL) != 0)
    {
        LOGE(1, "avcodec_open2() failed");
        return CODEC_OPEN_FAILED;
    }

    //all good, set index so that nativeProcess() can now recognise the video stream
    gVideoStreamIdx = videoStreamIdx;
    return SUCCESS;
}

JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeCloseVideo(JNIEnv* env, jobject thiz)
{
    LOGI(3, "nativeCloseVideo()");

    if (gVideoCodecCtx)
    {
        avcodec_close(gVideoCodecCtx);
        // causes segmentation fault when avformat_close_input is called
//        av_free(gVideoCodecCtx);
        gVideoCodecCtx = NULL;

    }

    sws_freeContext(gSwsContext);
    gSwsContext = NULL;

    if (gBitmapRef)
    {
        (*env)->DeleteGlobalRef(env, gBitmapRef);
        gBitmapRef = NULL;
    }

    gVideoStreamIdx = -1;

    av_free(gVideoFrame);
    gVideoFrame = NULL;

    memset(&gAbi, 0, sizeof(gAbi));
}

int decodeFrameFromPacket(AVPacket* aPacket)
{
    if (aPacket->stream_index == gVideoStreamIdx)
    {
        int frameFinished = 0;
        if (avcodec_decode_video2(gVideoCodecCtx, gVideoFrame, &frameFinished, aPacket) <= 0)
        {
            LOGW(1, "avcodec_decode_video2() decoded no frame");
            return NO_FRAME_DECODED;
        }
        return SUCCESS;
    }

    return NOTHING_FOUND;
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeDecodeFrame(JNIEnv* env, jobject thiz)
{
    AVPacket packet;
    memset(&packet, 0, sizeof(packet)); //make sure we can safely free it

    int i;
    for (i = 0; i < gFormatCtx->nb_streams; ++i)
    {
        if (av_read_frame(gFormatCtx, &packet) != 0)
        {
            LOGE(1, "av_read_frame() failed");
            return READ_FRAME_FAILED;
        }

        int result = decodeFrameFromPacket(&packet);
        av_free_packet(&packet);
        return result;
    }

    return NOTHING_FOUND;
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeUpdateBitmap(JNIEnv* env, jobject thiz)
{
    AVFrame* 	pFrame_RGB565;
    int result;
    void *buffer;
	int destWidth = gVideoCodecCtx->width;
	int destHeight = gVideoCodecCtx->height;

	if ((destWidth != 640) || destHeight != 360) {
    	LOGE(1, "destWidth = %d", destWidth);
    	LOGE(1, "destHeight = %d", destHeight);
		result = CODEC_DIMENSION_ERROR;
		goto end;
	}

	if ((result = AndroidBitmap_lockPixels(env, gBitmapRef, &buffer)) < 0) {
		LOGE(1, "AndroidBitmap_lockPixels() failed ! error=%d", result);
		goto end;
	}

    pFrame_RGB565 = avcodec_alloc_frame();

    int size = avpicture_fill((AVPicture *)pFrame_RGB565, buffer, PIX_FMT_RGB565, destWidth, destHeight);
    if (size != gAbi.stride * gAbi.height)
    {
    	LOGE(1, "buffersize = %d", sizeof(*buffer));
    	LOGE(1, "destWidth = %d", destWidth);
    	LOGE(1, "destHeight = %d", destHeight);
        LOGE(1, "size != gAbi.stride * gAbi.height");
        LOGE(1, "size = %d", size);
        LOGE(1, "gAbi.stride * gAbi.height = %d", gAbi.stride * gAbi.height);
        result = FILL_PICTURE_FAILED;
        goto release;
    }

    gSwsContext = sws_getCachedContext(gSwsContext, destWidth, destHeight,
    		gVideoCodecCtx->pix_fmt, gAbi.width, gAbi.height, PIX_FMT_RGB565, SWS_BICUBIC, NULL, NULL, NULL);
    if (gSwsContext == 0)
    {
        LOGE(1, "sws_getCachedContext() failed");
        result = GET_SWCONTEXT_FAILED;
        goto release;
    }

    int height = sws_scale(gSwsContext, (const uint8_t* const*)gVideoFrame->data, gVideoFrame->linesize, 0,
    		gVideoCodecCtx->height, pFrame_RGB565->data, pFrame_RGB565->linesize);
    if (height != gAbi.height)
    {
        LOGE(1, "height != gAbi.height");
        LOGE(1, "height = %d", height);
        LOGE(1, "gAbi.height = %d", gAbi.height);
        result = SCALE_FAILED;
        goto release;
    }

    result = SUCCESS;

	release:
    av_free(pFrame_RGB565);
	AndroidBitmap_unlockPixels(env, gBitmapRef);

    end:
    return result;
}

