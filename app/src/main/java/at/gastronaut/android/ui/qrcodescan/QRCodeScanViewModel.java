package at.gastronaut.android.ui.qrcodescan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QRCodeScanViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public QRCodeScanViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Login");
    }

    public LiveData<String> getText() {
        return mText;
    }
}