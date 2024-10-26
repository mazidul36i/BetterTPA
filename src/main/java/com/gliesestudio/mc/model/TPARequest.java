/*
 * MIT License
 *
 * Copyright (c) 2024 Mazidul Islam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
