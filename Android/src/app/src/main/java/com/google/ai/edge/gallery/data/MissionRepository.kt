/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.data

import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionRepository @Inject constructor() {
    private val missionBox = ObjectBox.store.boxFor<Mission>()

    fun addMission(mission: Mission) {
        missionBox.put(mission)
    }

    fun getAllMission(agentName: String): List<Mission> {
        return missionBox.query(Mission_.agentName.equal(agentName)).build().find()
    }

    fun getMission(agentName: String): Mission? {
        return getAllMission(agentName).firstOrNull()
    }

    fun searchMissions(agentName: String, embedding: FloatArray, limit: Int): List<Mission> {
        val query = missionBox.query(
            Mission_.embedding.nearestNeighbors(embedding, limit)
                .and(Mission_.agentName.equal(agentName))).build()

        val resultsWithScores = query.findWithScores()
        val results = mutableListOf<Mission>()
        for (result in resultsWithScores) {
            results.add(result.get())
        }

        return results
    }
}
