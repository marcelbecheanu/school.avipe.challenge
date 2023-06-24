package com.avipe.manager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.avipe.screens.Dashboard;
import com.avipe.screens.Notifications;
import com.avipe.screens.Predictions;
import com.avipe.screens.Profile;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0 :
                return new Dashboard();
            case 1 :
                return new Predictions();
            case 2 :
                return new Notifications();
            case 3 :
                return new Profile();
        }

        return new Dashboard();
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}