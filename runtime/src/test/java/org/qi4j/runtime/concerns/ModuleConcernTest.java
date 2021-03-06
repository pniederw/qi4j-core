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

package org.qi4j.runtime.concerns;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblyVisitorAdapter;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.TransientDeclaration;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test of declaring concern in assembly
 */
public class ModuleConcernTest
    extends AbstractQi4jTest
{
    public static boolean ok;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( FooComposite.class );

        module.layerAssembly().applicationAssembly().visit( new AssemblyVisitorAdapter<RuntimeException>()
        {
            @Override
            public void visitComposite( TransientDeclaration declaration )
            {
                declaration.withConcerns( TraceConcern.class );
            }
        }

        );
    }

    @Test
    public void testModuleConcerns()
    {
        transientBuilderFactory.newTransient( Foo.class ).test( "Foo", 42 );
        assertThat( "Concern has executed", ok, equalTo( true ) );
    }

    @Mixins( FooMixin.class )
    public interface FooComposite
        extends TransientComposite, Foo
    {
    }

    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class TraceConcern
        extends GenericConcern
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            ok = true;
            Object result = next.invoke( proxy, method, args );
            String str = method.getName() + Arrays.asList( args );
            System.out.println( str );
            return result;
        }
    }
}
