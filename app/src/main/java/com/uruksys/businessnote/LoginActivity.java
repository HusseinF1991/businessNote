package com.uruksys.businessnote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText etxtPassword , etxtUserName;
    CheckBox ckBoxKeepSignedIn;
    Button btnSignIn , btnForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_LoginActivity);
        toolbar.setTitle("businessNote");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        etxtPassword = findViewById(R.id.etxtPassword);
        etxtUserName = findViewById(R.id.etxtUserName);
        ckBoxKeepSignedIn = findViewById(R.id.ckBoxKeepSignedIn);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnForgetPassword = findViewById(R.id.btnForgetPassword);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("BusinessNoteSharedPrefereces", Context.MODE_PRIVATE);
                String userName = sharedPref.getString("BusinessNoteUserNameSharedPrefereces" , "");
                String password = sharedPref.getString("BusinessNotePasswordSharedPrefereces" , "");

                if(etxtUserName.getText().toString().equals(userName) && etxtPassword.getText().toString().equals(password)){

                    SharedPreferences.Editor editor = sharedPref.edit();
                    if(ckBoxKeepSignedIn.isChecked()){
                        editor.putBoolean("BusinessNoteKeepSignedInSharedPrefereces", true);
                        editor.commit();
                    }
                    else{
                        editor.putBoolean("BusinessNoteKeepSignedInSharedPrefereces", false);
                        editor.commit();
                    }

                    finish();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, "اسم المستخدم او الرمز السري المستخدم المدخل غير صحيح" , Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
