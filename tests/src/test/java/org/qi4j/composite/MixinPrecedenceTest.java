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

package org.qi4j.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test mixin declaration precedence test
 */
public class MixinPrecedenceTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestComposite1.class,
                              TestComposite2.class,
                              TestComposite3.class );
    }

    public void testWhenMultipleTypedMixinsPrecedence()
    {
        TestComposite1 instance = compositeBuilderFactory.newComposite( TestComposite1.class );
        assertEquals( "A1", instance.AMethod() );
    }

    public void testWhenGenericAndTypedMixinPrecedence()
    {
        TestComposite2 instance = compositeBuilderFactory.newComposite( TestComposite2.class );
        assertEquals( "A1", instance.AMethod() );
    }

    public void testWhenMultipleGenericMixinsPrecedence()
    {
        TestComposite3 instance = compositeBuilderFactory.newComposite( TestComposite3.class );
        assertEquals( "GM1", instance.AMethod() );
    }

    @Mixins( { AMixin1.class, AMixin2.class } )
    public static interface TestComposite1
        extends A, Composite
    {
    }

    @Mixins( { GenericMixin1.class, AMixin1.class } )
    public static interface TestComposite2
        extends A, Composite
    {
    }

    @Mixins( { GenericMixin1.class, GenericMixin2.class } )
    public static interface TestComposite3
        extends A, Composite
    {
    }

    public interface A
    {
        String AMethod();
    }

    public static class AMixin1
        implements A
    {

        public String AMethod()
        {
            return "A1";
        }
    }

    public static class AMixin2
        implements A
    {

        public String AMethod()
        {
            return "A2";
        }
    }

    public static class GenericMixin1
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            return "GM1";
        }
    }

    public static class GenericMixin2
        implements InvocationHandler
    {
        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            return "GM2";
        }
    }
}