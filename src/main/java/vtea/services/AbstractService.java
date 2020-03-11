/* 
 * Copyright (C) 2020 Indiana University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;


/**
 *
 * @author sethwinfree  
 * 
 * This class is nearly a copy of the AbstractProvider for Trackmate to provide
 * a way to organize plugins using the SciJava framework.
 * @param <K>
 */
@Plugin(type = Service.class)
public abstract class AbstractService< K extends VTEAModule > 
{
	private final Class< K > cl;

	public AbstractService( final Class< K > cl , Context context)
	{
		this.cl = cl;
		registerModules(context);
	}

	protected List< String > keys;

	protected List< String > visibleKeys;
        
        protected List< String > names;
        
        protected List< String > qualifiedNames;

	protected List< String > disabled;

	protected Map< String, K > implementations;

	private void registerModules(Context context)
	{
		//final Context context = new Context( LogService.class, PluginService.class );
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
