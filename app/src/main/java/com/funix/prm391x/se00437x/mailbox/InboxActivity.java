package com.funix.prm391x.se00437x.mailbox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mail.util.MailConnectException;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class InboxActivity extends AppCompatActivity {
    private Context mCtx = this;    //context of app: activity or service...
    private ProgressDialog mProgressDialog; //create progress thing to show up when pending to load new activity.
    private String mEmail;  //user for login
    private String mPassword; //password for login
    private ArrayList<Email> mMessages = new ArrayList<>(); //the amount of mail/message
    private ListView mMailList; //ListView to load each of mail message in inbox
    private CustomAdapter mCustomAdapter; //help ListView to get data
    private LayoutInflater mInflater; //convert file XML into view.
    private Button mBtnMoreMessage; //button to load more messages
    private int mLoadedCount = 0; //the amount of message have been loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        setTitle("Inbox");
        mInflater = LayoutInflater.from(mCtx);

        // get email and password from previous activity
        Intent intent = getIntent();
        mEmail = intent.getStringExtra(Key.EMAIL); //take out the key presents for EMAIL in Key class
        mPassword = intent.getStringExtra(Key.PASSWORD); ////take out the key presents for PASSWORD in Key class

        setUpMailList();    //set up the message list
        addFooterToMailList();  // footer show the message left and button to load left messages.
        setMailListItemClick(); //set information to your clicked item in mail list

        // fetch email
        new MailFetcher().execute();
    }

    private void setUpMailList() {
        mCustomAdapter = new CustomAdapter();
        mMailList = (ListView) findViewById(R.id.mail_list);
        mMailList.setAdapter(mCustomAdapter);
    }

    @SuppressLint("InflateParams")
    private void addFooterToMailList() {
        View footer = mInflater.inflate(R.layout.mail_footer, null, false);
        mBtnMoreMessage = (Button) footer.findViewById(R.id.btn_more_message);
        mBtnMoreMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MailFetcher().execute();
            }
        });
        mMailList.addFooterView(footer);
    }

    private void setMailListItemClick() {
        mMailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mCtx, ReadActivity.class);
                Email email = mMessages.get(position);
                intent.putExtra(Key.EMAIL, mEmail);
                intent.putExtra(Key.PASSWORD, mPassword);
                intent.putExtra(Key.FROM, email.getFrom());
                intent.putExtra(Key.SUBJECT, email.getSubject());
                intent.putExtra(Key.MESSAGE_NUMBER, email.getMessageNumber());
                intent.putExtra(Key.REPLY_TO, email.getAddress());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_in_inbox, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_compose:
                Intent intent = new Intent(this, ComposeActivity.class);
                intent.putExtra(Key.EMAIL, mEmail);
                intent.putExtra(Key.PASSWORD, mPassword);
                startActivity(intent);
                break;
            case R.id.btn_refresh:
                mMessages.clear();
                mLoadedCount = 0;
                new MailFetcher().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setTitle("Confirm logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Stay",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Logout",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(mCtx, LoginActivity.class));
                            }
                        });
        alertBuilder.create().show();
    }

    private class MailFetcher extends AsyncTask<Void, Void, Boolean> {
        String mErr;
        private int mMessagesLeft;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(mCtx,
                    "Please wait...", "Checking inbox folder", true, false);
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

                //retrieve the messages from the folder (10 messages a time)
                Message[] messages;
                int messageCount = inbox.getMessageCount();
                mMessagesLeft = messageCount - mLoadedCount;
                if (mMessagesLeft > 10) {
                    messages = inbox.getMessages(mMessagesLeft - 9, mMessagesLeft);
                    mLoadedCount += 10;
                } else if (mMessagesLeft > 0) {
                    messages = inbox.getMessages(1, mMessagesLeft);
                    mLoadedCount = messageCount;
                } else {
                    messages = new Message[0];
                }
                mMessagesLeft = messageCount - mLoadedCount;

                for (int i = messages.length - 1; i >= 0; i--) {
                    mMessages.add(new Email(messages[i]));
                }

                //5) close the store and folder object
                inbox.close(false);
                store.close();

            } catch (MessagingException e) {
                setErrorMessage(e);
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            mCustomAdapter.notifyDataSetChanged();
            mBtnMoreMessage.setText(mMessagesLeft + " messages left");
            mProgressDialog.dismiss();
            Toast.makeText(mCtx, (isSuccessful ? "Done!" : mErr),
                    Toast.LENGTH_LONG).show();
            if (!isSuccessful) {
                startActivity(new Intent(mCtx, LoginActivity.class));
            }
            super.onPostExecute(isSuccessful);
        }

        private void setErrorMessage(Exception e) {
            if (e instanceof AuthenticationFailedException) {
                String err = e.toString();
                if (err.contains("Invalid credentials")) {
                    mErr = "The password you entered might be incorrect";
                } else if (err.contains("Please log in")) {
                    mErr = "You have to allow less secure apps first";
                } else if (err.contains("Lookup failed")) {
                    mErr = "The email you entered might be incorrect";
                } else {
                    mErr = err.substring(42);
                }
            } else if (e instanceof MailConnectException) {
                mErr = "Couldn't connect to host. Your internet connection" +
                        " might not be in the right state";
            } else {
                mErr = "Unknown Error";
            }
        }
    }

    private static class ViewHolder {
        TextView mTxvSubject;
        TextView mTxvFrom;
        TextView mTxvReceivedDate;
    }

    private class CustomAdapter extends ArrayAdapter<Email> {
        CustomAdapter() {
            super(mCtx, R.layout.mail_row, mMessages);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mail_row, parent, false);
                holder = new ViewHolder();
                holder.mTxvSubject = (TextView) convertView.findViewById(R.id.txv_subject_row);
                holder.mTxvFrom = (TextView) convertView.findViewById(R.id.txv_from_row);
                holder.mTxvReceivedDate = (TextView) convertView.findViewById(R.id.txv_received_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Email email = mMessages.get(position);
            holder.mTxvSubject.setText(email.getSubject());
            holder.mTxvFrom.setText(email.getFrom());
            holder.mTxvReceivedDate.setText(email.getReceivedDate());
            return convertView;
        }
    }
}
