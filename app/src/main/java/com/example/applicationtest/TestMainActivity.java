package com.example.applicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.intune.mam.client.app.MAMComponents;
import com.microsoft.intune.mam.policy.MAMEnrollmentManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestMainActivity extends AppCompatActivity {
    private MAMEnrollmentManager mEnrollmentManager;

    public static final String[] MSAL_SCOPES = {"https://graph.microsoft.com/User.Read"};

    private AppAccount mUserAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEnrollmentManager = MAMComponents.get(MAMEnrollmentManager.class);
        mUserAccount = AppSettings.getAccount(this);
        boolean isOffline = MAMComponents.isAppOffline();
        Log.d("zpb","isOffline"+isOffline);
        boolean isManager = MAMComponents.isManagedApp(this);
        Log.d("zpb","isManager="+isManager);
        Log.d("zpb","account is null "+(mUserAccount == null));
        Log.d("zpb","mEnrollmentManager class="+(mEnrollmentManager.getClass().getSimpleName()));
        if (mUserAccount != null){
            Intent secordActivity = new Intent(TestMainActivity.this,SecondActivity.class);
            startActivity(secordActivity);
            return;
        }

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.d("zpb","hash key="+something);
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLogin();
            }
        });
    }

    private void  clickLogin(){
        Thread thread = new Thread(() -> {
            try {
                String loginHint = null;
                if (mUserAccount != null) {
                    loginHint = mUserAccount.getUPN();
                }
                MSALUtil.acquireToken(TestMainActivity.this, MSAL_SCOPES, loginHint, new AuthCallback());
            } catch (MsalException | InterruptedException e) {
                Log.e("zpb","error ="+(e.getMessage()));
                e.printStackTrace();
            }
        });
        thread.start();
    }

    class AuthCallback implements AuthenticationCallback {
        @Override
        public void onError(final MsalException exc) {
            Log.d("zpb","error"+(exc.getMessage()));
            exc.printStackTrace();
        }

        @Override
        public void onSuccess(final IAuthenticationResult result) {
            IAccount account = result.getAccount();

            final String upn = account.getUsername();
            final String aadId = account.getId();
            final String tenantId = account.getTenantId();
            final String authorityURL = account.getAuthority();

            String message = "Authentication succeeded for user " + upn;

            MAMEnrollmentManager mEnrollmentManager = MAMComponents.get(MAMEnrollmentManager.class);
            Log.d("zpb","mEnrollmentManager class="+(mEnrollmentManager.getClass().getSimpleName()));
            // Register the account for MAM.
            mEnrollmentManager.registerAccountForMAM(upn, aadId, tenantId, authorityURL);
            mUserAccount = new AppAccount(upn, aadId, tenantId, authorityURL);
            AppSettings.saveAccount(getApplicationContext(), mUserAccount);
            Intent secordActivity = new Intent(TestMainActivity.this,SecondActivity.class);
            startActivity(secordActivity);
        }

        @Override
        public void onCancel() {
        }
    }
}
