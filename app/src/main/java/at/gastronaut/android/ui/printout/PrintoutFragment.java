package at.gastronaut.android.ui.printout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import at.gastronaut.android.databinding.FragmentPrintoutBinding;

public class PrintoutFragment extends Fragment {

    private FragmentPrintoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PrintoutViewModel printoutViewModel =
                new ViewModelProvider(this).get(PrintoutViewModel.class);

        binding = FragmentPrintoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPrintout;
        printoutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}