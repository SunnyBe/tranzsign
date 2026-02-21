package com.sunday.tranzsign.data.repository

import com.sunday.tranzsign.domain.entity.AppFeature
import com.sunday.tranzsign.domain.repository.FeatureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureRepositoryImpl @Inject constructor() : FeatureRepository {
    override fun getAvailableFeatures(): Flow<List<AppFeature>> {
        val features = listOf(
            AppFeature(id = 0, title = "withdrawal", isDisabled = false),
            AppFeature(id = 1, title = "transfer", isDisabled = true),
            AppFeature(id = 2, title = "swap", isDisabled = true)
        )
        return flowOf(features)
    }
}