/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.jndi;

//$Id$

import java.util.Properties;

import javax.naming.InitialContext;

import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.spi.testing.OSGiRuntime;

/**
 * Adds the JNDI capability to the {@link OSGiRuntime}
 * under test. 
 * 
 * It is ignored if the {@link InitialContext} is already registered.
 * 
 * Installed bundles: jboss-osgi-common-core.jar, jboss-osgi-jndi.jar
 * 
 * Default properties set by this capability
 * 
 * <table>
 * <tr><th>Property</th><th>Value</th></tr> 
 * <tr><td>org.jboss.osgi.jndi.host</td><td>${jboss.bind.address}</td></tr> 
 * <tr><td>org.jboss.osgi.jndi.rmi.port</td><td>1198</td></tr> 
 * <tr><td>org.jboss.osgi.jndi.port</td><td>1199</td></tr> 
 * </table>
 *  
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public class JNDICapability extends Capability
{
   public JNDICapability()
   {
      super(InitialContext.class.getName());
      
      Properties props = getProperties();
      props.setProperty("org.jboss.osgi.jndi.host", System.getProperty("jboss.bind.address", "localhost"));
      props.setProperty("org.jboss.osgi.jndi.rmi.port", "1198");
      props.setProperty("org.jboss.osgi.jndi.port", "1199");
      
      addBundle("bundles/jboss-osgi-common-core.jar");
      addBundle("bundles/jboss-osgi-jndi.jar");
   }
}