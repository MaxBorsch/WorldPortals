/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.worldportals;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.worldportals.component.PortalComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PortalSystem extends BaseComponentSystem {

    /**
     * Triggers the portal when a player touches it.
     * @param event    An event type variable which checks for the player entering a block (starting to touch)
     * @param entity   The entity entering a block
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        if (event.getNewBlock().getEntity().hasComponent(PortalComponent.class)) {
            PortalComponent portal = event.getNewBlock().getEntity().getComponent(PortalComponent.class);

            ClientComponent clientComp = entity.getComponent(ClientComponent.class);
            clientComp.character.send(new CharacterTeleportEvent(portal.destination.toVector3f()));
        }
    }
}