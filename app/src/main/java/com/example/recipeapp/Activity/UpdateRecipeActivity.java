package com.example.recipeapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.recipeapp.Model.FoodData;
import com.example.recipeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UpdateRecipeActivity extends AppCompatActivity {
    ImageView recipeImage;
    Uri uri;
    EditText txt_name,txt_description,txt_price;
    String imageUrl;
    String key,oldImageUrl;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String recipename,recipeDescription,recipePrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        recipeImage = findViewById(R.id.iv_foodImage);
        txt_name = findViewById(R.id.txt_recipe_name);
        txt_description = findViewById(R.id.text_description);
        txt_price = findViewById(R.id.text_price);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            Glide.with(UpdateRecipeActivity.this)
                    .load(bundle.getString("oldimageUrl"))
                    .into(recipeImage);
            txt_name.setText(bundle.getString("recipeNameKey"));
            txt_description.setText(bundle.getString("descriptionKey"));
            txt_price.setText(bundle.getString("priceKey"));
            key = bundle.getString("key");
            oldImageUrl = bundle.getString("oldimageUrl");
        }


        databaseReference = FirebaseDatabase.getInstance().getReference("Recipe").child(key);



    }

    public void btnSelectImage(View view) {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            uri = data.getData();
            recipeImage.setImageURI(uri);

        }
        else Toast.makeText(this, "You haven't picked image", Toast.LENGTH_SHORT).show();

    }


    public void btnUpdateRecipe(View view) {
      if (uri!=null){
          recipename = txt_name.getText().toString().trim();
          recipeDescription = txt_description.getText().toString().trim();
          recipePrice = txt_price.getText().toString();

          final ProgressDialog progressDialog = new ProgressDialog(this);
          progressDialog.setMessage("Recipe Uplading....");
          progressDialog.show();
          storageReference = FirebaseStorage.getInstance()
                  .getReference().child("RecipeImage").child(uri.getLastPathSegment());
          storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {

              Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
              while(!uriTask.isComplete());
              Uri urlImage = uriTask.getResult();
              imageUrl = urlImage.toString();
              uploadRecipe();
              progressDialog.dismiss();
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  progressDialog.dismiss();
              }
          });

      }
      else {
          Toast.makeText(this, "Select Image ", Toast.LENGTH_SHORT).show();
      }


    }

    public void uploadRecipe(){



        FoodData foodData = new FoodData(
                recipename,
                recipeDescription,
                recipePrice,
                imageUrl
        );


        databaseReference.setValue(foodData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                StorageReference storageReferenceNew  = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                storageReferenceNew.delete();
                Intent intent=new Intent(UpdateRecipeActivity.this,MainActivity.class);
                startActivity(intent);
                Toast.makeText(UpdateRecipeActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
            }
        });



    }
}