package si.virag.videostreamer;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;

import java.io.IOException;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    private TextureView tv;

    private Camera cam;

    private long lastTime;
    private int fpsCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextureView) findViewById(R.id.preview);
        tv.setSurfaceTextureListener(this);
        lastTime = System.currentTimeMillis();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (tv.isAvailable())
            initCamera();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (cam != null)
            cam.release();
    }

    private void initCamera()
    {
        cam = Camera.open();
        try
        {
            cam.setPreviewTexture(tv.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = cam.getParameters();
        params.setPreviewSize(720, 480);
        params.setRotation(90);
        params.setPreviewFpsRange(30000, 30000);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setPreviewFormat(ImageFormat.YV12);
        params.setAutoExposureLock(false);
        cam.setDisplayOrientation(90);
        cam.setParameters(params);
        cam.setPreviewCallback(this);
        cam.startPreview();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2)
    {
        initCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2)
    {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera)
    {
        if (System.currentTimeMillis() - lastTime > 1000)
        {
            Log.d("VideoStreamer", "FPS: " + fpsCounter);
            lastTime = System.currentTimeMillis();
        }

        fpsCounter ++;
    }
}
