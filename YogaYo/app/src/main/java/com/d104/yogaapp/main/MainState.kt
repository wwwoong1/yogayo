package com.d104.yogaapp.main

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.MyPageInfo
import com.d104.domain.model.YogaPose

data class MainState(
    val selectedTab: Tab = Tab.Solo,
    val showBottomBar: Boolean = true,
    val soloYogaCourse:UserCourse? = null,
    val isLogin:Boolean = false,
    val myPageInfo: MyPageInfo? = null,
    val room: Room? = null,
    val yogaPoses:List<YogaPose> = emptyList(),
)
enum class Tab{
    Solo, Multi, MyPage
}
