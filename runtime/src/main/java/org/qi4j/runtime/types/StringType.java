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

package org.qi4j.runtime.types;

import java.lang.reflect.Type;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;

/**
 * String type
 */
public final class StringType
    extends AbstractStringType
{
    public static boolean isString( Type type )
    {
        if( type instanceof Class )
        {
            Class typeClass = (Class) type;
            return ( typeClass.equals( String.class ) );
        }
        return false;
    }

    public StringType()
    {
        super( TypeName.nameOf( String.class ) );
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        json.value( value );
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        return value;
    }

    public Object fromJSON( Object json, Module module )
    {
        return json;
    }

    @Override
    public String toQueryParameter( Object value )
    {
        return value == null ? null : value.toString();
    }

    @Override
    public Object fromQueryParameter( String parameter, Module module )
    {
        return parameter;
    }
}