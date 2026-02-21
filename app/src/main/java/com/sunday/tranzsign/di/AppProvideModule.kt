package com.sunday.tranzsign.di

import com.sunday.tranzsign.data.service.AppCoroutineDispatcher
import com.sunday.tranzsign.domain.service.CoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvideModule {
    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider = AppCoroutineDispatcher()
}