/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.test.context;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.ContextComposite;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class ContextCompositeTest
    extends AbstractQi4jTest
{

    @Test
    public void testThreadScope()
        throws InterruptedException
    {
        for( int i = 0; i < 5; i++ )
        {
            CompositeBuilder<MyContextComposite> builder = compositeBuilderFactory.newCompositeBuilder( MyContextComposite.class );
            builder.propertiesFor( MyData.class ).data().set( 0 );
            MyContextComposite context = new ContextComposite<MyContextComposite>( builder ).getProxy();

            Worker w1;
            Worker w2;
            MyContextComposite c1 = builder.newInstance();
            {
                w1 = new Worker( "w1", context, 100, 0 );
                w2 = new Worker( "w2", context, 400, 20 );
                w2.start();
                w1.start();
            }
            w1.join();
            w2.join();
            System.out.println( "W1: " + w1.getData() );
            System.out.println( "W2: " + w2.getData() );
            assertEquals( 0, (int) c1.data().get() );
            assertEquals( 100, w1.getData() );
            assertEquals( 400, w2.getData() );
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( MyContextComposite.class );
    }

    public static interface MyContextComposite extends Composite, MyData
    {
    }

    public static interface MyData
    {
        Property<Integer> data();
    }

    static class Worker extends Thread
    {
        private MyContextComposite composite;
        private int loops;
        private final String spaces;
        private int data;


        public Worker( String name, MyContextComposite composite, int loops, int spaces )
        {
            super( name );
            this.composite = composite;
            this.loops = loops;
            StringBuilder builder = new StringBuilder();
            for( int i = 0; i < spaces; i++ )
            {
                builder.append( " " );
            }
            this.spaces = builder.toString();
        }

        public void run()
        {
            int counter = 0;
            int mismatchCounter = 0;
            Property<Integer> readProperty = composite.data();
            Property<Integer> writeProperty = composite.data();
            try
            {
                int oldValue = 0;
                for( int i = 0; i < loops; i++ )
                {
                    int value;
                    value = readProperty.get();
                    if( oldValue != value )
                    {
                        mismatchCounter++;
                    }
                    value = value + 1;
                    oldValue = value;
                    Thread.sleep( Math.round( Math.random() * 3 ) );
                    writeProperty.set( value );
                    counter++;
                }
            }
            catch( InterruptedException e )
            {
                e.printStackTrace();
            }
            data = composite.data().get();
            System.out.println( counter + "/" + loops + "    " + data + ", " + mismatchCounter + ", " + System.identityHashCode( readProperty ) + ", " + System.identityHashCode( writeProperty ) );
        }

        public int getData()
        {
            return data;
        }
    }
}
