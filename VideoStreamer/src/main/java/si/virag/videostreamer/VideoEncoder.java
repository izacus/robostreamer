package si.virag.videostreamer;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jernej on 30.7.2013.
 */
public class VideoEncoder
{

    private List<RawImage> images = new ArrayList<RawImage>();
    private long timer = 0;

    private final Context ctx;
    private MediaCodec codec;

    private long memoryPressure = 0;

    public VideoEncoder(Context ctx)
    {
        this.ctx = ctx;
    }

    public void init()
    {
        codec = MediaCodec.createEncoderByType("video/avc");


        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 720, 480);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        Log.d("VideoStreamer", "Codec configured.");
        codec.start();
    }

    public void pushBuffer(byte[] buffer)
    {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        if (codec != null)
        {
            images.add(new RawImage(buffer));
            memoryPressure += buffer.length;

            while (images.size() > 0)
            {
                int bufferIndex = codec.dequeueInputBuffer(1000);
                if (bufferIndex < 0)
                    break;

                RawImage img = images.remove(0);
                codec.getInputBuffers()[bufferIndex].put(img.data);
                memoryPressure -= img.data.length;

                codec.queueInputBuffer(bufferIndex, 0, img.data.length, timer, 0);
                timer += 1000000;
            }

            while (true)
            {
                int outputBufferIndex = codec.dequeueOutputBuffer(info, 1000);
                if (outputBufferIndex < 0)
                    break;

                Log.i("VideoStreamer", "Output frame: " + info.size + " B, presentation time: " + info.presentationTimeUs);
                codec.releaseOutputBuffer(outputBufferIndex, false);
            }


        }

        Log.i("VideoStreamer", "Memory pressure " + (memoryPressure / 1000) + " kB.");
    }

    public void close()
    {
        if (codec != null)
        {
            codec.stop();
            codec.release();
        }
    }

    private class RawImage
    {
        byte[] data;

        public RawImage(byte[] data)
        {
            this.data = data;
        }
    }
}
