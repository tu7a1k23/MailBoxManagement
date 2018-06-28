package com.funix.prm391x.se00437x.mailbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText mEdtEmail; //type in user
    private EditText mEdtPassword;  //type in password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Sign in");    //title on the top
        mEdtEmail = (EditText) findViewById(R.id.edt_email);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        setLoginBtnClick();
    }
    //logic when click login
    private void setLoginBtnClick() {
        Button mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEdtEmail.getText().toString().trim();
                String password = mEdtPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {    //test empty condition
                    Toast.makeText(LoginActivity.this,
                            "Email and password cannot be empty", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(LoginActivity.this, InboxActivity.class);
                    intent.putExtra(Key.EMAIL, email);  //Key.EMAIL presents the key of email
                    intent.putExtra(Key.PASSWORD, password);    // Key.PASSWORD is the same to Key.EMAIL
                    startActivity(intent); //Start intent
                }
            }
        });
    }
}
