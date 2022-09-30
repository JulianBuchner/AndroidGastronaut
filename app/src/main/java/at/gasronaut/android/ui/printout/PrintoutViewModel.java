package at.gasronaut.android.ui.printout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrintoutViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PrintoutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Testausdruck");
    }

    public LiveData<String> getText() {
        return mText;
    }
}