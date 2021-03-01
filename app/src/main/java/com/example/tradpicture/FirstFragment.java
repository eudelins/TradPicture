package com.example.tradpicture;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class FirstFragment extends Fragment {
    private Context context;

    private ImageView imageView;
    private Button buttonCamera;
    private Button buttonImport;
    private Button buttonConvertToPdf;
    private Button rotateImageButton;

    private TessOCR mTessOCR;
    private ProgressDialog mProgressDialog;
    private TextView textViewOCR;
    private String textOCR;

    private String currentPhotoPath;
    private Bitmap currentPhotoBitmap;
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int RESULT_LOAD_IMG = 1;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity().getApplicationContext();

        mTessOCR = new TessOCR(context, "eng");
        textViewOCR = view.findViewById(R.id.textOCR);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        createCameraButton(view);
        createImportButton(view);
        createButtonConvertToPdf(view);
        createRotateImageButton(view);
    }

    private void createButtonConvertToPdf(View view) {
        buttonConvertToPdf = view.findViewById(R.id.buttonConvertToPdf);
        buttonConvertToPdf.setEnabled(false);
        buttonConvertToPdf.setVisibility(View.INVISIBLE);
        buttonConvertToPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NavHostFragment.findNavController(FirstFragment.this)
                //        .navigate(R.id.action_FirstFragment_to_navMenu);
                doOCR(currentPhotoBitmap);
            }
        });
    }

    private void createRotateImageButton(View view) {
        rotateImageButton = view.findViewById(R.id.rotateImageButton);
        rotateImageButton.setEnabled(false);
        rotateImageButton.setVisibility(View.INVISIBLE);
        rotateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateBitmap((float) 90.0);
                imageView.setImageBitmap(currentPhotoBitmap);
            }
        });
    }


    /**
     * Save an image in the right directory
     * @return the file created for the image
     * @throws IOException creation failed
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    void rotateBitmap(float x)
    {
        // create new matrix
        Matrix matrix = new Matrix();

        // setup rotation degree
        matrix.postRotate(x);
        Bitmap bmp = Bitmap.createBitmap(currentPhotoBitmap, 0, 0, currentPhotoBitmap.getWidth(), currentPhotoBitmap.getHeight(), matrix, true);
        currentPhotoBitmap = bmp;
    }

    /**
     * Display the picture taken in the imageView
     */
    private void setPic() {

        currentPhotoBitmap =  BitmapFactory.decodeFile(currentPhotoPath);;
        rotateBitmap((float) 90.0);
        imageView.setImageBitmap(currentPhotoBitmap);

        // delete the temporary file that contains the image
        File imageFile = new File(currentPhotoPath);
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                System.out.println("file Deleted :" + currentPhotoPath);
            } else {
                System.out.println("file not Deleted :" + currentPhotoPath);
            }
        }
    }

    /**
     * Method which is called when a picture is taken
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null && resultCode == RESULT_OK) {
            setPic();
        } else if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                currentPhotoBitmap = selectedImage;
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, "Une erreur s'est produite", Toast.LENGTH_LONG).show();
            }
        }

        buttonConvertToPdf.setEnabled(true);
        buttonConvertToPdf.setVisibility(View.VISIBLE);
        rotateImageButton.setEnabled(true);
        rotateImageButton.setVisibility(View.VISIBLE);
    }

    public void createCameraButton(View view) {
        buttonCamera = (Button) view.findViewById(R.id.buttonCamera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //prepare intent
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(context, "errorFileCreate", Toast.LENGTH_SHORT).show();
                        Log.i("File error", ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.example.tradpicture.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }


                }
            }
        });
    }


    public void createImportButton(View view) {
        buttonImport = (Button) view.findViewById(R.id.buttonImport);
        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });
    }


    /**
     * Uses the TessOCR to read the text on an image and write it in a pdf file
     * @param bitmap the image that contains a text
     */
    private void doOCR(final Bitmap bitmap) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("OCR process dialog");
        mProgressDialog.setMessage("OCR processing...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        new Thread(new Runnable() {
            public void run() {
                textOCR = mTessOCR.getOCRResult(bitmap);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (textOCR != null && !textOCR.equals("")) {
                            textViewOCR.setText(textOCR);
                            FirstFragment.this.stringToPdf();
                        }

                        mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }


    /**
     * Write a string in a pdf file
     */
    private void stringToPdf() {
            try {
                Document document = new Document(PageSize.A4);

                File storageDir = getActivity().getExternalFilesDir("/files/");
                Date date = new Date() ;
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
                File pdfFile = new File(storageDir + timeStamp + "test.pdf");

                PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                pdfWriter.setPdfVersion(PdfWriter.VERSION_1_7);
                pdfWriter.setTagged();
                pdfWriter.setViewerPreferences(PdfWriter.DisplayDocTitle);
                document.addLanguage("en-US");
                document.addTitle("English pangram");
                pdfWriter.createXmpMetadata();

                document.open();
                // document.add(new Paragraph("Blabalanandqjdsndhj"));
                document.add(new Paragraph(textViewOCR.getText().toString()));
                document.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}