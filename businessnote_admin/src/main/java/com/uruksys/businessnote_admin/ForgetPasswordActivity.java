package com.uruksys.businessnote_admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ForgetPasswordActivity extends AppCompatActivity {


    Button btnConfirmMyPassword,btnResetPassword;
    EditText etxtMyPassword,etxtMyUserName;
    TextView txtPartOfPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ForgetPasswordActivity);
        toolbar.setTitle("businessNote");
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        txtPartOfPassword = findViewById(R.id.txtPartOfPassword);
        btnConfirmMyPassword = findViewById(R.id.btnConfirmMyPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        etxtMyPassword = findViewById(R.id.etxtMyPassword);
        etxtMyUserName = findViewById(R.id.etxtMyUserName);



        SharedPreferences sharedPref = ForgetPasswordActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
        String password = sharedPref.getString("BusinessNoteAdminPasswordSharedPrefereces" , "");

        String partOfPassword = password.substring(0,1) +"***********";
        txtPartOfPassword.setText(partOfPassword);

        CheckPasswordEntry();
        ResetAccount();
    }



    private void CheckPasswordEntry(){
        btnConfirmMyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = ForgetPasswordActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                String userName = sharedPref.getString("BusinessNoteAdminUserNameSharedPrefereces" , "");
                String password = sharedPref.getString("BusinessNoteAdminPasswordSharedPrefereces" , "");

                if(etxtMyUserName.getText().toString().equals(userName) && etxtMyPassword.getText().toString().equals(password))
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPasswordActivity.this);
                    builder.setMessage(password)
                            .setTitle("تم استرجاع الرمز السري الخاص بك")
                            .setPositiveButton("الغاء", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    Toast.makeText(ForgetPasswordActivity.this , "الرمز السري او اسم المستخدم المدخل غير صحيح" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void ResetAccount(){
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPasswordActivity.this);
                builder.setMessage("سيتم حذف الحساب الحالي لتسجيل حساب جديد, للاستمرار اضغط موافق")
                        .setPositiveButton("موافق", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                SharedPreferences sharedPref = ForgetPasswordActivity.this.getSharedPreferences("BusinessNoteAdminSharedPrefereces", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("BusinessNoteAdminUserNameSharedPrefereces", "");
                                editor.putString("BusinessNoteAdminPasswordSharedPrefereces", "");
                                editor.commit();

                                finish();
                                Intent intent = new Intent(ForgetPasswordActivity.this , RegistrationActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}
