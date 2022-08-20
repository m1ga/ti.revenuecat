package ti.revenuecat;

import androidx.annotation.NonNull;

import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreTransaction;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import java.util.HashMap;
import java.util.List;


@Kroll.module(name = "TiRevenuecat", id = "ti.revenuecat")
public class TiRevenuecatModule extends KrollModule {

    List<Package> availablePackages;
    String TAG = "TiRevenuecat";
    public TiRevenuecatModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
    }

    // Methods
    @Kroll.method
    public void init(KrollDict dict) {
        boolean debugging = dict.getBoolean("debug");
        String apiKey = dict.getString("apiKey");
        Purchases.setDebugLogsEnabled(debugging);
        Purchases.configure(new PurchasesConfiguration.Builder(TiApplication.getAppCurrentActivity(), apiKey).build());
    }

    // Methods
    @Kroll.method
    public void offerings() {
        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                if (offerings.getCurrent() != null) {
                    KrollDict kd = new KrollDict();
                    HashMap obj = new HashMap();
                    availablePackages = offerings.getCurrent().getAvailablePackages();
                    for (Package item : availablePackages) {
                        obj.put("name", item.toString());
                        obj.put("identifier", item.getIdentifier());
                        obj.put("offering", item.getOffering());
                        obj.put("productDescription", item.getProduct().getDescription());
                        obj.put("productPrice", item.getProduct().getPrice());
                        obj.put("productTitle", item.getProduct().getTitle());
                    }
                    kd.put("items", obj);
                    fireEvent("offerings", kd);
                }
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                // An error occurred
                KrollDict kd = new KrollDict();
                kd.put("error", error.getMessage());
                fireEvent("error", kd);
            }
        });
    }

    @Kroll.method
    public void restorePurchases() {
        Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                KrollDict kd = new KrollDict();
                kd.put("entitlements", customerInfo.getEntitlements().toString());
                fireEvent("restorePurchase", kd);
                Log.i(TAG, customerInfo.getEntitlements().toString());
            }

            @Override
            public void onError(@NonNull PurchasesError purchasesError) {
                KrollDict kd = new KrollDict();
                kd.put("error", purchasesError.getMessage());
                fireEvent("error", kd);
            }
        });
    }

    @Kroll.method
    public void purchase(String identifier) {
        Package aPackage = null;
        for (Package allPackages : availablePackages) {
            if (allPackages.getIdentifier() == identifier) {
                aPackage = allPackages;
            }
        }
        if (aPackage == null) {
            Log.e(TAG, "No package found");
            return;
        }
        Purchases.getSharedInstance().purchasePackage(
                TiApplication.getAppCurrentActivity(),
                aPackage,
                new PurchaseCallback() {
                    @Override
                    public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {
                        if (customerInfo.getEntitlements().get(identifier).isActive()) {
                            // Unlock that great "pro" content
                            KrollDict kd = new KrollDict();
                            kd.put("identifier", identifier);
                            fireEvent("success", kd);
                        }
                    }

                    @Override
                    public void onError(@NonNull PurchasesError purchasesError, boolean b) {
                        KrollDict kd = new KrollDict();
                        kd.put("error", purchasesError.getMessage());
                        fireEvent("error", kd);
                    }
                }
        );

    }

}

