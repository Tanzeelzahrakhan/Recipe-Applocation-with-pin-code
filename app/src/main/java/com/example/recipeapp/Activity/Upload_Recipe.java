package com.example.recipeapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.recipeapp.Model.FoodData;
import com.example.recipeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Calendar;


public class Upload_Recipe extends AppCompatActivity {
    ImageView recipeImage,cameraImage,galleryImage;
    Uri uri;
    EditText txt_name,txt_description,txt_price;
    String imageUrl;
    Dialog dialog;
    private final int CAMERA_REQUEST_CODE=100;
    private final int GALLERY_CODE=200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_recipe);
        recipeImage = (ImageView)findViewById(R.id.iv_foodImage);
        txt_name = (EditText)findViewById(R.id.txt_recipe_name);
        txt_description = (EditText)findViewById(R.id.text_description);
        txt_price = (EditText)findViewById(R.id.text_price);

    }
    public void btnSelectImage(View view) {
        dialog=new Dialog(Upload_Recipe.this);
        dialog.setContentView(R.layout.selectimage);
        cameraImage=dialog.findViewById(R.id.camera);
        galleryImage=dialog.findViewById(R.id.gallary);
        dialog.show();
        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             startActivityForResult(intent,CAMERA_REQUEST_CODE);
             dialog.dismiss();}});
        galleryImage.setOnClickListener(view1 -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            startActivityForResult(photoPicker,1);
            dialog.dismiss();});}
    public Uri getImageUri(Context inContext, Bitmap inImage){
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
         if (requestCode==CAMERA_REQUEST_CODE){
             Bitmap urii=(Bitmap) (data.getExtras().get("data"));
             uri=getImageUri(Upload_Recipe.this,urii);
             recipeImage.setImageURI(uri);
         }
         else{
             uri = data.getData();
             recipeImage.setImageURI(uri);
         }}
        else Toast.makeText(this, "You haven't picked image", Toast.LENGTH_SHORT).show();}
    public void uploadImage(){

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("RecipeImage").child(uri.getLastPathSegment());
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Recipe Uplading....");
        progressDialog.show();
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                imageUrl = urlImage.toString();
                uploadRecipe();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });



    }


    public void btnUploadRecipe(View view) {

        uploadImage();

    }

    public void uploadRecipe(){



        FoodData foodData = new FoodData(
                txt_name.getText().toString(),
                txt_description.getText().toString(),
                txt_price.getText().toString(),
                imageUrl
        );

        String myCurrentDateTime = DateFormat.getDateTimeInstance()
                .format(Calendar.getInstance().getTime());

        FirebaseDatabase.getInstance().getReference("Recipe")
                .child(myCurrentDateTime).setValue(foodData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(Upload_Recipe.this, "Recipe Uploaded", Toast.LENGTH_SHORT).show();

                            finish();

                        }



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Upload_Recipe.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });



    }
}