package hu.esamu.rft.esamurft;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private LoginButton facebookButton;
    private CallbackManager callbackManager;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textview;

    private ProgressDialog progressDialog;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9002;

    private GoogleApiClient mGoogleApiClient;
    public static boolean signedIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        callbackManager = CallbackManager.Factory.create();

        facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        facebookButton.setReadPermissions(Arrays.asList("public_profile","email"));
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                } else {
                                    getInfoFromFacebookUser(me);
                                }
                            }
                        }).executeAsync();
                goMainScreen();

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.cancel_Login, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), R.string.error_Login, Toast.LENGTH_SHORT).show();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.google_button).setOnClickListener(this);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.registraiton_button).setOnClickListener(this);

        editTextEmail = (EditText)findViewById(R.id.user);
        editTextPassword = (EditText)findViewById(R.id.password);

        textview =(TextView) findViewById(R.id.textView5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void goMainScreen(){
        Intent intent = new Intent(this, ControlActivity.class);
        intent.addFlags( intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );

    }

    private void goRegistrationScreen(){
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.addFlags( intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_button:
                signIn();
                break;
            case R.id.login_button:
                userLogin();
                break;
            case R.id.registraiton_button:
                goRegistrationScreen();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            getInfoFromGoogleUser(result);
            // Signed in successfully, show authenticated UI.
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            LoginActivity.signedIn=true;
            signOut();
            goMainScreen();
        } else {

        }
    }

    private void signOut() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                    }
                });
    }

    private void getInfoFromGoogleUser(GoogleSignInResult result) {

        GoogleSignInAccount acct = result.getSignInAccount();
        String personName = acct.getDisplayName();
        String personGivenName = acct.getGivenName();
        String personFamilyName = acct.getFamilyName();
        String personEmail = acct.getEmail();
        String personId = acct.getId();
    }

    private void getInfoFromFacebookUser(JSONObject person) {
        String personEmail = person.optString("email");
        String personId = person.optString("id");
        String personName = person.optString("name");;
    }

    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter editTextPassword",Toast.LENGTH_LONG).show();
        }
        progressDialog.setMessage("Authentication is in progress,please wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            finish();
                            LoginActivity.signedIn=true;
                            goMainScreen();
                        }
                    }
                });
    }
}
