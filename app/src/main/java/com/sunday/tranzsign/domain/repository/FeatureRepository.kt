package com.sunday.tranzsign.domain.repository

import com.sunday.tranzsign.domain.entity.AppFeature
import kotlinx.coroutines.flow.Flow

/**
 * Manages the list of available features in the app.
 * Using conditions like location or user account type, some features may be disabled for certain users.
 * Remote configuration can be used to enable or disable features without requiring an app update.
 */
interface FeatureRepository {
    fun getAvailableFeatures(): Flow<List<AppFeature>>
}