package it.units.simandroid.progetto.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.ShareTripPagerAdapter;

public class ShareTripFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public ShareTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_share_trip, container, false);
        viewPager = fragmentView.findViewById(R.id.share_trip_pager);
        tabLayout = fragmentView.findViewById(R.id.share_trip_tab_layout);
        List<Fragment> fragments = new ArrayList<>(2);
        fragments.add(new SelectUsersFragment());
        fragments.add(new SelectedUsersFragment());
        ShareTripPagerAdapter adapter = new ShareTripPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.choose_users);
                    break;
                case 1:
                    tab.setText(R.string.chosen_users);
                    break;
                default:
                    break;
            }
        }).attach();
        return fragmentView;
    }
}