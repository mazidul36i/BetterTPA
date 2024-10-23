package com.gliesestudio.mc.model;

import java.util.UUID;

public class TPARequest {

    private UUID requester;
    private UUID tpaPlayer;
    private UUID tpaToPlayer;

    public TPARequest() {
    }

    public TPARequest(UUID requester, UUID tpaPlayer, UUID tpaToPlayer) {
        this.requester = requester;
        this.tpaPlayer = tpaPlayer;
        this.tpaToPlayer = tpaToPlayer;
    }

    public UUID getRequester() {
        return requester;
    }

    public void setRequester(UUID requester) {
        this.requester = requester;
    }

    public UUID getTpaPlayer() {
        return tpaPlayer;
    }

    public void setTpaPlayer(UUID tpaPlayer) {
        this.tpaPlayer = tpaPlayer;
    }

    public UUID getTpaToPlayer() {
        return tpaToPlayer;
    }

    public void setTpaToPlayer(UUID tpaToPlayer) {
        this.tpaToPlayer = tpaToPlayer;
    }
}
