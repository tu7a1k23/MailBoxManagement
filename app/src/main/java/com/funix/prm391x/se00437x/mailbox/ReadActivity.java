package com.funix.prm391x.se00437x.mailbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class ReadActivity extends AppCompatActivity {
    private Context mContext = this;
    private TextView mTxvFrom;  //From info typed in
    private TextView mTxvSubject;  //Subject info typed in
    private WebView mTxvContent;    //show up the content of mail
    private ProgressDialog mProgressDialog; //show up the progress dialog when loading
    private String mEmail;  // user of your account
    private String mPassword;   // password of your account
    private String mFrom;   // from info of mail
    private String mReplyTo;    //reply to info of mail
    private String mSubject;    //subject info of mail
    private int mMessageNo;     //the index of message
    private String mContent;    //content of a message comprise...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        setTitle("Mail");

        mTxvFrom = (TextView) findViewById(R.id.txv_from);
        mTxvSubject = (TextView) findViewById(R.id.txv_subject);
        mTxvContent = (WebView) findViewById(R.id.wbv_message);

        // Get info from previous activity
        Intent intent = getIntent();
        mEmail = intent.getStringExtra(Key.EMAIL);
        mPassword = intent.getStringExtra(Key.PASSWORD);
        mFrom = intent.getStringExtra(Key.FROM);
        mSubject = intent.getStringExtra(Key.SUBJECT);
        mMessageNo = intent.getIntExtra(Key.MESSAGE_NUMBER, 0);
        mReplyTo = intent.getStringExtra(Key.REPLY_TO);

        // Load email content
        new ContentGetter().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_in_read, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_reply:
                replyEmail();
        }
        return super.onOptionsItemSelected(item);
    }

    private void replyEmail() {
        Intent intent = new Intent(mContext, ComposeActivity.class);
        intent.putExtra(Key.EMAIL, mEmail);
        intent.putExtra(Key.PASSWORD, mPassword);
        intent.putExtra(Key.REPLY_TO, mReplyTo);
        startActivity(intent);
    }

    private class ContentGetter extends AsyncTask<Void, Void, Boolean> {
        String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(mContext,
                    "Please wait...", "Getting email content", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //get the session object
                Session emailSession = Session.getInstance(new Properties());

                //create the store object and connect with the IMAP server
                Store store = emailSession.getStore("imaps");
                store.connect("imap.gmail.com", mEmail, mPassword);

                //create the folder object and open it
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                //retrieve the content from the message
                mContent = Email.getText(inbox.getMessage(mMessageNo));

                //close the store and folder object
                inbox.close(false);
                store.close();

            } catch (MessagingException e) {
                e.printStackTrace();
                errorMessage = e.toString();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = e.toString();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            mTxvFrom.setText(mFrom);
            mTxvSubject.setText(mSubject);
            mTxvContent.loadData(mContent, "text/html; charset=utf-8", "utf-8");
            mProgressDialog.dismiss();
            Toast.makeText(mContext, (isSuccessful ? "Done!" : errorMessage),
                    Toast.LENGTH_LONG).show();
        }
    }
}
