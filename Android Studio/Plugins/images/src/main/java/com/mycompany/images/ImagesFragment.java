package com.mycompany.images;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unity3d.player.UnityPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mopsicus.ru on 22.04.16.
 */
public class ImagesFragment extends Fragment {

    final FragmentManager fragmentManager = UnityPlayer.currentActivity.getFragmentManager();
    private static int GET_IMAGE = 101;
    private Uri outputFileUri, selectedImageUri;


    public void getImage () {
        Context context = UnityPlayer.currentActivity.getApplicationContext();
        final File root = new File(context.getExternalCacheDir() + File.separator + "images");
        root.mkdirs();
        final String fname = "image.jpg";
        final File imageDir = new File(root, fname);
        outputFileUri = Uri.fromFile(imageDir);
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = UnityPlayer.currentActivity.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, GET_IMAGE);
    }


    public void processResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == GET_IMAGE) {
            int rotate = 0;
            Matrix matrix = new Matrix();
            boolean isCamera;
            if (data == null)
                isCamera = true;
            else
                isCamera = (data.getAction() == null) ? false : (data.getAction().equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE));
            if (isCamera) {
                selectedImageUri = outputFileUri;
                rotate = getExifOrientation(outputFileUri.getPath());
            } else {
                selectedImageUri = data == null ? null : data.getData();
                rotate = getExifOrientation(getRealPathFromURI(selectedImageUri));
            }
            if (selectedImageUri != null){
                try {
                    Bitmap bit = BitmapFactory.decodeStream(UnityPlayer.currentActivity.getContentResolver().openInputStream(selectedImageUri));
                    if (rotate != 0) {
                        matrix.postRotate(rotate);
                        bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
                    }
                    float maxSize = 1024;
                    float width = bit.getWidth(), height = bit.getHeight();
                    if (width > maxSize) {
                        height *= maxSize / width;
                        width = maxSize;
                    }
                    if (height > maxSize) {
                        width *= maxSize / height;
                        height = maxSize;
                    }
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bit, (int) width, (int) height, true);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    Context context = UnityPlayer.currentActivity.getApplicationContext();
                    final File root = new File(context.getExternalCacheDir() + File.separator + "images");
                    root.mkdirs();
                    final String fname = "image.jpg";
                    final File f = new File(root, fname);
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UnityPlayer.UnitySendMessage("Plugins", "OnImageReceived", outputFileUri.getPath());
                fragmentManager.beginTransaction().remove(this).commit();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = UnityPlayer.currentActivity.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        processResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
