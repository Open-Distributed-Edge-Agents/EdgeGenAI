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

    fun getMission(agentName: String): Mission? {
        return missionBox.query(Mission_.agentName.equal(agentName)).build().findFirst()
    }

    fun searchMissions(embedding: FloatArray): List<Mission> {
        return missionBox.query().findNeighbors(Mission_.embedding, embedding, 10)
    }
}
