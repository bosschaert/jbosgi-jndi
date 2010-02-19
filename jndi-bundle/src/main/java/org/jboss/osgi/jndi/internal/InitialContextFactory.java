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
package org.jboss.osgi.jndi.internal;

//$Id$

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.naming.NamingContextFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * A {@link ServiceFactory} for the {@link NamingService}
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class InitialContextFactory implements ServiceFactory
{
   private String jndiHost, jndiPort;

   public InitialContextFactory(String jndiHost, String jndiPort)
   {
      this.jndiHost = jndiHost;
      this.jndiPort = jndiPort;
   }

   public Object getService(Bundle bundle, ServiceRegistration registration)
   {
      try
      {
         return getInitialContext(bundle);
      }
      catch (NamingException ex)
      {
         throw new IllegalStateException("Cannot get the InitialContext", ex);
      }
   }

   public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
   {
      // nothing to do
   }

   InitialContext getInitialContext(Bundle bundle) throws NamingException
   {
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         //Thread.currentThread().setContextClassLoader(BundleClassLoader.createClassLoader(bundle));
         
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         
         Hashtable<String, String> env = new Hashtable<String, String>();
         env.put("java.naming.factory.initial", NamingContextFactory.class.getName());
         env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
         env.put("java.naming.provider.url", "jnp://" + jndiHost + ":" + jndiPort);

         InitialContext iniCtx = new InitialContext(env);
         return iniCtx;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
   }
}