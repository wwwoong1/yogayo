package com.d104.yogaapp.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.event.AuthEvent
import com.d104.domain.event.AuthEventManager
import com.d104.domain.model.UserCourse
import com.d104.domain.usecase.GetLoginStatusUseCase
import com.d104.domain.usecase.GetYogaPosesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val reducer: MainReducer,
    private val authEventManager: AuthEventManager,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val getYogaPosesUseCase:GetYogaPosesUseCase
) : ViewModel(){

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    // 네비게이션 이벤트를 위한 SharedFlow
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> {
                // 로그인이 필요한 탭인지 확인
                if ((intent.tab == Tab.Multi || intent.tab == Tab.MyPage) && !isLoggedIn()) {
                    // 로그인 화면으로 네비게이션 이벤트 발행
                    viewModelScope.launch {
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                    return
                }
            }

            else -> {}
        }
        val newState = reducer.reduce(state.value, intent)
        _state.value = newState
    }

    private fun isLoggedIn(): Boolean {
        return runBlocking {
            val value = getLoginStatusUseCase().first()
            Timber.d("isLoggedIn: $value")
            value
        }
    }

    init {
        // AuthEvent를 수집하여 NavigationEvent로 변환
        viewModelScope.launch {
            Timber.d("???")
            getLoginStatusUseCase().collectLatest { isLogin ->
                _state.update { it.copy(
                    selectedTab = Tab.Solo,
                    isLogin = isLogin
                ) }
            }


        }
        viewModelScope.launch {
            try {
                Timber.d("!!!")
                getYogaPosesUseCase().collectLatest { result ->
                    when {
                        result.isSuccess -> {
                            _state.update {
                                it.copy(
                                    yogaPoses = result.getOrDefault(emptyList()),
                                )
                            }
                            Timber.d("yogaPoses${_state.value.yogaPoses}")
                        }
                        result.isFailure -> {
                            Timber.d("Error: ${result.exceptionOrNull()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching yoga poses")
            }
        }
        viewModelScope.launch {
            authEventManager.authEvents.collect { authEvent ->
                when (authEvent) {
                    is AuthEvent.TokenRefreshFailed -> {
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                }
            }

        }

    }



}