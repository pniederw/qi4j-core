/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.Association;
import org.qi4j.association.ManyAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.Lifecycle;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.structure.CompositeBuilderImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;

/**
 * TODO
 */
public final class EntitySessionCompositeBuilder<T extends Composite>
    extends CompositeBuilderImpl<T>
{
    private EntitySessionInstance entitySession;
    private EntityStore store;
    private static Method IDENTITY_METHOD = null;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    public EntitySessionCompositeBuilder( ModuleInstance moduleInstance, CompositeContext compositeContext, EntitySessionInstance entitySession, EntityStore store )
    {
        super( moduleInstance, compositeContext );
        this.entitySession = entitySession;
        this.store = store;
    }

    public void use( Object... usedObjects )
    {
        throw new InvalidApplicationException( "Entities may not use other objects" );
    }

    public T newInstance()
    {
        EntityState state;
        String identity = getPropertyValues().get( IDENTITY_METHOD ).toString();
        Map<Method, Object> propertyValues = getPropertyValues();
        try
        {
            state = store.newEntityState( entitySession, identity, context.getCompositeBinding(), propertyValues );
        }
        catch( StoreException e )
        {
            throw new InstantiationException( "Could not create new entity in store", e );
        }

        Map<Method, AbstractAssociation> associationValues = getAssociationValues();
        for( Map.Entry<Method, AbstractAssociation> association : associationValues.entrySet() )
        {
            AbstractAssociation associationValue = state.getAssociation( association.getKey() );
            if( associationValue instanceof ManyAssociation )
            {
                ManyAssociation manyAssociation = (ManyAssociation) associationValue;
                ManyAssociation newAssociation = (ManyAssociation) association;
                manyAssociation.addAll( newAssociation );
            }
            else
            {
                Association singleAssociation = (Association) associationValue;
                Association newAssociation = (Association) association;
                singleAssociation.set( newAssociation.get() );
            }
        }

        EntityCompositeInstance compositeInstance = context.newEntityCompositeInstance( moduleInstance, entitySession, store, identity );
        context.newEntityMixins( moduleInstance, compositeInstance, state );
        T instance = compositeInterface.cast( compositeInstance.getProxy() );
        entitySession.createEntity( (EntityComposite) instance );

        // Invoke lifecycle create() method
        if( instance instanceof Lifecycle )
        {
            context.invokeCreate( instance, compositeInstance );
        }

        return instance;
    }

    public Iterator<T> iterator()
    {
        final Iterator<T> decoratedIterator = super.iterator();

        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                T instance = decoratedIterator.next();
                entitySession.createEntity( (EntityComposite) instance );
                return instance;
            }

            public void remove()
            {
            }
        };
    }


}
