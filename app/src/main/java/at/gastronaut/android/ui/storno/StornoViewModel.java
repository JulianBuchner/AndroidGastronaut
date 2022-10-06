package at.gastronaut.android.ui.storno;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StornoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public StornoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Storno");
    }

    public LiveData<String> getText() {
        return mText;
    }
}