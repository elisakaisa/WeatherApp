package com.example.lab1.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectivityVM extends ViewModel {


    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();

    public Boolean getIsConnected() {
        return isConnected.getValue();
    }

    public void setIsConnected(Boolean connected) {
        isConnected.setValue(connected);
    }
}
