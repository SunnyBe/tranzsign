package com.sunday.tranzsign.di

import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCase
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {
    @Binds
    @ViewModelScoped
    abstract fun bindSignTransactionUseCase(
        impl: SignTransactionUseCaseImpl
    ): SignTransactionUseCase
}