package com.adminsurfacetech.mycatchy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adminsurfacetech.mycatchy.Common.Common;
import com.adminsurfacetech.mycatchy.Retrofit.IMyRestaurantAPI;
import com.adminsurfacetech.mycatchy.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class SplashScreen extends AppCompatActivity {

    private static final String TAG = "AuthViewModel";
    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {


                            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                @Override
                                public void onSuccess(Account account) {

                                    dialog.show();
                                    compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY, account.getId())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(userModel -> {

                                                        Log.d(TAG, "AuthViewModel: viewmodel is working in splash screen...");

                                                        if (userModel.isSuccess()) // if user is in database
                                                        {
                                                            Log.d(TAG, "AuthViewModel: userModel is working in splash screen...");
                                                            Common.currentUser = userModel.getResult().get(0);
                                                            Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else // if user isnt available yet send them back to update activity
                                                        {
                                                            Intent intent = new Intent(SplashScreen.this, UpdateInfoActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }


                                                        dialog.dismiss();
                                                    },
                                                    throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(SplashScreen.this, "[GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));


                                }


                                @Override
                                public void onError(AccountKitError accountKitError) {
                                    Toast.makeText(SplashScreen.this, "Not sign in! Please sign in !", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(SplashScreen.this, "You must accept this permission to use this app", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();



    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }

}
