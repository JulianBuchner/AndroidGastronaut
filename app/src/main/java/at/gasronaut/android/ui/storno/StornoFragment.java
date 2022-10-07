package at.gasronaut.android.ui.storno;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import at.gasronaut.android.databinding.FragmentStornoBinding;

public class StornoFragment extends Fragment {

    private FragmentStornoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StornoViewModel stornoViewModel =
                new ViewModelProvider(this).get(StornoViewModel.class);

        binding = FragmentStornoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textStorno;
        stornoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}