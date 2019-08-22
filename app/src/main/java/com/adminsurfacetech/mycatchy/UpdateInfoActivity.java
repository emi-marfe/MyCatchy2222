package com.adminsurfacetech.mycatchy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adminsurfacetech.mycatchy.Common.Common;
import com.adminsurfacetech.mycatchy.Retrofit.IMyRestaurantAPI;
import com.adminsurfacetech.mycatchy.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class UpdateInfoActivity extends AppCompatActivity {

    private static final String TAG = "AuthViewModel";
    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.edt_user_name)
    EditText edt_user_name;
    @BindView(R.id.edt_user_address)
    EditText edt_user_address;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        ButterKnife.bind(this);
        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            finish();// close when they click back arrow in update activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        compositeDisposable.add(myRestaurantAPI.updateUserInfo(Common.API_KEY,
                                        account.getPhoneNumber().toString(),
                                        edt_user_name.getText().toString(),
                                        edt_user_address.getText().toString(),
                                        account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateUserModel -> {
                                            Log.d(TAG, "AuthViewModel: viewmodel is working in update111...");
                                    if (updateUserModel.isSuccess())
                                    {
                                        Log.d(TAG, "AuthViewModel: updateUserModel is working in update...");
                                        // refresh if it already has users before
                                        compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY, account.getId())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(userModel -> {

                                                   if (userModel.isSuccess())
                                                   {
                                                      // Common.currentUser = userModel.getResult().get(0);
                                                       //                                                       startActivity(new Intent(UpdateInfoActivity.this,HomeActivity.class));
                                                       //
                                                       //                                                       finish();
                                                       Common.currentUser = userModel.getResult().get(0);
                                                       Intent intent = new Intent(UpdateInfoActivity.this, HomeActivity.class);
                                                       startActivity(intent);
                                                       finish();

                                                   }
                                                   else
                                                   {
                                                       Toast.makeText(UpdateInfoActivity.this, "[GET USER RESULT]"+userModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                   }

                                                            dialog.dismiss();

                                                        },
                                                       throwable -> {
                                                           dialog.dismiss();
                                                           Toast.makeText(UpdateInfoActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                       })
                                        );
                                    }
                                    else
                                    {
                                        Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API RETURN]"+updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                        },
                                        throwable -> {
                                    dialog.dismiss();
                                            Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                        );
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(UpdateInfoActivity.this, "[ACCOUT KIT ERROR ]"+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        myRestaurantAPI  = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }
}
