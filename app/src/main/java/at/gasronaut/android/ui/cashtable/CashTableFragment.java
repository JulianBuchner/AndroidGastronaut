package at.gasronaut.android.ui.cashtable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import at.gasronaut.android.databinding.FragmentCashtableBinding;

public class CashTableFragment extends Fragment {

    private FragmentCashtableBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CashTableViewModel cashtableViewModel =
                new ViewModelProvider(this).get(CashTableViewModel.class);

        binding = FragmentCashtableBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCashtable;
        cashtableViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}