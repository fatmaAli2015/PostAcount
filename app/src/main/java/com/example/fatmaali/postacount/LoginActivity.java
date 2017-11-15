package com.example.fatmaali.postacount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_SignIn;
    private EditText edt_Email, edt_Password;
    private TextView txt_SignUp;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            Intent LoginIntent = new Intent(LoginActivity.this, MainActivity.class);
            LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(LoginIntent);
        }
        progressDialog = new ProgressDialog(this);
        edt_Email = (EditText) findViewById(R.id.editEmail);
        edt_Password = (EditText) findViewById(R.id.editPassword);
        btn_SignIn = (Button) findViewById(R.id.btn_SignIn);
        txt_SignUp = (TextView) findViewById(R.id.textViewSignUp);

        btn_SignIn.setOnClickListener(this);
        txt_SignUp.setOnClickListener(this);
    }

    public void onBackPressed() {
        super.onBackPressed();
        LoginActivity.this.finish();
    }

    private void userLogin() {
        String email = edt_Email.getText().toString().trim();
        String password = edt_Password.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Checking Login....");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            final String customer_id = firebaseAuth.getCurrentUser().getUid();
                            final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(customer_id);
                            current_user_db.keepSynced(true);
                            current_user_db.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(customer_id)) {
                                        current_user_db.setValue(true);
                                        finish();
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);
                                    } else {
                                        Intent SignUpIntent = new Intent(LoginActivity.this, SetUpActivity.class);
                                        SignUpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(SignUpIntent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "You need to create An account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == btn_SignIn) {
            userLogin();
        }
        if (view == txt_SignUp) {
            finish();
            startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
        }
    }
}
