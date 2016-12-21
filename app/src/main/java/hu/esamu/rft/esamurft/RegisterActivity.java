package hu.esamu.rft.esamurft;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button mButtonSignup;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private UserInformation userInformation;

	
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
		
        firebaseAuth = FirebaseAuth.getInstance();
		 //getting the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();
		
        userInformation = new UserInformation();

        editTextEmail = (EditText) findViewById(R.id.editTextEmail) ;
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    goMainScreen();
                }
            }
        };
        mButtonSignup = (Button) findViewById(R.id.buttonSignup);
        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        progressDialog = new ProgressDialog(this);


    }
    private void registerUser(){

        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();



        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            finish();
							//saving data to firebase database
							//first we are creating a new child in firebase with the
							//unique id of logged in user
							//and then for that user under the unique id we are saving data
							//for saving data we are using setvalue method this method takes a normal java object
                            databaseReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(userInformation);
                            LoginActivity.signedIn=true;
                            goMainScreen();
                        }else{
                            //display some message here
                            Toast.makeText(RegisterActivity.this,"Registration Error",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void goMainScreen(){
        Intent intent = new Intent(this, ControlActivity.class);
        intent.addFlags( intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );

    }
}
