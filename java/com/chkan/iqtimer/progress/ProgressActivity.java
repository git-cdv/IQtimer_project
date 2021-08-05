package com.chkan.iqtimer.progress;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.chkan.iqtimer.R;
import com.chkan.iqtimer.dialogs.DialogOnLock;
import com.chkan.iqtimer.dialogs.DialogProgressDeleteGoal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProgressActivity extends AppCompatActivity {

    private static final String TAG = "MYLOGS";
    DialogFragmentGoal dlgSetGoal;
    DialogProgressDeleteGoal dlgDelGoal;
    DialogOnLock dlgOnLock;
    NavController navController;
    ProgressViewModel mViewmodel;
    private BillingClient mBillingClient;
    private final Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mViewmodel = new ViewModelProvider(this).get(ProgressViewModel.class);
}

    @Override
    public boolean onSupportNavigateUp() {
        //изменяет поведение при нажати на стрелку "UP" - здесь идет по стеку, а не на главную
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void toClickProgress(View v) {

        if(v.getId()==R.id.img_btn_add_goal||v.getId()==R.id.btnNewGoal){
            dlgSetGoal = new DialogFragmentGoal();
            dlgSetGoal.show(getSupportFragmentManager(), "dlgSetGoal");
            mViewmodel.isPutAdd.set(true);
        } else if (v.getId()==R.id.img_btn_cancel_goal){
            dlgDelGoal= new DialogProgressDeleteGoal();
            dlgDelGoal.show(getSupportFragmentManager(), "dlgDelGoal");
            mViewmodel.isPutAdd.set(false);
        } else if (v.getId()==R.id.BtnMore){
            toProgressList();
        } else if (v.getId()==R.id.img_btn_lock){
            if (mBillingClient==null){initBilling();}
            dlgOnLock = new DialogOnLock();
            dlgOnLock.show(getSupportFragmentManager(), "dlgOnLock");

        }
    }

    private void initBilling() {

        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    //сюда мы попадем когда будет осуществлена покупка
                    //purchases - список покупок сделанные пользователем через приложение (содержит sku, purchaseToken и isAcknowledged атрибуты)

                    //Проверка и подтверждение покупки
                    //Если ваше приложение не подтвердит покупку в течение 72 часов, пользователю будет возвращена сумма,
                    // и он больше не будет иметь доступа к первоначальной покупке, которую он совершил
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                    Log.d(TAG, "onPurchasesUpdated: payComplete");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    Log.d(TAG, "onPurchasesUpdated: Cancel");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    // возвращает если эта покупка уже есть
                    Toast.makeText(ProgressActivity.this, getString(R.string.already_owned), Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "onPurchasesUpdated: ITEM_ALREADY_OWNED");
                } else {
                    // Handle any other error codes.
                    Log.d(TAG, "onPurchasesUpdated: Error");
                }
            }
        };
        //основной интерфейс для связи с Google Play Billing Library
        mBillingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();

        //стартуем подключение к сервису
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "startConnection: Connected");
                    // сюда приходим после удачного подключения
                    //здесь мы можем запросить информацию о товарах и покупках
                    //Прежде чем предлагать товар на продажу, убедитесь, что пользователь еще не владеет этим товаром.
                    querySkuDetails(); //запрос о товарах
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "startConnection: Disconnected");
                // сюда приходим если не удалось подключиться
                Toast.makeText(ProgressActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
            }
        });

        }

    void handlePurchase(Purchase purchase) {
        Log.d(TAG, "handlePurchase");
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                if(billingResult.getResponseCode()== BillingClient.BillingResponseCode.OK){
                    Toast.makeText(ProgressActivity.this, getString(R.string.purchase_ack), Toast.LENGTH_SHORT).show();
                }
            }

        };


        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "PurchaseState - PURCHASED");
            payComplete();//выполняем активацию Премиум

            if (!purchase.isAcknowledged()) {
                Log.d(TAG, "PurchaseState - isAcknowledged");
                //подтверждение для нерасходуемых товаров
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private void payComplete() {
    //TODO сдлеать активацию Достижений
        //TODO настроить проверку покупок и активацию при первом посещении


    }

    private void querySkuDetails() {

        List<String> skuList = new ArrayList<>();
        skuList.add("sku_access_achivements");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        //получаем данные о товаре и кладем их в Мар
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                            }

                        }
                    }
                });
    }



    public void toProgressList(){
    navController.navigate(R.id.action_progressFragment_to_listProgressFragment);
}
    //вызывается для отображения окна покупки и в setSkuDetails указывваем какой товар показывать
    public void launchBilling(String skuId) {
        Log.d(TAG, "launchBilling");
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mSkuDetailsMap.get(skuId))
                .build();
        mBillingClient.launchBillingFlow(this, billingFlowParams);
    }


}