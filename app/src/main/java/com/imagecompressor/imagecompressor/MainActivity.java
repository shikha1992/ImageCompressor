package com.imagecompressor.imagecompressor;


        import android.Manifest;
        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
        import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
        import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
        import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

        import org.json.JSONException;

        import java.io.ByteArrayInputStream;
        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.text.DecimalFormat;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Random;


        import id.zelory.compressor.Compressor;
        import io.reactivex.android.schedulers.AndroidSchedulers;
        import io.reactivex.functions.Consumer;
        import io.reactivex.schedulers.Schedulers;

//        import id.zelory.compressor.Compressor;
//        import io.reactivex.android.schedulers.AndroidSchedulers;
//        import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView actualImageView;
    private ImageView compressedImageView;
    private TextView actualSizeTextView;
    private TextView compressedSizeTextView;
    private File actualImage;
    private File compressedImage;
    TransferObserver observerthumb_user=null;
    ProgressDialog progressDialog;
    private TransferUtility transferUtility;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        util               = new Util();
        transferUtility    = util.getTransferUtility(this);
        actualImageView = (ImageView) findViewById(R.id.actual_image);
        compressedImageView = (ImageView) findViewById(R.id.compressed_image);
        actualSizeTextView = (TextView) findViewById(R.id.actual_size);
        compressedSizeTextView = (TextView) findViewById(R.id.compressed_size);

        actualImageView.setBackgroundColor(getRandomColor());
        clearImage();
    }

    public void chooseImage(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//					String messagesPermission1 = Manifest.permission.CAMERA;
//
//					String messagesPermission2 = Manifest.permission.READ_EXTERNAL_STORAGE;
//					String messagesPermission3 = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//					int hasaccesslocation4 = checkSelfPermission(messagesPermission1);
//					int hasaccesslocation3 = checkSelfPermission(messagesPermission2);
//					int hasaccesslocation2 =checkSelfPermission(messagesPermission3);
//
//					List<String> permissions = new ArrayList<String>();
//					if (hasaccesslocation4 != PackageManager.PERMISSION_GRANTED ||hasaccesslocation3 != PackageManager.PERMISSION_GRANTED ||hasaccesslocation2 != PackageManager.PERMISSION_GRANTED ) {
//						requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//
//						return;
//					}
//				}
//				else {


            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
//        }
    }

    public void compressImage(View view) {
        if (actualImage == null) {
            showError("Please choose an image!");
        } else {

            // Compress image in main thread
            //compressedImage = new Compressor(this).compressToFile(actualImage);
            //setCompressedImage();

            // Compress image to bitmap in main thread
            //compressedImageView.setImageBitmap(new Compressor(this).compressToBitmap(actualImage));

            // Compress image using RxJava in background thread
            new Compressor(this)
                    .compressToFileAsFlowable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            compressedImage = file;
                            setCompressedImage();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            showError(throwable.getMessage());
                        }
                    });
        }
    }

    public void customCompressImage(View view) {
        if (actualImage == null) {
            showError("Please choose an image!");
        } else {
            // Compress image in main thread using custom Compressor
            try {
                compressedImage = new Compressor(this)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(50)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);

                setCompressedImage();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }

            // Compress image using RxJava in background thread with custom Compressor
            /*new Compressor(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFileAsFlowable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            compressedImage = file;
                            setCompressedImage();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            showError(throwable.getMessage());
                        }
                    });*/
        }
    }

    private void setCompressedImage() {
        compressedImageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
        compressedSizeTextView.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));
        uploadThumbUserImg(compressedImage);
        Toast.makeText(this, "Compressed image save in " + compressedImage.getPath(), Toast.LENGTH_LONG).show();
        Log.d("Compressor", "Compressed image save in " + compressedImage.getPath());
    }

    private void clearImage() {
        actualImageView.setBackgroundColor(getRandomColor());
        compressedImageView.setImageDrawable(null);
        compressedImageView.setBackgroundColor(getRandomColor());
        compressedSizeTextView.setText("Size : -");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                showError("Failed to open picture!");
                return;
            }
            try {
                actualImage = FileUtil.from(this, data.getData());
                actualImageView.setImageBitmap(BitmapFactory.decodeFile(actualImage.getAbsolutePath()));
                actualSizeTextView.setText(String.format("Size : %s", getReadableFileSize(actualImage.length())));
                clearImage();
            } catch (IOException e) {
                showError("Failed to read picture data!");
                e.printStackTrace();
            }
        }
    }
    private Bitmap decodeFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

        File destFile = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".png");
        try {
            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }
    public void uploadThumbUserImg(File compressedImage) {
        if(observerthumb_user!=null){
            observerthumb_user.cleanTransferListener();
        }

        Bitmap bmp = decodeFile(compressedImage);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        if(decoded.getWidth()>1200)
        {
            decoded.compress(Bitmap.CompressFormat.JPEG, 50, out);
        }
        else {
            decoded.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }


        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(out.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        observerthumb_user = transferUtility.upload("dailydeedclient", destination.getName(),
                destination);


        if(progressDialog!=null){
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Picture Uploading");
        progressDialog.show();
        progressDialog.setCancelable(true);

        String file_key = observerthumb_user.getKey();
        observerthumb_user.setTransferListener(new UploadListenerThumb(file_key));



    }
    private class UploadListenerThumb implements TransferListener {
        String file_key_name = "";
        public UploadListenerThumb( String file_key ) {
            this.file_key_name = file_key;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("err1:", e.toString());
            if(progressDialog!=null) {
                progressDialog.dismiss();
            }
            observerthumb_user.cleanTransferListener();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.e("state1: ",newState.toString());
            if(newState.toString().equals("COMPLETED")) {

                if(progressDialog!=null) {
                    progressDialog.dismiss();
                }
//                Log.e("key: ",(String) transferRecordMaps.get(pos).get("key"));

//                Log.e("url1: ","https://s3-ap-southeast-1.amazonaws.com/beleadr/"+file_key_name);
              String  imagee = "https://s3.amazonaws.com/dailydeedclient/"+file_key_name;
                Log.e("imageeee",imagee);
                observerthumb_user.cleanTransferListener();
//                try {
//                    uploadimage();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private int getRandomColor() {
        Random rand = new Random();
        return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
