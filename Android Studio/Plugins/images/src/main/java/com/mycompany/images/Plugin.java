package com.mycompany.images;

import com.unity3d.player.UnityPlayer;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by mopsicus.ru on 22.04.16.
 */
public class Plugin {

    static void launch() {
        FragmentManager fragmentManager = UnityPlayer.currentActivity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(new ImagesFragment(), "images_fragment");
        fragmentTransaction.commit();
    }
}
