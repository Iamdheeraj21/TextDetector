package com.example.textdetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import static android.Manifest.permission.CAMERA;

public class ScannerActivity extends AppCompatActivity {

    ImageView capture;
    TextView textView;
    Button snapButton,detectButton;
    static final int REQUEST_IMAGE_CAPTURE=100;
    Bitmap bitmap;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        initViews();
        snapButton.setOnClickListener(v->{
            if(checkPermissions()){
                captureImage();
            }else {
                requestPermissions();
            }
        });
        detectButton.setOnClickListener(v->{
            detectText();
        });
    }

    private boolean checkPermissions(){
        int cameraPermission= ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission== PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions(){
        int PERMISSION_CODE=200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERMISSION_CODE);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void captureImage(){
        Intent takePicture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePicture,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission=grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                captureImage();
            }else {
                Toast.makeText(this, "Permission denied.....", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras=data.getExtras();
        bitmap=(Bitmap) extras.get("data");
        capture.setImageBitmap(bitmap);
    }

    private void detectText (){
        InputImage image= InputImage.fromBitmap(bitmap,0);
        TextRecognizer recognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> textTask=recognizer.process(image).addOnSuccessListener(text -> {
            StringBuilder result=new StringBuilder();
            for (Text.TextBlock block:text.getTextBlocks()){
                String blockText=block.getText();
                Point[] blockPoints=block.getCornerPoints();
                Rect lineRect=block.getBoundingBox();

                for (Text.Line line:block.getLines()){
                    String lineText=line.getText();
                    Point[] linePointCorners=line.getCornerPoints();
                    Rect linRect=line.getBoundingBox();
                    for (Text.Element element:line.getElements()){
                        String elementText=element.getText();
                        result.append(elementText);
                    }
                    textView.setText(blockText);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure (@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews (){
        capture=findViewById(R.id.captureImage);
        snapButton=findViewById(R.id.snapButton);
        detectButton=findViewById(R.id.detectButton);
        textView=findViewById(R.id.resultHeading);
    }


}