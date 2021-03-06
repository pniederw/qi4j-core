package org.qi4j.entitystore.memory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore, BackupRestore
{
    private final Map<EntityReference, String> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<EntityReference, String>();
    }

    public boolean contains( EntityReference entityReference, EntityType entityType )
        throws EntityStoreException
    {
        return store.containsKey( entityReference );
    }

    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        String state = store.get( entityReference );
        if( state == null )
        {
            throw new EntityNotFoundException( entityReference );
        }

        return new StringReader( state );
    }

    public void applyChanges( MapEntityStore.MapChanges changes )
        throws IOException
    {
        changes.visitMap( new MemoryMapChanger() );
    }

    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<Reader, ReceiverThrowableType> output ) throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<Reader, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, IOException
                    {
                        for( String state : store.values() )
                        {
                            receiver.receive( new StringReader( state ) );
                        }
                    }
                });
            }
        };
    }

    public Input<String, IOException> backup()
    {
        return new Input<String, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<String, ReceiverThrowableType> output ) throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<String, IOException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, IOException
                    {
                        for (String state : store.values())
                        {
                            receiver.receive( state );
                        }
                    }
                });
            }
        };
    }

    public Output<String, IOException> restore()
    {
        return new Output<String, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<String, SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                store.clear();

                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        public void receive( String item ) throws IOException
                        {
                            try
                            {
                                JSONTokener tokener = new JSONTokener( item );
                                JSONObject entity = (JSONObject) tokener.nextValue();
                                String id = entity.getString( JSONKeys.identity.name() );
                                store.put( new EntityReference( id ), item );
                            } catch (JSONException e)
                            {
                                throw new IOException(e);
                            }
                        }
                    });
                } catch (IOException e)
                {
                    store.clear();
                    throw e;
                }
            }
        };
    }

    private class MemoryMapChanger
        implements MapChanger
    {
        public Writer newEntity( final EntityReference ref, EntityType entityType )
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    String old = store.put( ref, toString() );
                    if( old != null )
                    {
                        store.put( ref, old );
                        throw new EntityAlreadyExistsException( ref );
                    }
                }
            };
        }

        public Writer updateEntity( final EntityReference ref, EntityType entityType )
            throws IOException
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    String old = store.put( ref, toString() );
                    if( old == null )
                    {
                        store.remove( ref );
                        throw new EntityNotFoundException( ref );
                    }
                }
            };
        }

        public void removeEntity( EntityReference ref, EntityType entityType )
            throws EntityNotFoundException
        {
            String state = store.remove( ref );
            // Ignore if the entity didn't already exist, as that can happen if it is both created and removed
            // within the same UnitOfWork.
//            if( state == null )
//            {
//                throw new EntityNotFoundException( ref );
//            }
        }
    }
}