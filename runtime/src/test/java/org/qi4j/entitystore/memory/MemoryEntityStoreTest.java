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

package org.qi4j.entitystore.memory;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * Test of MemoryEntityStoreService
 */
public class MemoryEntityStoreTest
        extends AbstractEntityStoreTest
{
    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
        module.importServices( StatePrinter.class ).importedBy( NEW_OBJECT );
        module.addObjects( StatePrinter.class );
    }

    static public class StatePrinter
            implements StateChangeListener
    {
        public void notifyChanges( Iterable<EntityState> changedStates )
        {
            for (EntityState changedState : changedStates)
            {
                System.out.println( changedState.status().name() + ":" + changedState.identity() );
            }
        }
    }
}
