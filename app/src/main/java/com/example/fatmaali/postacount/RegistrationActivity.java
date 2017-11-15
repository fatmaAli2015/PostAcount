package com.example.fatmaali.postacount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private Button btn_Register;
    private EditText edt_Email, edt_Password, edUsername;
    private TextView txt_SignIn;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private static String TAG = "MAIN_ACTIVITY";
    private static int RC_SIGN_IN = 0;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        firebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("AUTH", "user logged in: " + user.getEmail());
                    finish();
                    Intent RegIntent = new Intent(getApplicationContext(), MainActivity.class);
                    RegIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(RegIntent);
                } else
                    Log.d("AUTH", "user logged out.");
            }
        };
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        progressDialog = new ProgressDialog(this);

        btn_Register = (Button) findViewById(R.id.btn_Register);
        edUsername = (EditText) findViewById(R.id.editName);
        edt_Email = (EditText) findViewById(R.id.editEmail);
        edt_Password = (EditText) findViewById(R.id.editPassword);
        txt_SignIn = (TextView) findViewById(R.id.textViewSignIn);


        btn_Register.setOnClickListener(this);
        txt_SignIn.setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onBackPressed() {
        super.onBackPressed();
        RegistrationActivity.this.finish();
    }

    private void registerUser() {
        final String email = edt_Email.getText().toString().trim();
        final String password = edt_Password.getText().toString().trim();
        final String username = edUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(getApplicationContext(), "please enter username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Registering Please Wait....");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String customer_id = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(customer_id);
                            current_user_db.keepSynced(true);
                            current_user_db.setValue(true);
                            current_user_db.child("username").setValue(username);
                            current_user_db.child("email").setValue(email);
                            current_user_db.child("password").setValue(password);
                            current_user_db.child("image").setValue("default");
                            finish();
                            progressDialog.dismiss();
                            Intent LoginIntent = new Intent(getApplicationContext(), MainActivity.class);
                            LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(LoginIntent);
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(getApplicationContext(), "Failed Registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            return;
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else
                Log.d(TAG, "Google Login Failed");

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("AUTH", "signInWithCredential:on complete: " + task.isSuccessful());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == btn_Register) {
            registerUser();
        }
        if (view == txt_SignIn) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        if (view.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
            firebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed.");

    }
}
