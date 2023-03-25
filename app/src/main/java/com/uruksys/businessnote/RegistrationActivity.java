package com.uruksys.businessnote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    EditText etxtNewUserName,etxtNewPassword,etxtConfirmNewPassword,etxtActivationKey;
    Button btnBuildRegistrationKey,btnCreateNewUser;
    TextView txtRegistrationKey;

    String secretKey_Reg = "BusinessNote_Reg";
    String secretKey_Act = "BusinessNote_Act";

    public final int MY_PERMISSION_For_3_REQUESTS = 93;

    String newUserName , newPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etxtNewUserName = findViewById(R.id.etxtNewUserName);
        etxtNewPassword = findViewById(R.id.etxtNewPassword);
        etxtConfirmNewPassword = findViewById(R.id.etxtConfirmNewPassword);
        etxtActivationKey = findViewById(R.id.etxtActivationKey);
        btnBuildRegistrationKey = findViewById(R.id.btnBuildRegistrationKey);
        btnCreateNewUser = findViewById(R.id.btnCreateNewUser);
        txtRegistrationKey = findViewById(R.id.txtRegistrationKey);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_RegistrationActivity);
        toolbar.setTitle("التسجيل");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);


        CheckIfUserAlreadyExists();
        BuildRegistrationKey();
        CreateNewUser();



        txtRegistrationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager cm = (ClipboardManager)RegistrationActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(txtRegistrationKey.getText());
                Toast.makeText(RegistrationActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
            }
        });



        getPermissionsForApp();
    }




    private void getPermissionsForApp() {
        if
        (
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {

            // Should we show an explanation?
            if (
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this
                        , new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , MY_PERMISSION_For_3_REQUESTS);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , MY_PERMISSION_For_3_REQUESTS);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_For_3_REQUESTS:
                for (int i = 0; i < permissions.length; i++) {
                    Log.d("myPermission", permissions[i] + " ," + grantResults[i]);
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    }
                }
                break;
        }
    }



    private void CheckIfUserAlreadyExists(){

        SharedPreferences sharedPref = RegistrationActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
        String password = sharedPref.getString("BusinessNotePasswordSharedPrefereces" , "");
        boolean keepSignedIn = sharedPref.getBoolean("BusinessNoteKeepSignedInSharedPrefereces" , false);
        if(!password.equals("") && !keepSignedIn){
            finish();
            Intent intent = new Intent(RegistrationActivity.this , LoginActivity.class);
            startActivity(intent);
        }
        else if(!password.equals("") && keepSignedIn){
            finish();
            Intent intent = new Intent(RegistrationActivity.this , MainActivity.class);
            startActivity(intent);
        }
    }


    private void BuildRegistrationKey(){
        btnBuildRegistrationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etxtNewUserName.getText().toString().trim().equals("")){
                    Toast.makeText(RegistrationActivity.this , "رجاءا ادخل اسم المستخدم",Toast.LENGTH_SHORT).show();
                }
                else if(etxtNewPassword.getText().toString().trim().length() < 6){

                    Toast.makeText(RegistrationActivity.this , "رجاءا ادخل رمز السري لا يقل عن 6 مراتب",Toast.LENGTH_SHORT).show();
                }
                else if(etxtNewPassword.getText().toString().trim().equals("")){

                    Toast.makeText(RegistrationActivity.this , "رجاءا ادخل الرمز السري",Toast.LENGTH_SHORT).show();
                }
                else if(!etxtConfirmNewPassword.getText().toString().trim().equals(etxtNewPassword.getText().toString().trim())){

                    Toast.makeText(RegistrationActivity.this , "رجاءا تاكد من ادخالك الرمز السري مرتين صحيحا",Toast.LENGTH_SHORT).show();
                }
                else{
                    String android_id = Settings.Secure.getString(RegistrationActivity.this.getContentResolver(),
                            Settings.Secure.ANDROID_ID);

                    newUserName = etxtNewUserName.getText().toString().trim();
                    newPassword = etxtNewPassword.getText().toString().trim();
                    String stringToEncrypt = newUserName + newPassword + android_id;
                    String registrationKey = AES.encrypt(stringToEncrypt , secretKey_Reg);
                    txtRegistrationKey.setText(registrationKey);

                    btnCreateNewUser.setEnabled(true);
                }
            }
        });
    }


    private void CreateNewUser(){
        btnCreateNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txtRegistrationKey.getText().toString().equals("")){
                    if(etxtActivationKey.getText().toString().equals("")){
                       Toast.makeText(RegistrationActivity.this, "رجاءا ادخل مفتاح التفعيل" , Toast.LENGTH_SHORT).show();
                    }
                    else if(txtRegistrationKey.getText().toString().equals("")){
                        Toast.makeText(RegistrationActivity.this, "رجاءا عملية انشاء مستخدم جديد" , Toast.LENGTH_SHORT).show();
                    }
                    else {

                        String android_id = Settings.Secure.getString(RegistrationActivity.this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        String stringToEncrypt = newUserName + newPassword + android_id + "Approved";
                        String activationKey = AES.encrypt(stringToEncrypt , secretKey_Act);

                        if(etxtActivationKey.getText().toString().equals(activationKey)){

                            SharedPreferences sharedPref = RegistrationActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("BusinessNoteUserNameSharedPrefereces", newUserName);
                            editor.putString("BusinessNotePasswordSharedPrefereces", newPassword);
                            editor.commit();

                            Toast.makeText(RegistrationActivity.this, "تم التسجيل بنجاح" , Toast.LENGTH_SHORT).show();

                            finish();
                            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(RegistrationActivity.this, "مفتاح التوليد غير صحيح" , Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
}
