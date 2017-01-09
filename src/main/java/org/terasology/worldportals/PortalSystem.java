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

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.worldportals.component.PortalComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share (PortalSystem.class)
public class PortalSystem implements ComponentSystem, UpdateSubscriberSystem {

    @In
    EntityManager entityManager;

    /**
     * Map of portal locations to their destinations.
     */
    private Map<Vector3i, Vector3f> portals = new HashMap<>();

    /**
     * List of portals to be generated once this system initializes. Used for portal requests during world generation.
     */
    private static final List<GeneratedPortal> generatedPortals = Collections.synchronizedList(new LinkedList<>());

    private final List<TeleportRequest> teleportingPlayers = new LinkedList<>();

    @ReceiveEvent(components = {PortalComponent.class})
    public void onPortalActivated(OnActivatedComponent event, EntityRef entity, PortalComponent portalComponent) {
        registerPortal(portalComponent.location, portalComponent.destination);
    }

    @ReceiveEvent(components = {PortalComponent.class})
    public void onSystemDeactivated(BeforeDeactivateComponent event, EntityRef entity, PortalComponent portalComponent) {
        deregisterPortal(portalComponent.location);
    }

    /**
     * Triggers the portal when a player touches it.
     * @param event    An event type variable which checks for the player entering a block (starting to touch)
     * @param entity   The entity entering a block
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        Vector3f startPositionF = entity.getComponent(LocationComponent.class).getWorldPosition();//.sub(event.getCharacterRelativePosition().toVector3f());
        Vector3i startPosition = new Vector3i(startPositionF.x(), startPositionF.y(), startPositionF.z());

        if (entity.getOwner().hasComponent(ClientComponent.class) && portals.containsKey(startPosition)) {
            teleportingPlayers.add(new TeleportRequest(entity, portals.get(startPosition)));
        }
    }

    public void registerPortal (Vector3i startPosition, Vector3f destination) {
        portals.put(startPosition, destination);
    }

    public void deregisterPortal (Vector3i startPosition) {
        portals.remove(startPosition);
    }

    public void spawnPortal (Vector3i location, Vector3f destination) {
        EntityBuilder entityBuilder = entityManager.newBuilder();

        PortalComponent portalComponent = new PortalComponent();
        portalComponent.location = location;
        portalComponent.destination = destination;

        entityBuilder.addComponent(portalComponent);
        entityBuilder.build();
    }

    public static void generatePortal (Vector3i location, Vector3f destination) {
        synchronized (generatedPortals) {
            generatedPortals.add(new GeneratedPortal(location, destination));
        }
    }

    @Override
    public void initialise() {

    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {
        portals.clear();
        generatedPortals.clear();
    }

    @Override
    public void update(float delta) {
        synchronized (generatedPortals) {
            if (generatedPortals.size() > 0) {
                Iterator<GeneratedPortal> toGenerate = generatedPortals.iterator();
                while (toGenerate.hasNext()) {
                    GeneratedPortal p = toGenerate.next();
                    spawnPortal(p.location, p.destination);
                    toGenerate.remove();
                }
            }
        }

        for (TeleportRequest request : teleportingPlayers) {
            request.character.send(new CharacterTeleportEvent(request.destination));
        }
        teleportingPlayers.clear();
    }

    private static class GeneratedPortal {
        public Vector3i location;
        public Vector3f destination;

        public GeneratedPortal(Vector3i location, Vector3f destination) {
            this.location = location;
            this.destination = destination;
        }
    }

    private class TeleportRequest {
        public EntityRef character;
        public Vector3f destination;

        public TeleportRequest(EntityRef character, Vector3f destination) {
            this.character = character;
            this.destination = destination;
        }
    }
}