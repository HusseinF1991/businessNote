package com.uruksys.businessnote_reg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText etxtGenerationKey;
    Button btnGenerateKey;
    TextView txtActivationKey;

    String secretKey_Reg = "BusinessNote_Reg";
    String secretKey_Act = "BusinessNote_Act";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtActivationKey = findViewById(R.id.txtActivationKey);
        btnGenerateKey = findViewById(R.id.btnGenerateKey);
        etxtGenerationKey = findViewById(R.id.etxtGenerationKey);



        txtActivationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(txtActivationKey.getText());
                Toast.makeText(MainActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
            }
        });


        btnGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etxtGenerationKey.getText().toString().trim().equals("")){
                    String decryptedGenerationKey = AES.decrypt(etxtGenerationKey.getText().toString() , secretKey_Reg);

                    String decryptedActivationKey = decryptedGenerationKey + "Approved";
                    String activationKey = AES.encrypt(decryptedActivationKey , secretKey_Act);
                    txtActivationKey.setText(activationKey);
                }
                else {
                    Toast.makeText(MainActivity.this, "رجاءا ادخل مفتاح التوليد", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
