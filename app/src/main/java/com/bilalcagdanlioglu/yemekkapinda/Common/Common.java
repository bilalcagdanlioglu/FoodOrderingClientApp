package com.bilalcagdanlioglu.yemekkapinda.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bilalcagdanlioglu.yemekkapinda.Model.User;
import com.bilalcagdanlioglu.yemekkapinda.Remote.APIService;
import com.bilalcagdanlioglu.yemekkapinda.Remote.GoogleRetrofitClient;
import com.bilalcagdanlioglu.yemekkapinda.Remote.IGoogleService;
import com.bilalcagdanlioglu.yemekkapinda.Remote.RetrofitClient;

public class Common {
    public static User currentUser;

    public static final String INTENT_FOOD_ID = "FoodId";

    private static final String BASE_URL ="https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL ="https://maps.googleapis.com/";

    public static APIService getFCMService(){
        return RetrofitClient.getClient( BASE_URL ).create( APIService.class );
    }

    public static IGoogleService getGoogleMapAPI(){
        return GoogleRetrofitClient.getGoogleClient( GOOGLE_API_URL ).create( IGoogleService.class );
    }

    public static final String DELETE= "Delete";
    public static final String USER_KEY= "User";
    public static final String PWD_KEY= "Password";


    public static String convertCodeToStatus(String status) {
        if(status.equals( "0" )){
            return "Sipariş Alındı. Hazırlanıyor";
        }
        else if (status.equals( "1" )) {
            return "Yolda";
        }
        else{
            return "Teslim edildi";
        }
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        if(connectivityManager != null)
        {
            NetworkInfo[] info =connectivityManager.getAllNetworkInfo();
            if(info !=null)
            {
                for(int i=0;i<info.length;i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
