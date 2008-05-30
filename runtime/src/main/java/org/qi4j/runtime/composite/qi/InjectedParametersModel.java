/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public final class InjectedParametersModel
{
    private final List<DependencyModel> parameterDependencies = new ArrayList<DependencyModel>();

    public InjectedParametersModel()
    {
    }

    public void visitDependencies( DependencyVisitor dependencyVisitor )
    {
        for( DependencyModel dependencyModel : parameterDependencies )
        {
            dependencyVisitor.visit( dependencyModel );
        }
    }

    // Binding
    public void bind( Resolution resolution )
    {
        for( DependencyModel parameterDependency : parameterDependencies )
        {
            parameterDependency.bind( resolution );
        }
    }

    // Context
    public Object[] newParametersInstance( InjectionContext context )
    {
        Object[] parametersInstance = new Object[parameterDependencies.size()];

        // Inject parameterDependencies
        for( int j = 0; j < parameterDependencies.size(); j++ )
        {
            DependencyModel dependencyModel = parameterDependencies.get( j );
            Object parameter = dependencyModel.inject( context );
            parametersInstance[ j ] = parameter;
        }

        return parametersInstance;
    }

    public void addDependency( DependencyModel dependency )
    {
        parameterDependencies.add( dependency );
    }
}
