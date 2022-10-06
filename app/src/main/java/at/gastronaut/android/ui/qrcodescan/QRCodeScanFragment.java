package at.gastronaut.android.ui.qrcodescan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import at.gastronaut.android.databinding.FragmentLoginBinding;
import at.gastronaut.android.databinding.FragmentQrcodescanBinding;

public class QRCodeScanFragment extends Fragment {

    private FragmentQrcodescanBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        QRCodeScanViewModel loginViewModel =
                new ViewModelProvider(this).get(QRCodeScanViewModel.class);

        binding = FragmentQrcodescanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textQrcodescan;
        loginViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}