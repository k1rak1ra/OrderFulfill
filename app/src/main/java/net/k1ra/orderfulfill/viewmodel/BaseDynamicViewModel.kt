package net.k1ra.orderfulfill.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.orderfulfill.model.LoadingState

open class BaseDynamicViewModel : ViewModel() {
    //Basic state-encompassing MutableLiveData emitted by the ViewModel that the Fragment listens to
    val mainLoadingState = MutableLiveData(LoadingState.LOADING)
    val errorText = MutableLiveData("")
    val loadingText = MutableLiveData("")
    val errorActionCallback = MutableLiveData(Runnable{})

    /**
     * Sets an error state with the specified error string and resolution action (thing to run to try the action again)
     * Action can either be executed now or only when the user presses "try again"
     */
    fun setErrorWithAction(text: String, execActionNow: Boolean, action: Runnable) = viewModelScope.launch(
        Dispatchers.Main) {
        errorText.value = text
        errorActionCallback.value = action
        mainLoadingState.value = LoadingState.ERROR

        if (execActionNow)
            action.run()
    }

    /**
     * Sets a loading state with the specified string
     */
    fun setLoading(text: String) = viewModelScope.launch(Dispatchers.Main) {
        loadingText.value = text
        mainLoadingState.value = LoadingState.LOADING
    }

    /**
     * Sets a loading completed state
     */
    fun setComplete() = viewModelScope.launch(Dispatchers.Main) {
        mainLoadingState.value = LoadingState.COMPLETE
    }
}