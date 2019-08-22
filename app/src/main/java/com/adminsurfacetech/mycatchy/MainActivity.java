package com.adminsurfacetech.mycatchy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.adminsurfacetech.mycatchy.Common.Common;
import com.adminsurfacetech.mycatchy.Retrofit.IMyRestaurantAPI;
import com.adminsurfacetech.mycatchy.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    private static final int APP_REQUEST_CODE = 1234;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser()
    {
        Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                builder.build());
        startActivityForResult(intent,APP_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE)
        {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null)
            {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }
            else if(loginResult.wasCancelled())
            {
                toastMessage = "Login Cancelled";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }else
            {
                //Login success
              //  Toast.makeText(this, "Success Login", Toast.LENGTH_SHORT).show();
                dialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,
                                account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(userModel -> {

                                    // if user is in database
                                    if (userModel.isSuccess())
                                    {
                                    Common.currentUser = userModel.getResult().get(0);
                                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                    finish();
                                    }
                                    else // if user is not in database
                                    {
                                        startActivity(new Intent(MainActivity.this,UpdateInfoActivity.class));
                                        finish();
                                    }
                                    dialog.dismiss();
                                        },
                                        throwable -> {
                                    dialog.dismiss();
                                            Toast.makeText(MainActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                        );
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(MainActivity.this, "[ACCOUT KIT ERROR]"+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }
}
