package hu.esamu.rft.esamurft;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by ShadowJabtko on 2016.12.20..
 */

public class FirebaseSender {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private boolean connectedToFirebase;

    public FirebaseSender(){
        firebaseAuth =FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        if (firebaseUser !=null){
            connectedToFirebase =true;
        }
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    connectedToFirebase =false;
                }
            }
        };

        firebaseAuth.addAuthStateListener(authListener);
    }

    public boolean uploadPicture(Uri photoUri){
        final boolean[] succes = {false};
        StorageReference filepath = storageReference.child("Photos").child(photoUri.getLastPathSegment());
        filepath.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                succes[0] =true;
            }
        });

        return succes[0];
    }

    public boolean isConnected(){
        return connectedToFirebase;
    }

}
