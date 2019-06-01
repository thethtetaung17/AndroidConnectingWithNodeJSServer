package com.intellex.connectwithnodejs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.intellex.connectwithnodejs.Retrofit.IMyService;
import com.intellex.connectwithnodejs.Retrofit.RetrofitClient;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    TextView txtRegister;
    MaterialEditText edtEmail, edtPassword;
    Button btnLogin;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    @Override
    protected void onStop() {
        compositeDisposable. clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);

        edtEmail = (MaterialEditText) findViewById(R.id.edtEmail);
        edtPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View registerLayout = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.register_layout, null);

                new MaterialStyledDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.met_ic_clear)
                        .setTitle("REGISTRATION")
                        .setDescription("Please fill all fields")
                        .setCustomView(registerLayout)
                        .setNegativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("REGISTER")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MaterialEditText edtRegisterEmail = (MaterialEditText) registerLayout.findViewById(R.id.registerEmail);
                                MaterialEditText edtRegisterName = (MaterialEditText) registerLayout.findViewById(R.id.registerName);
                                MaterialEditText edtRegisterPassword = (MaterialEditText) registerLayout.findViewById(R.id.registerPassword);
                                if(TextUtils.isEmpty(edtRegisterEmail.getText().toString())){
                                    Toast.makeText(MainActivity.this, "Email cannot be empty",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(TextUtils.isEmpty(edtRegisterName.getText().toString())){
                                    Toast.makeText(MainActivity.this, "Name cannot be empty",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(TextUtils.isEmpty(edtRegisterPassword.getText().toString())){
                                    Toast.makeText(MainActivity.this, "Password cannot be empty",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                registerUser(edtRegisterEmail.getText().toString(),
                                        edtRegisterName.getText().toString(),
                                        edtRegisterPassword.getText().toString());
                            }
                        }).show();
            }
        });
    }

    private void registerUser(String email, String name, String password) {
        compositeDisposable.add(iMyService.registerUser(email,name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_LONG).show();
                    }
                }));

    }

    private void loginUser(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email cannot be empty",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password cannot be empty",Toast.LENGTH_LONG).show();
            return;
        }

        compositeDisposable.add(iMyService.loginUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_LONG).show();
                    }
                }));

    }
}
