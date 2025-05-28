package com.d104.yogaapp.main

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.MyPageInfo

sealed class MainIntent {
    data class SelectTab(val tab: Tab) : MainIntent()
    data class SetBottomBarVisibility(val visible: Boolean) : MainIntent()
    data class SetUserRecord(val myPageInfo: MyPageInfo): MainIntent()

    data class SelectSoloCourse(val course: UserCourse) : MainIntent()
    data class SelectRoom(val room: Room) : MainIntent()
    object ClearSoloCourse:MainIntent()
    object ClearUserRecord:MainIntent()

}