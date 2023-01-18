package comp5216.sydney.edu.au.customerapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {


    private EditText emailTextField;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        emailTextField = (EditText) findViewById(R.id.emailTextField);

        // get data from intent if there is any
        Intent data = getIntent();
        email = data.getStringExtra("email");
        if (email != null) {
            emailTextField.setText(email);
        }
    }

    public void OnCancel(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
        builder.setTitle(R.string.dialog_cancel_resetpassword_title)
                .setMessage(R.string.dialog_cancel_resetpassword_msg)
                .setPositiveButton(R.string.Yes, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Prepare data intent for sending it back
                                Intent data = new Intent();

                                email = emailTextField.getText().toString();
                                data.putExtra("email", email);

                                // Activity finishes OK, return the data
                                setResult(RESULT_CANCELED, data); // Set result code and bundle data for response
                                finish(); // Close the activity, pass data to parent
                            }
                        })
                // User cancelled the dialog and nothing happens
                .setNegativeButton(R.string.No, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
        builder.create().show();
    }

    public void onResetPassword(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
        builder.setTitle(R.string.dialog_resetpassword_title)
                .setMessage(R.string.dialog_resetpassword_msg)
                .setPositiveButton(R.string.Yes, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Prepare data intent for sending it back
                                Intent data = new Intent();
                                email = emailTextField.getText().toString();
                                data.putExtra("email", email);

                                // Activity finishes OK, return the data
                                setResult(RESULT_OK, data); // Set result code and bundle data for response
                                finish(); // Close the activity, pass data to parent

                            }
                        })
                // User cancelled the dialog and nothing happens
                .setNegativeButton(R.string.No, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
        builder.create().show();
    }
}
