package com.d104.data.di

import com.d104.data.remote.api.UserApiService
import com.d104.data.remote.api.UserCourseApiService
import com.d104.data.remote.api.YogaPoseApiService
import com.d104.data.remote.api.YogaPoseHistoryApiService
import com.d104.data.remote.datasource.yogaposehistory.YogaPoseHistoryDataSource
import com.d104.data.remote.datasource.yogaposehistory.YogaPoseHistoryDataSourceImpl
import com.d104.data.remote.datasource.user.UserDataSource
import com.d104.data.remote.datasource.user.UserDataSourceImpl
import com.d104.data.remote.datasource.usercourse.UserCourseDataSource
import com.d104.data.remote.datasource.usercourse.UserCourseDataSourceImpl
import com.d104.data.remote.datasource.yogapose.YogaPoseDataSource
import com.d104.data.remote.datasource.yogapose.YogaPoseDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataModule {
    @Provides
    @Singleton
    fun provideYogaPoseRemoteDataSource(yogaPoseApiService: YogaPoseApiService): YogaPoseDataSource {
        return YogaPoseDataSourceImpl(yogaPoseApiService)
    }

    @Provides
    @Singleton
    fun provideUserCourseRemoteDataSource(userCourseApiService: UserCourseApiService): UserCourseDataSource {
        return UserCourseDataSourceImpl(userCourseApiService)
    }

    @Provides
    @Singleton
    fun provideYogaPoseHistoryRemoteDataSource(yogaPoseHistoryApiService: YogaPoseHistoryApiService): YogaPoseHistoryDataSource {
        return YogaPoseHistoryDataSourceImpl(yogaPoseHistoryApiService)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(userApiService: UserApiService): UserDataSource {
        return UserDataSourceImpl(userApiService)
    }

}