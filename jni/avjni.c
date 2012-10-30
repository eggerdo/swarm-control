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

#define VIDEO_DATA_ID   1

AVFormatContext*    gFormatCtx;

//video
AVCodecContext*     gVideoCodecCtx;
int                 gVideoStreamIdx;
AVFrame*            gVideoFrame;
jobject             gBitmapRef;
void*               gBitmapRefPixelBuffer;
AndroidBitmapInfo   gAbi;
struct SwsContext*  gSwsContext;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    LOGI(3, "JNI_OnLoad()");

    gFormatCtx = NULL;

    //video
    gVideoCodecCtx = NULL;
    gVideoStreamIdx = -1;
    gVideoFrame = NULL;
    gBitmapRef = NULL;
    gBitmapRefPixelBuffer = NULL;
    gSwsContext = NULL;
    memset(&gAbi, 0, sizeof(gAbi));

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{

}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeOpenFromURL(JNIEnv* env, jobject thiz, jstring url, jstring format)
{
	LOGI(3, "nativeOpenFromURL()");

    avcodec_register_all();
    av_register_all();
	avformat_network_init();

	const char* mUrl = (*env)->GetStringUTFChars(env, url, 0);
	if (mUrl == 0)
	{
		LOGE(1, "failed to retrieve url");
		return -1;
	}

	LOGI(3, "opening %s", mUrl);

	const char* mFormat = (*env)->GetStringUTFChars(env, format, 0);
	if (mFormat == 0)
	{
		LOGE(1, "failed to retrieve format");
		return -1;
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
        return -2;
    }

//    if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
//    {
//    	LOGE(1, "av_find_stream_info() failed");
//        return -3;
//    }

    return 0;
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
        return -1;
    }

    LOGI(3, "opening %s", mfileName);

    int result = avformat_open_input(&gFormatCtx, mfileName, NULL, NULL);

    (*env)->ReleaseStringUTFChars(env, mediafile, mfileName); //always release the java string reference

    if (result != 0)
    {
    	LOGE(1, "avformat_open_input() failed");
        return -2;
    }

    if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
    {
    	LOGE(1, "av_find_stream_info() failed");
        return -3;
    }

    return 0;
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
        return -1;
    }

    if ((*env)->IsSameObject(env, bitmap, NULL))
    {
        LOGE(1, "invalid arguments");
        return -2;
    }

    gVideoFrame = avcodec_alloc_frame();
    if (gVideoFrame == 0)
    {
        LOGE(1, "avcodec_alloc_frame() failed");
        return -3;
    }

    gBitmapRef = (*env)->NewGlobalRef(env, bitmap); //lock the bitmap preventing the garbage collector from destructing it
    if (gBitmapRef == NULL)
    {
        LOGE(1, "NewGlobalRef() failed");
        return -4;
    }

    int result = AndroidBitmap_lockPixels(env, gBitmapRef, &gBitmapRefPixelBuffer);
    if (result != 0)
    {
        LOGE(1, "AndroidBitmap_lockPixels() failed with %d", result);
        gBitmapRefPixelBuffer = NULL;
        return -5;
    }

    if (AndroidBitmap_getInfo(env, gBitmapRef, &gAbi) != 0)
    {
        LOGE(1, "AndroidBitmap_getInfo() failed");
        return -6;
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
        return -7;
    }

    gVideoCodecCtx = gFormatCtx->streams[videoStreamIdx]->codec;
    AVCodec* videoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
    if (!videoCodec)
    {
        LOGE(1, "avcodec_find_decoder() failed to find decoder");
        return -8;
    }

    if (videoCodec->capabilities & CODEC_CAP_TRUNCATED)
    	gVideoCodecCtx->flags |= CODEC_FLAG_TRUNCATED;

    if (avcodec_open2(gVideoCodecCtx, videoCodec, NULL) != 0)
    {
        LOGE(1, "avcodec_open2() failed");
        return -9;
    }

    //all good, set index so that nativeProcess() can now recognise the video stream
    gVideoStreamIdx = videoStreamIdx;
    return 0;
}

JNIEXPORT void JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeCloseVideo(JNIEnv* env, jobject thiz)
{
    LOGI(3, "nativeCloseVideo()");

    if (gVideoCodecCtx)
    {
        avcodec_close(gVideoCodecCtx);
        av_free(gVideoCodecCtx);
        gVideoCodecCtx = NULL;
    }

    sws_freeContext(gSwsContext);
    gSwsContext = NULL;

    if (gBitmapRef)
    {
        if (gBitmapRefPixelBuffer)
        {
            AndroidBitmap_unlockPixels(env, gBitmapRef);
            gBitmapRefPixelBuffer = NULL;
        }
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
            LOGE(1, "avcodec_decode_video2() decoded no frame");
            return -1;
        }
        return VIDEO_DATA_ID;
    }

    return 0;
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeDecodeFrame(JNIEnv* env, jobject thiz)
{
    AVPacket packet;
    memset(&packet, 0, sizeof(packet)); //make sure we can safely free it

    int i;
    for (i = 0; i < gFormatCtx->nb_streams; ++i)
    {
        //av_init_packet(&packet);
        if (av_read_frame(gFormatCtx, &packet) != 0)
        {
            LOGE(1, "av_read_frame() failed");
            return -1;
        }

        int ret = decodeFrameFromPacket(&packet);
        av_free_packet(&packet);
        if (ret != 0) //an error or a frame decoded
            return ret;

//        av_free_packet(&packet);
//        return 3;
    }

    return 0;
}

JNIEXPORT jint JNICALL Java_org_dobots_robots_parrot_ParrotVideoProcessor_nativeUpdateBitmap(JNIEnv* env, jobject thiz)
{
//	static int sws_flags = av_get_int(sws_opts, "sws_flags", NULL);
    gSwsContext = sws_getCachedContext(gSwsContext, gVideoCodecCtx->width, gVideoCodecCtx->height,
    		gVideoCodecCtx->pix_fmt, gAbi.width, gAbi.height, PIX_FMT_RGB565LE, SWS_FAST_BILINEAR, NULL, NULL, NULL);
    if (gSwsContext == 0)
    {
        LOGE(1, "sws_getCachedContext() failed");
        return -1;
    }

//    AVPicture pict;
    AVFrame* pict;
    pict = avcodec_alloc_frame();
    int size = avpicture_fill((AVPicture *)pict, gBitmapRefPixelBuffer, PIX_FMT_RGB565LE, gAbi.width, gAbi.height);
    if (size != gAbi.stride * gAbi.height)
    {
        LOGE(1, "size != gAbi.stride * gAbi.height");
        LOGE(1, "size = %d", size);
        LOGE(1, "gAbi.stride * gAbi.height = %d", gAbi.stride * gAbi.height);
        return -2;
    }

    int height = sws_scale(gSwsContext, (const uint8_t* const*)gVideoFrame->data, gVideoFrame->linesize, 0, gVideoCodecCtx->height, pict->data, pict->linesize);
    if (height != gAbi.height)
    {
        LOGE(1, "height != gAbi.height");
        LOGE(1, "height = %d", height);
        LOGE(1, "gAbi.height = %d", gAbi.height);
        return -3;
    }

    av_free(pict);

    return 0;
}

