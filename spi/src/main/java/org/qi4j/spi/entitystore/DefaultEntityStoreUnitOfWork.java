/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.entitystore;

import java.util.LinkedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * JAVADOC
 */
public final class DefaultEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private EntityStoreSPI entityStoreSPI;
    private String identity;
    private ModuleSPI module;
    private LinkedList<EntityState> states = new LinkedList<EntityState>();
    private Usecase usecase;

    public DefaultEntityStoreUnitOfWork( EntityStoreSPI entityStoreSPI, String identity, ModuleSPI module, Usecase usecase )
    {
        this.entityStoreSPI = entityStoreSPI;
        this.identity = identity;
        this.module = module;
        this.usecase = usecase;
    }

    public String identity()
    {
        return identity;
    }

    public ModuleSPI module()
    {
        return module;
    }

    // EntityStore

    public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        EntityState state = entityStoreSPI.newEntityState( this, anIdentity, descriptor );
        states.add( state );
        return state;
    }

    public EntityState getEntityState( EntityReference anIdentity )
        throws EntityStoreException, EntityNotFoundException
    {
        EntityState entityState = entityStoreSPI.getEntityState( this, anIdentity );
        states.add( entityState );
        return entityState;
    }

    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return entityStoreSPI.applyChanges( this, states, identity, System.currentTimeMillis() );
    }

    public void discard()
    {
    }

    public void registerEntityState( EntityState state )
    {
        states.add( state );
    }

    public Usecase usecase()
    {
        return usecase;
    }
}
