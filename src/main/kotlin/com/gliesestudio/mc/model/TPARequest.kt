package com.gliesestudio.mc.model

import java.util.UUID

data class TPARequest(
    val requester: UUID,
    val tpaPlayer: UUID,
    val tpaToPlayer: UUID
)
