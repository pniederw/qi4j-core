/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.spi.entity.property;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.qi4j.entity.property.AbstractProperty;
import org.qi4j.spi.composite.PropertyResolution;

/**
 * TODO
 */
public class PropertyBinding
{
    private PropertyResolution propertyResolution;
    private Class<? extends AbstractProperty> implementationClass;
    private Constructor<? extends AbstractProperty> constructor;
    private Map<Class, Object> propertyInfo;
    private Object defaultValue;

    public PropertyBinding( PropertyResolution propertyResolution, Class<? extends AbstractProperty> implementationClass, Constructor<? extends AbstractProperty> constructor, Map<Class, Object> propertyInfo, Object defaultValue )
    {
        this.defaultValue = defaultValue;
        this.propertyInfo = propertyInfo;
        this.constructor = constructor;
        this.propertyResolution = propertyResolution;
        this.implementationClass = implementationClass;
    }

    public PropertyResolution getPropertyResolution()
    {
        return propertyResolution;
    }

    public Class<? extends AbstractProperty> getImplementationClass()
    {
        return implementationClass;
    }

    public Constructor<? extends AbstractProperty> getConstructor()
    {
        return constructor;
    }

    public <T> T getPropertyInfo( Class<T> infoClass )
    {
        return infoClass.cast( propertyInfo.get( infoClass ) );
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }
}