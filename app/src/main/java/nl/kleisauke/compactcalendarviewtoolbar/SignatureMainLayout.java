package nl.kleisauke.compactcalendarviewtoolbar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

class SignatureMainLayout extends LinearLayout implements View.OnClickListener {
    LinearLayout buttonsLayout;
    SignatureView signatureView;
    private int i = 0;

    public SignatureMainLayout(Context context) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);

        this.buttonsLayout = this.buttonsLayout();
        this.signatureView = new SignatureView(context);

        // add the buttons and signature views
        this.addView(this.buttonsLayout);
        this.addView(signatureView);

    }

    private LinearLayout buttonsLayout() {

        // create the UI programatically
        LinearLayout linearLayout = new LinearLayout(this.getContext());
        Button saveBtn = new Button(this.getContext());
        Button clearBtn = new Button(this.getContext());
        Button View = new Button(this.getContext());

        // set orientation
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackgroundColor(Color.GRAY);

        // set texts, tags and OnClickListener
        saveBtn.setText("Save");
        saveBtn.setTag("Save");
        saveBtn.setOnClickListener(this);

        clearBtn.setText("Clear");
        clearBtn.setTag("Clear");
        clearBtn.setOnClickListener(this);
        View.setText("View");
        View.setTag("View");
        View.setOnClickListener(this);

        linearLayout.addView(saveBtn);
        linearLayout.addView(clearBtn);
        linearLayout.addView(View);

        // return the whoe layout
        return linearLayout;
    }

    // the on click listener of 'save' and 'clear' buttons
    @Override
    public void onClick(View v) {
        String tag = v.getTag().toString().trim();

        // save the signature
        if (tag.equalsIgnoreCase("save")) {
            this.saveImage(this.signatureView.getSignature());
        }

        // empty the canvas
        else {
            this.signatureView.clearSignature();
        }

    }

    /**
     * save the signature to an sd card directory
     * @param signature bitmap
     */
    final void saveImage(Bitmap signature) {

//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                Uri.fromParts("package", getContext().getPackageName(), null));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getContext().startActivity(intent);

        isStoragePermissionGranted();


        String root = Environment.getExternalStorageDirectory().toString();

        // the directory where the signature will be saved
        File myDir = new File(root + "/saved_signature");





        // make the directory if it does not exist yet
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // set the file name of your choice
        String fname = "signature.png";
        // in our case, we delete the previous file, you can remove this
        i++;
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();

        }

        try {
            // save the signature

            FileOutputStream out = new FileOutputStream(file);
            Toast.makeText(this.getContext(),"started",Toast.LENGTH_LONG).show();
            signature.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));

            Toast.makeText(this.getContext(), "Signature saved.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(),"Enable Storge periion in app settings..",Toast.LENGTH_LONG).show();
        }
    }

    private boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("MAina","Permission is granted");
                return true;
            } else {

                Log.v("MAina","Permission is revoked");
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Maina","Permission is granted");
            return true;
        }
    }

    /**
     * The View where the signature will be drawn
     */
    private class SignatureView extends View {

        // set the stroke width
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public SignatureView(Context context) {

            super(context);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);

            // set the bg color as white
            this.setBackgroundColor(Color.WHITE);

            // width and height should cover the screen
            this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        }

        /**
         * Get signature
         *
         * @return
         */
        protected Bitmap getSignature() {

            Bitmap signatureBitmap = null;

            // set the signature bitmap
            if (signatureBitmap == null) {
                signatureBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);
            }

            // important for saving signature
            final Canvas canvas = new Canvas(signatureBitmap);
            this.draw(canvas);

            return signatureBitmap;
        }

        /**
         * clear signature canvas
         */
        private void clearSignature() {
            path.reset();
            this.invalidate();
        }

        // all touch events during the drawing
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(this.path, this.paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    path.moveTo(eventX, eventY);

                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);

                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:

                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }


        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

}
