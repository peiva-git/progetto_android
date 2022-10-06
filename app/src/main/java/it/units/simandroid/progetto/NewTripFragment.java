package it.units.simandroid.progetto;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class NewTripFragment extends Fragment {

    private ActivityResultLauncher<PickVisualMediaRequest> pickTripImages;
    public static final int MAX_NUMBER_OF_IMAGES = 10;
    public static final String IMAGE_PICKER_TAG = "IMG_PICK";

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickTripImages = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(MAX_NUMBER_OF_IMAGES), uris -> {
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
            pickTripImages.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        return view;
    }
}