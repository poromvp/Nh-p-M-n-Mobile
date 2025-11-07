package com.example.bai_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class EditActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> pickImageLauncher;
    protected ImageView avt;
    Uri imgUri;
    protected TextView change_avt;
    protected EditText name;
    protected EditText email;
    protected Button save;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        LanguageManager langManager = new LanguageManager(this);
        langManager.setLocale(langManager.getLanguage()); // áp dụng ngôn ngữ đã lưu

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent=getIntent();

        avt=findViewById(R.id.imgAvatar);

        String uriString = intent.getStringExtra("img");
        if(uriString != null){
            imgUri = Uri.parse(uriString);
            try {
                InputStream inputStream = getContentResolver().openInputStream(imgUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                avt.setImageBitmap(bitmap);
                inputStream.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri originalUri = result.getData().getData();
                    imgUri = copyUriToInternalStorage(originalUri);
                    avt.setImageURI(imgUri);
                }
            }
        );

        avt.setOnClickListener(v->{
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            pickImageLauncher.launch(i);
        });

        change_avt=findViewById(R.id.tvChangeAvatar);
        change_avt.setOnClickListener(v->{
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            pickImageLauncher.launch(i);
        });

        name=findViewById(R.id.editName);
        name.setText(intent.getStringExtra("name"));

        email=findViewById(R.id.editEmail);
        email.setText(intent.getStringExtra("email"));

        save=findViewById(R.id.btnSave);
        save.setOnClickListener(v->{

            // Lưu dữ liệu vào SharedPreferences
            getSharedPreferences("UserData", MODE_PRIVATE)
                    .edit()
                    .putString("name", name.getText().toString())
                    .putString("email", email.getText().toString())
                    .putString("avatar", imgUri.toString())
                    .apply();


            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name.getText().toString());
            resultIntent.putExtra("email", email.getText().toString());
            if (imgUri != null) resultIntent.putExtra("img", imgUri.toString());



            setResult(RESULT_OK, resultIntent); // trả dữ liệu
            Snackbar.make(findViewById(android.R.id.content),
                    "Đã thay đổi thành công",
                    Snackbar.LENGTH_SHORT).show();

            finish();
        });

    }

    public Uri copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "temp_avatar.png");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();
            return Uri.fromFile(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
