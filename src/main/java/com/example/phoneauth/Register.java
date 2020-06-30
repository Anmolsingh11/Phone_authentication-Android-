package com.example.phoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    FirebaseAuth fauth;
    FirebaseFirestore firestore;
    EditText phoneNumber,codeEnter;
    Button nextbtn;
    ProgressBar progressBar;
    TextView state;
    CountryCodePicker codePicker;
    String verificationid ;
    PhoneAuthProvider.ForceResendingToken token;
    Boolean VerificationinProgress = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fauth = FirebaseAuth.getInstance();
        phoneNumber = findViewById(R.id.phone);
        codeEnter = findViewById(R.id.codeEnter);
        progressBar = findViewById(R.id.progressBar);
        nextbtn = findViewById(R.id.nextBtn);
        state = findViewById(R.id.state);
        codePicker = findViewById(R.id.ccp);
        firestore = FirebaseFirestore.getInstance();

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!VerificationinProgress)
               {
                   if( !phoneNumber.getText().toString().isEmpty() && phoneNumber.getText().toString().length() == 10)
                   {
                       String phonenum = "+"+codePicker.getSelectedCountryCode() + phoneNumber.getText().toString();
                       Log.d(TAG,"onclick: Phone No -> "+ phonenum);
                       state.setText("Sending OTP");
                       progressBar.setVisibility(View.VISIBLE);
                       state.setVisibility(View.VISIBLE);
                       requestotp(phonenum);
                   }else
                   {
                       phoneNumber.setError("phone number is not valid");
                   }
               }else
               {
                   String userOTP = codeEnter.getText().toString();
                   if(!userOTP.isEmpty() & userOTP.length() == 6)
                   {
                       PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid,userOTP);
                       verifyauth(credential);
                   }else
                   {
                       codeEnter.setError("Valid OTP is required");
                   }
               }
            }
        });
    }
    @Override
    protected void onStart(){
        super.onStart();

        if(fauth.getCurrentUser() != null)
        {
            progressBar.setVisibility(View.VISIBLE);
            state.setText("Checking...");
            state.setVisibility(View.VISIBLE);
            checkUserProfile();
        }
    }



    private void verifyauth(PhoneAuthCredential credential) {
        fauth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    checkUserProfile();

                }else
                {
                    Toast.makeText(Register.this,"Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkUserProfile() {
        DocumentReference docref = firestore.collection("users").document(fauth.getCurrentUser().getUid());
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }else
                {
                    Intent intent = new Intent(Register.this,Add_Details.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void requestotp(String phonenum) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenum, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                verifyauth(phoneAuthCredential);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                progressBar.setVisibility(View.GONE);
                state.setVisibility(View.GONE);
                codeEnter.setVisibility(View.VISIBLE);
                verificationid  = s;
                token = forceResendingToken;
                nextbtn.setText("Verify");
                VerificationinProgress = true;

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(getApplicationContext(),"OTP Expired, Re - Request the OTP",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(Register.this,"Cannot create Account"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
