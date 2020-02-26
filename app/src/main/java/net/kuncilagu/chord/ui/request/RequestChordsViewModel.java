package net.kuncilagu.chord.ui.request;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RequestChordsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RequestChordsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is request chords fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
