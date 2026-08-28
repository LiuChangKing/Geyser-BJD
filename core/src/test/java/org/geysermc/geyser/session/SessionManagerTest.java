/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.geysermc.geyser.session;

import org.geysermc.geyser.entity.type.player.SessionPlayerEntity;
import org.geysermc.geyser.session.auth.AuthData;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class SessionManagerTest {
    @Test
    void newestNeteaseSessionAtomicallyReplacesPreviousOwner() {
        SessionManager manager = new SessionManager();
        long uid = 2748748137L;
        GeyserSession first = session(uid, UUID.randomUUID(), 19132);
        GeyserSession replacement = session(uid, UUID.randomUUID(), 19133);

        assertNull(manager.claimNeteaseSession(first));
        assertSame(first, manager.claimNeteaseSession(replacement));
        assertSame(replacement, manager.sessionByNeteaseUid(uid));
    }

    @Test
    void staleDisconnectCannotRemoveReplacementNeteaseOwner() {
        SessionManager manager = new SessionManager();
        long uid = 2748748137L;
        GeyserSession first = session(uid, UUID.randomUUID(), 19132);
        GeyserSession replacement = session(uid, UUID.randomUUID(), 19133);

        manager.claimNeteaseSession(first);
        manager.claimNeteaseSession(replacement);
        manager.addPendingSession(first);
        manager.removeSession(first);

        assertSame(replacement, manager.sessionByNeteaseUid(uid));
    }

    @Test
    void staleDisconnectCannotRemoveReplacementJavaUuidSession() {
        SessionManager manager = new SessionManager();
        UUID javaUuid = UUID.randomUUID();
        GeyserSession first = session(2748748137L, javaUuid, 19132);
        GeyserSession replacement = session(2748748137L, javaUuid, 19133);

        manager.addPendingSession(first);
        manager.addPendingSession(replacement);
        manager.claimNeteaseSession(first);
        manager.addSession(javaUuid, first);
        manager.claimNeteaseSession(replacement);
        manager.addSession(javaUuid, replacement);
        manager.removeSession(first);

        assertSame(replacement, manager.getSessions().get(javaUuid));
    }

    @Test
    void lateLoginSuccessCannotRestoreReplacedNeteaseSession() {
        SessionManager manager = new SessionManager();
        UUID javaUuid = UUID.randomUUID();
        long uid = 2748748137L;
        GeyserSession first = session(uid, javaUuid, 19132);
        GeyserSession replacement = session(uid, javaUuid, 19133);

        manager.claimNeteaseSession(first);
        manager.claimNeteaseSession(replacement);
        manager.addSession(javaUuid, replacement);
        manager.addSession(javaUuid, first);

        assertSame(replacement, manager.getSessions().get(javaUuid));
    }

    private static GeyserSession session(long uid, UUID playerUuid, int port) {
        GeyserSession session = mock(GeyserSession.class);
        when(session.getAuthData()).thenReturn(new AuthData(
                "player-" + port, UUID.randomUUID(), "xuid-" + port, uid, 1L));
        when(session.getSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", port));

        SessionPlayerEntity playerEntity = mock(SessionPlayerEntity.class);
        when(playerEntity.getUuid()).thenReturn(playerUuid);
        when(session.getPlayerEntity()).thenReturn(playerEntity);
        return session;
    }
}
