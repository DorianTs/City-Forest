package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class EditorHomeActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;
    private Button editor_panel_butt;
    private Button home_butt;
    private TextView select_action_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_home);

        select_action_txt = (TextView)findViewById(R.id.selectActionTxt);

        editor_panel_butt = (Button)findViewById(R.id.editorPanelButt);
        home_butt = (Button)findViewById(R.id.homeButt);

        editor_panel_butt.setOnClickListener(new ClickListener());
        home_butt.setOnClickListener(new ClickListener());
    }

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == editor_panel_butt.getId()){
                Intent i = new Intent(EditorHomeActivity.this, EditorPanelActivity.class);
                startActivity(i);
            }
            if(v.getId() == home_butt.getId()){
                Intent i = new Intent(EditorHomeActivity.this, Home.class);
                startActivity(i);
            }
        }
    }

    /*In order to be able to sign out from the logged in account, I have to
    * check who is the logged in user.*/
    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String welcome_message = "Welcome back, " + userName;
        select_action_txt.setText(welcome_message);
        super.onStart();
    }








    /*Exit app dialog if the user clicked back button*/
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_app_title)
                .setMessage(R.string.dialog_exit_app_body)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Exits the application*/
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.signOut:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Method signs out user's google account*/
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(EditorHomeActivity.this, SignInActivity.class);
                        startActivity(i);
                    }});
    }
}
