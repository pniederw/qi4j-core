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

package org.qi4j.api.common;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.structure.ApplicationSPI;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

/**
 * Sample of how a plugin architecture could work.
 * The plugins can use services in the main application and the main
 * application can use services from the plugins
 */
public class PluginTest
{
    @Test
    @Ignore( "Must fix the TODOs below." )
    public void testPlugins()
        throws Exception
    {
        Energy4Java runtime = new Energy4Java();
        ApplicationSPI app = runtime.newApplication( new MainApplicationAssembler() );
        app.activate();
    }

    // Main application

    class MainApplicationAssembler
        implements ApplicationAssembler
    {
        public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException
        {
            return applicationFactory.newApplicationAssembly( new Assembler[][][]
                {
                    {
                        {
                            new PluginAssembler(),
                            new UIAssembler(),
                        }
                    },
                    {
                        {
                            new ServiceAssembler()
                        }
                    }
                } );
        }
    }

    // The UI uses the plugins

    class UIAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( PluginTesterService.class ).instantiateOnStartup();
            module.importServices( Plugin.class )
                .importedBy( ServiceFinderImporter.class )
                .visibleIn( Visibility.layer );
        }
    }

    // Get a reference to a plugin and use it

    @Mixins( PluginTesterService.PluginTesterMixin.class )
    interface PluginTesterService
        extends Activatable, ServiceComposite
    {
        class PluginTesterMixin
            implements Activatable
        {
            @Service
            Plugin plugin;

            @Service
            PluginsService plugins;

            public void activate()
                throws Exception
            {
                // Use plugin
                System.out.println( plugin.say( "Hello", "World" ) );

                // Restart plugin
                plugins.passivate();

                // Plugin is now unavailable
                try
                {
                    System.out.println( plugin.say( "Hello", "World" ) );
                }
                catch( ServiceImporterException e )
                {
                    // Ignore
                }

                plugins.activate();

                // Use plugin
                System.out.println( plugin.say( "Hello", "World" ) );
            }

            public void passivate()
                throws Exception
            {
            }
        }
    }

    // Assemble the base service that the plugin can use

    class ServiceAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( HelloWorldService.class ).visibleIn( Visibility.application );
        }
    }

    // Plugins can access service instances with this interface

    interface HelloWorld
    {
        String say( String phrase, String name );
    }

    // Implementation of service that the plugin can use

    @Mixins( HelloWorldService.HelloWorldMixin.class )
    interface HelloWorldService
        extends HelloWorld, ServiceComposite
    {
        class HelloWorldMixin
            implements HelloWorld
        {
            public String say( String phrase, String name )
            {
                return phrase + " " + name;
            }
        }
    }

    // Assemble the plugins module

    class PluginAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( PluginsService.class ).instantiateOnStartup();
        }
    }

// Plugin service
// This assembles and activates a separate application for the plugins
// The plugins can look up services in the host application and the

    // plugins can be looked up by the host application

    @Mixins( PluginsService.PluginsMixin.class )
    interface PluginsService
        extends Activatable, ServiceComposite
    {
        class PluginsMixin
            implements Activatable
        {
            @Structure
            ServiceFinder finder;
            @Service
            ServiceReference<Plugin> plugin;
            private ApplicationSPI app;

            public void activate()
                throws Exception
            {
                Energy4Java runtime = new Energy4Java();
                app = runtime.newApplication( new PluginApplicationAssembler( finder ) );

                app.activate();
                ServiceFinder pluginFinder = app.findModule( "Plugin layer", "Plugin module" ).serviceFinder();

                // TODO: Niclas wrote: No clue how all this Test is supposed to work, and can't figure out to create a workaround for this.
//                finder.findService(Plugin.class).metaInfo().add(ServiceFinder.class, pluginFinder);
            }

            public void passivate()
                throws Exception
            {
                // TODO: Niclas wrote: No clue how all this Test is supposed to work, and can't figure out to create a workaround for this.
//                plugin.metaInfo().remove(ServiceFinder.class);

                app.passivate();
            }
        }
    }

    // Assemble the plugin application

    public static class PluginApplicationAssembler
        implements ApplicationAssembler
    {
        private ServiceFinder finder;

        public PluginApplicationAssembler( ServiceFinder finder )
        {
            this.finder = finder;
        }

        public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException
        {
            return applicationFactory.newApplicationAssembly( new Assembler()
            {
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    new LayerName( "Plugin layer" ).assemble( module );
                    new ModuleName( "Plugin module" ).assemble( module );

                    LayerAssembly layer = module.layerAssembly();

                    // In a real case you would "detect" the plugins somehow. Here the plugin assembler is hardcoded
                    List<Assembler> pluginAssemblers = Collections.<Assembler>singletonList( new SimonAssembler() );

                    for( int i = 0; i < pluginAssemblers.size(); i++ )
                    {
                        ModuleAssembly pluginModule = layer.moduleAssembly( "Plugin " + ( i + 1 ) );
                        Assembler assembler = pluginAssemblers.get( i );
                        assembler.assemble( pluginModule );
                    }

                    // Import host services
                    module.importServices( HelloWorld.class )
                        .importedBy( ServiceFinderImporter.class )
                        .setMetaInfo( finder )
                        .visibleIn( Visibility.layer );
                }
            } );
        }
    }

    // Service importer that uses a ServiceFinder

    public static class ServiceFinderImporter
        implements ServiceImporter
    {
        public Object importService( final ImportedServiceDescriptor serviceDescriptor )
            throws ServiceImporterException
        {
            return Proxy.newProxyInstance( serviceDescriptor.type().getClassLoader(), new Class[]{ serviceDescriptor.type() }, new InvocationHandler()
            {
                public Object invoke( Object proxy, Method method, Object[] args )
                    throws Throwable
                {
                    ServiceFinder finder = serviceDescriptor.metaInfo( ServiceFinder.class );
                    if( finder == null )
                    {
                        throw new ServiceImporterException( "No ServiceFinder specified for imported service " + serviceDescriptor
                            .identity() );
                    }
                    Object service = finder.findService( serviceDescriptor.type() ).get();
                    return method.invoke( service, args );
                }
            } );
        }

        public boolean isActive( Object instance )
        {
            return true;
        }

        public boolean isAvailable( Object instance )
        {
            return true;
        }
    }

// The plugin interface. Plugins should implement this, and they can then

    // be looked up by the host application

    interface Plugin
    {
        String say( String phrase, String name );
    }

    // Assemble a sample plugin

    public static class SimonAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( SimonSaysPlugin.class ).visibleIn( Visibility.layer );
        }
    }

    // Test implementation of the plugin interface

    @Mixins( SimonSaysPlugin.SimonSaysMixin.class )
    interface SimonSaysPlugin
        extends Plugin, ServiceComposite
    {
        class SimonSaysMixin
            implements Plugin
        {
            @Service
            HelloWorld helloWorld;
            private long time;

            public SimonSaysMixin()
            {
                time = System.currentTimeMillis();
            }

            public String say( String phrase, String name )
            {
                return "Simon says:" + helloWorld.say( phrase, name ) + " (plugin started at:" + time + ")";
            }
        }
    }
}
