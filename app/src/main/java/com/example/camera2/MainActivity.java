package com.example.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private Button captureButton;
    private SurfaceView surfaceView;

    private ImageReader imageReader;
    private CameraManager cameraManager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private Handler handler;
    private Runnable runnable;

    private boolean capturing = false;

    private imageArray array;

    private int subindex,superindex;

//    private void createCameraPreviewSession() {
//        try {
//            Surface surface = surfaceView.getHolder().getSurface();
//
//            imageReader = ImageReader.newInstance(
//                    surfaceView.getWidth(),
//                    surfaceView.getHeight(),
//                    ImageFormat.JPEG,
//                    1);
//            imageReader.setOnImageAvailableListener(imageAvailableListener, null);
//
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            captureRequestBuilder.addTarget(imageReader.getSurface());
//
//            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), cameraCaptureSessionCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

//    private Bitmap convertImageToBitmap(Image image) {
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] data = new byte[buffer.remaining()];
//        buffer.get(data);
//        return BitmapFactory.decodeByteArray(data, 0, data.length);
//    }

//    private final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            Image image = reader.acquireLatestImage();
//            if (image != null) {
//                // Convert the captured image to a Bitmap
//                Bitmap bitmap = convertImageToBitmap(image);
//
//                // Process the Bitmap or store it as needed
//                // ...
//                image imagetemp = new image(bitmap , superindex , subindex);
//
//                array.addImage(imagetemp);
//
//                Log.i("array" , array.toString());
//
//                image.close();
//            }
//        }
//    };


    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private final CameraCaptureSession.StateCallback cameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            cameraCaptureSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(getApplicationContext(), "Failed to configure camera capture session", Toast.LENGTH_SHORT).show();
        }
    };

    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // No implementation needed
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = findViewById(R.id.capture_button);
        captureButton.setText("Start");
        surfaceView = findViewById(R.id.surface_view);

        subindex = 0;
        superindex = -1;

        repatedcapture();

        captureButton.setOnClickListener(v -> {
            if(!capturing) {
                capturing = true;
                captureButton.setText("Stop");
                superindex += 1;
                subindex = 0;
                handler.post(runnable);
            }
            else{
                capturing = false;
                captureButton.setText("Restart");
                handler.removeCallbacks(runnable);
            }
        });


        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
                return;
            }
            cameraManager.openCamera(cameraId, cameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            Surface surface = surfaceView.getHolder().getSurface();

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), cameraCaptureSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void repatedcapture(){

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                captureImage();
                subindex += 1;
                Log.d("Task", "Capturing...");

                // Repeat the task after 1 second
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove the callback when the activity is destroyed
        handler.removeCallbacks(runnable);
    }
    private void captureImage() {
        if (cameraDevice == null) {
            return;
        }

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(surfaceView.getHolder().getSurface());

            cameraCaptureSession.stopRepeating();
            cameraCaptureSession.capture(captureBuilder.build(), null, null);
//            Toast.makeText(this.captureButton.getContext(), "Image captured", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }
}
