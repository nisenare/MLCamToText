package com.example.mlcamtotext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button openCameraButton;
    private ImageView capturedImage;
    private EditText textMultiline;
    private File tempImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openCameraButton = findViewById((R.id.openCameraButton));
        capturedImage = findViewById(R.id.capturedImage);
        textMultiline = findViewById(R.id.textMultiline);

        openCameraButton.setOnClickListener(view -> {
            Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (openCamera.resolveActivity(getPackageManager()) != null) {
                try {
                    createFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (tempImage != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.mlcamtotext.fileprovider", tempImage);
                    openCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    cameraResultLaunch.launch(openCamera);
                }
            }
        });
    }

    private ActivityResultLauncher<Intent> cameraResultLaunch = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        detectTextFromImage();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    capturedImage.setImageURI(Uri.fromFile(tempImage));
                }
            }
    });

    private void detectTextFromImage() throws IOException {
        Uri tempImageUri = Uri.parse(tempImage.toURI().toString());
        InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), tempImageUri);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(inputImage)
            .addOnSuccessListener(visionText -> {
                String result1 = visionText.getText();
                textMultiline.setText(result1);
            })
            .addOnFailureListener(e -> {
                throw new RuntimeException(e);
            });
    }

    private void createFile() throws IOException {
        if (tempImage != null) {
            tempImage.delete();
            tempImage = null;
        }

        Context context = getApplicationContext();
        tempImage = File.createTempFile(
            "temporaryPhoto",
            ".jpg",
            context.getCacheDir()
        );
    }

}