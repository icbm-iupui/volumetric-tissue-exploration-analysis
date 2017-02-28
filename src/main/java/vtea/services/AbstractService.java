/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */
package vtea.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vtea.VTEAModule;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;


/**
 *
 * @author sethwinfree  
 * 
 * This class is nearly a copy of the AbstractProvider for Trackmate to provide
 * a way to organize plugins using the SciJava framework.
 * @param <K>
 */
public abstract class AbstractService< K extends VTEAModule > 
{
	private final Class< K > cl;

	public AbstractService( final Class< K > cl )
	{
		this.cl = cl;
		registerModules();
	}

	protected List< String > keys;

	protected List< String > visibleKeys;
        
        protected List< String > names;
        
        protected List< String > qualifiedNames;

	protected List< String > disabled;

	protected Map< String, K > implementations;

	private void registerModules()
	{
		final Context context = new Context( LogService.class, PluginService.class );
		final LogService log = context.getService( LogService.class );
		final PluginService pluginService = context.getService( PluginService.class );
		final List< PluginInfo< K >> infos = pluginService.getPluginsOfType( cl );

		final Comparator< PluginInfo< K >> priorityComparator = new Comparator< PluginInfo< K > >()
				{
			@Override
			public int compare( final PluginInfo< K > o1, final PluginInfo< K > o2 )
			{
				return o1.getPriority() > o2.getPriority() ? 1 : o1.getPriority() < o2.getPriority() ? -1 : 0;
			}
				};

				Collections.sort( infos, priorityComparator );

				keys = new ArrayList< String >( infos.size() );
                                names = new ArrayList< String >( infos.size() );
                                qualifiedNames = new ArrayList< String >( infos.size() );
				visibleKeys = new ArrayList< String >( infos.size() );
				disabled = new ArrayList< String >( infos.size() );
				implementations = new HashMap< String, K >();

				for ( final PluginInfo< K > info : infos )
				{
					if ( !info.isEnabled() )
					{
						disabled.add( info.getClassName() );
						continue;
					}
					try
					{
						final K implementation = info.createInstance();
						final String key = implementation.getKey();
                                                final String name = implementation.getName();
                                                final String qualifiedName = implementation.getClass().getName();

						implementations.put( key, implementation );
						keys.add( key );
                                                names.add( name );
                                                qualifiedNames.add( qualifiedName );
						if ( info.isVisible() )
						{
							visibleKeys.add( key );
						}
					}
					catch ( final InstantiableException e )
					{
						log.error( "Could not instantiate " + info.getClassName(), e );
					}
				}
	}

	public List< String > getKeys()
	{
		return new ArrayList< String >( keys );
	}
        
        public List< String > getNames()
	{
		return new ArrayList< String >( names );
	}

	public List< String > getVisibleKeys()
	{
		return new ArrayList< String >( visibleKeys );
	}

	public List< String > getDisabled()
	{
		return new ArrayList< String >( disabled );
	}

	public K getFactory( final String key )
	{
		return implementations.get( key );
	}
       
        
        public List< String> getQualifiedName()
	{
		return new ArrayList< String >( qualifiedNames );
	}

	public String echo()
	{
		final StringBuilder str = new StringBuilder();
		str.append( "Discovered modules for " + cl.getSimpleName() + ":\n" );
		str.append( "  Enabled & visible:" );
		if ( getVisibleKeys().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String key : getVisibleKeys() )
			{
				str.append( "  - " + key + "\t-->\t" + getFactory( key ).getName() + '\n' );
			}
		}
		str.append( "  Enabled & not visible:" );
		final List< String > invisibleKeys = getKeys();
		invisibleKeys.removeAll( getVisibleKeys() );
		if (invisibleKeys.isEmpty()) {
			str.append( " none.\n" );
		} else{
			str.append( '\n' );
			for ( final String key : invisibleKeys )
			{
				str.append( "  - " + key + "\t-->\t" + getFactory( key ).getName() + '\n' );
			}
		}
		str.append( "  Disabled:" );
		if ( getDisabled().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String cn : getDisabled() )
			{
				str.append( "  - " + cn + '\n' );
			}
		}
		return str.toString();
	}
}
