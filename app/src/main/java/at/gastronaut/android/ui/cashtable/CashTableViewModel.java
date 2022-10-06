package at.gastronaut.android.ui.cashtable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CashTableViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CashTableViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Tisch kassieren");
    }

    public LiveData<String> getText() {
        return mText;
    }
}