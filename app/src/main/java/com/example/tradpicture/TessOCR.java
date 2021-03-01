package com.example.tradpicture;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TessOCR {
    private static final String TAG = "Error : ";
    private final TessBaseAPI mTess;


    public TessOCR(Context context, String language) {
        mTess = new TessBaseAPI();
        boolean dstPathDirExistFlag = false;

        AssetManager assetManager = context.getAssets();

        String dstPathDir = "/tesseract/tessdata/";

        String trainedEnglishDataFile = "eng.traineddata";
        InputStream inFile = null;

        dstPathDir = context.getFilesDir() + dstPathDir;
        String dstInitPathDir = context.getFilesDir() + "/tesseract";
        String dstPathFile = dstPathDir + trainedEnglishDataFile;
        FileOutputStream outFile = null;

        try {
            inFile = assetManager.open(trainedEnglishDataFile);
            File f = new File(dstPathDir);

            if (!f.exists()) {
                if (!f.mkdirs()) {
                    Toast.makeText(context, trainedEnglishDataFile + " can't be created.", Toast.LENGTH_SHORT).show();
                }
                outFile = new FileOutputStream(new File(dstPathFile));
            } else {
                dstPathDirExistFlag = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        } finally {

            if (dstPathDirExistFlag) {
                try {
                    if (inFile != null) inFile.close();
                    mTess.init(dstInitPathDir, language);
                    return;

                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }

            if (inFile != null && outFile != null) {
                try {
                    //copy file
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inFile.read(buf)) != -1) {
                        outFile.write(buf, 0, len);
                    }
                    inFile.close();
                    outFile.close();
                    mTess.init(dstInitPathDir, language);
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            } else {
                Toast.makeText(context, trainedEnglishDataFile + " can't be read.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    public void onDestroy() {
        if (mTess != null) mTess.end();
    }

    public TessBaseAPI getMTess() {
        return mTess;
    }
}
