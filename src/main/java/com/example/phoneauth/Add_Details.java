package com.example.phoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Add_Details extends AppCompatActivity {

    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    EditText Name,email,password;
    Button submit;
    String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__details);
        Name = findViewById(R.id.editText);
        email = findViewById(R.id.editText2);
        password = findViewById(R.id.editText3);
        submit = findViewById(R.id.button);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userid = fauth.getCurrentUser().getUid();

        final DocumentReference docref = fstore.collection("users").document(userid);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Name.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password.getText().toString().isEmpty())
                {
                    String name = Name.getText().toString();
                    String Email = email.getText().toString();
                    String Password = password.getText().toString();

                    Map<String,Object> user = new HashMap<>();
                    user.put("Name",name);
                    user.put("Email",Email);
                    user.put("Password",Password);

                    docref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                               startActivity(new Intent(getApplicationContext(),MainActivity.class));
                               finish();
                            }else
                            {
                                Toast.makeText(Add_Details.this,"Data is not inserted",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(Add_Details.this,"All Fields Are requreid",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }
}
