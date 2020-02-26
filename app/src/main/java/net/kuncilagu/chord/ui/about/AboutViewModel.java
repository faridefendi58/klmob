package net.kuncilagu.chord.ui.about;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import net.kuncilagu.chord.R;

public class AboutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AboutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Version : "+ R.string.app_version);
    }

    public LiveData<String> getText() {
        return mText;
    }
}
