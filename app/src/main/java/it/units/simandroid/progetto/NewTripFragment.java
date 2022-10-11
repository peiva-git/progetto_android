package it.units.simandroid.progetto;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class NewTripFragment extends Fragment {

    private ActivityResultLauncher<String> pickTripImages;
    public static final int MAX_NUMBER_OF_IMAGES = 10;
    public static final String IMAGE_PICKER_TAG = "IMG_PICK";

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PickMultipleVisualMedia contract seems to be buggy, even when implemented according to docs
        pickTripImages = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (!uris.isEmpty()) {
                Log.d(IMAGE_PICKER_TAG, "Picked " + uris.size() + " items");
            } else {
                Log.d(IMAGE_PICKER_TAG, "No media selected");
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_trip, container, false);
        ImageButton newImageButton = view.findViewById(R.id.trip_images_button);

        newImageButton.setOnClickListener(imageView -> {
            pickTripImages.launch("image/*");
        });

        return view;
    }
}