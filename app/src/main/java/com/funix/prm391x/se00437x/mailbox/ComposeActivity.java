package com.funix.prm391x.se00437x.mailbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ComposeActivity extends AppCompatActivity {
    private Context mContext = this;
    private EditText mEdtTo;
    private EditText mEdtSubject;
    private EditText mEdtMessage;
    private ProgressDialog mProgressDialog;
    private String mEmail;
    private String mPassword;
    private String mTo;
    private String mSubject;
    private String mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        setTitle("Compose");

        mEdtTo = (EditText) findViewById(R.id.edt_from);
        mEdtSubject = (EditText) findViewById(R.id.edt_subject);
        mEdtMessage = (EditText) findViewById(R.id.edt_message);

        // Get email and password from previous activity
        Intent intent = getIntent();
        mEmail = intent.getStringExtra(Key.EMAIL);
        mPassword = intent.getStringExtra(Key.PASSWORD);

        // pre-set reply address
        mEdtTo.setText(intent.getStringExtra(Key.REPLY_TO));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_in_compose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_send:
                sendEmail();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        mTo = mEdtTo.getText().toString();
        mSubject = mEdtSubject.getText().toString();
        mMessage = mEdtMessage.getText().toString();
        new SendTask().execute();
    }

    private class SendTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(mContext,
                    "Please wait...", "Sending message", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //Get properties object
            Properties props = new Properties();
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");

            //get Session
            Session session = Session.getInstance(props);

            //compose message
            try {
                MimeMessage message = new MimeMessage(session);
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(mTo));
                message.setSubject(mSubject);
                message.setText(mMessage);
                //send message
                Transport.send(message, mEmail, mPassword);
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            mProgressDialog.dismiss();
            Toast.makeText(mContext, (isSuccessful ? "Message sent" : "Failed to send email"),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
