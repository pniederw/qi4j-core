/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.structure;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.InstantiationException;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.runtime.association.AssociationContext;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.composite.State;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.PropertyModel;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    protected Class<? extends T> compositeInterface;
    protected ModuleInstance moduleInstance;
    protected CompositeContext context;

    protected Set<Object> uses;
    protected Map<Method, Object> propertyValues;
    protected Map<Method, AbstractAssociation> associationValues;

    public CompositeBuilderImpl( ModuleInstance moduleInstance, CompositeContext context )
    {
        this.moduleInstance = moduleInstance;
        this.context = context;
        this.compositeInterface = (Class<? extends T>) context.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
    }

    public void use( Object... usedObjects )
    {
        Set<Object> useSet = getUses();
        for( Object usedObject : usedObjects )
        {
            useSet.add( usedObject );
        }
    }

    public T propertiesOfComposite()
    {
        // Instantiate proxy for given composite interface
        try
        {
            PropertiesInvocationHandler handler = new PropertiesInvocationHandler();
            ClassLoader proxyClassloader = compositeInterface.getClassLoader();
            Class[] interfaces = new Class[]{ compositeInterface };
            return compositeInterface.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new InstantiationException( e );
        }
    }

    public <K> K propertiesFor( Class<K> mixinType )
    {
        // Instantiate proxy for given interface
        try
        {
            PropertiesInvocationHandler handler = new PropertiesInvocationHandler();
            ClassLoader proxyClassloader = mixinType.getClassLoader();
            Class[] interfaces = new Class[]{ mixinType };
            return mixinType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new InstantiationException( e );
        }
    }

    public T newInstance()
    {
        // Calculate total set of Properties for this Composite
        Map<Method, Property> properties = new HashMap<Method, Property>();
        for( PropertyContext propertyContext : context.getPropertyContexts() )
        {
            Object value;
            Method accessor = propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getAccessor();
            if( propertyValues != null && propertyValues.containsKey( accessor ) )
            {
                value = propertyValues.get( accessor );
            }
            else
            {
                value = propertyContext.getPropertyBinding().getDefaultValue();
            }

            Property property = propertyContext.newInstance( moduleInstance, value );
            PropertyBinding binding = propertyContext.getPropertyBinding();
            PropertyResolution propertyResolution = binding.getPropertyResolution();
            PropertyModel propertyModel = propertyResolution.getPropertyModel();
            properties.put( propertyModel.getAccessor(), property );
        }

        // Calculate total set of Associations for this Composite
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
        for( AssociationContext mixinAssociation : context.getAssociationContexts() )
        {
            Object value = null;
            Method accessor = mixinAssociation.getAssociationBinding().getAssociationResolution().getAssociationModel().getAccessor();
            if( associationValues != null && associationValues.containsKey( accessor ) )
            {
                value = associationValues.get( accessor );
            }

            AbstractAssociation association = mixinAssociation.newInstance( moduleInstance, value );
            AssociationBinding binding = mixinAssociation.getAssociationBinding();
            AssociationResolution associationResolution = binding.getAssociationResolution();
            AssociationModel associationModel = associationResolution.getAssociationModel();
            associations.put( associationModel.getAccessor(), association );
        }

        CompositeInstance compositeInstance = context.newCompositeInstance( moduleInstance,
                                                                            uses,
                                                                            new CompositeBuilderState( properties, associations ) );
        return compositeInterface.cast( compositeInstance.getProxy() );
    }


    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
            }
        };
    }

    private Set<Object> getUses()
    {
        if( uses == null )
        {
            uses = new LinkedHashSet<Object>();
        }
        return uses;
    }

    protected Map<Method, Object> getPropertyValues()
    {
        if( propertyValues == null )
        {
            propertyValues = new HashMap<Method, Object>();
        }
        return propertyValues;
    }

    protected Map<Method, AbstractAssociation> getAssociationValues()
    {
        if( associationValues == null )
        {
            associationValues = new HashMap<Method, AbstractAssociation>();
        }
        return associationValues;
    }

    private void setProperty( PropertyContext propertyContext, Object property )
    {
        Map<Method, Object> compositeProperties = getPropertyValues();
        compositeProperties.put( propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getAccessor(), property );
    }

    private class PropertiesInvocationHandler
        implements InvocationHandler
    {
        public PropertiesInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            final PropertyContext propertyContext = context.getMethodDescriptor( method ).getCompositeMethodContext().getPropertyContext();
            if( propertyContext != null )
            {
                Object defValue = propertyContext.getPropertyBinding().getDefaultValue();
                PropertyBinding binding = propertyContext.getPropertyBinding();
                PropertyInstance<Object> propertyInstance = new ImmutablePropertySupport<Object>( binding, defValue, propertyContext );
                return propertyInstance;
            }
            else
            {
                throw new IllegalArgumentException( "Method is not a property: " + method );
            }
        }

    }

    private class ImmutablePropertySupport<T> extends PropertyInstance<T>
        implements ImmutableProperty<T>
    {
        private final PropertyContext propertyContext;

        public ImmutablePropertySupport( PropertyBinding binding, T defValue, PropertyContext propertyContext )
            throws IllegalArgumentException
        {
            super( binding, defValue );
            this.propertyContext = propertyContext;
        }

        @Override public T set( T newValue ) throws PropertyVetoException
        {
            super.set( newValue );
            setProperty( propertyContext, newValue );
            return newValue;
        }
    }

    static class CompositeBuilderState
        implements State
    {
        Map<Method, Property> properties;
        Map<Method, AbstractAssociation> associations;

        public CompositeBuilderState( Map<Method, Property> properties, Map<Method, AbstractAssociation> associations )
        {
            this.properties = properties;
            this.associations = associations;
        }

        public Property getProperty( Method method )
        {
            return properties.get( method );
        }

        public AbstractAssociation getAssociation( Method qualifiedName )
        {
            return associations.get( qualifiedName );
        }
    }
}
