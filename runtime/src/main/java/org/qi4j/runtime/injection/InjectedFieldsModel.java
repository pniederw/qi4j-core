/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.injection;

import org.qi4j.api.injection.InjectionScope;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.util.Annotations.hasAnnotation;
import static org.qi4j.api.util.Classes.fieldsOf;
import static org.qi4j.api.util.Iterables.*;

/**
 * JAVADOC
 */
public final class InjectedFieldsModel
    implements Binder, Serializable
{
    private final List<InjectedFieldModel> fields = new ArrayList<InjectedFieldModel>();

    public InjectedFieldsModel( Class fragmentClass )
    {
        for( Field field : fieldsOf( fragmentClass ) )
        {
            Annotation injectionAnnotation = first( filter( hasAnnotation( InjectionScope.class), iterable(field.getAnnotations()) ));
            if( injectionAnnotation != null )
            {
                addModel( fragmentClass, field, injectionAnnotation );
            }
        }
    }

    private void addModel( Class fragmentClass, Field field, Annotation injectionAnnotation )
    {
        boolean optional = DependencyModel.isOptional( injectionAnnotation, field.getAnnotations() );
        DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, field.getGenericType(), fragmentClass, optional, field.getAnnotations() );
        InjectedFieldModel injectedFieldModel = new InjectedFieldModel( field, dependencyModel );
        this.fields.add( injectedFieldModel );
    }

    public <ThrowableType extends Throwable> void visitModel( ModelVisitor<ThrowableType> modelVisitor )
        throws ThrowableType
    {
        for( InjectedFieldModel field : fields )
        {
            field.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution context )
        throws BindingException
    {
        for( InjectedFieldModel field : fields )
        {
            field.bind( context );
        }
    }

    public void inject( InjectionContext context, Object instance )
    {
        for( InjectedFieldModel field : fields )
        {
            field.inject( context, instance );
        }
    }
}