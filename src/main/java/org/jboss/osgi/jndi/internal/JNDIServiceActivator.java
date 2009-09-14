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

import static org.jboss.osgi.jndi.Constants.REMOTE_JNDI_HOST;
import static org.jboss.osgi.jndi.Constants.REMOTE_JNDI_PORT;
import static org.jboss.osgi.jndi.Constants.REMOTE_JNDI_RMI_PORT;

import java.io.IOException;
import java.net.Socket;

import javax.naming.InitialContext;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * A BundleActivator for the JNDI related services
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class JNDIServiceActivator implements BundleActivator
{
   private ServiceRegistration registration;
   private JNPServer jnpServer;

   public void start(BundleContext context)
   {
      String jndiHost = context.getProperty(REMOTE_JNDI_HOST);
      if (jndiHost == null)
         jndiHost = "localhost";

      String jndiRmiPort = context.getProperty(REMOTE_JNDI_RMI_PORT);
      if (jndiRmiPort == null)
         jndiRmiPort = "1098";

      String jndiPort = context.getProperty(REMOTE_JNDI_PORT);
      if (jndiPort == null)
         jndiPort = "1099";

      try
      {
         // Assume that the JNPServer is already running if we can connect to it 
         Socket socket = new Socket(jndiHost, Integer.parseInt(jndiPort));
         socket.close();
      }
      catch (IOException ex)
      {
         jnpServer = new JNPServer(context, jndiHost, Integer.parseInt(jndiPort), Integer.parseInt(jndiRmiPort));
         jnpServer.start();
      }
      
      InitialContextFactory serviceFactory = new InitialContextFactory(jndiHost, jndiPort);
      registration = context.registerService(InitialContext.class.getName(), serviceFactory, null);
   }

   public void stop(BundleContext context)
   {
      if (registration != null)
      {
         registration.unregister();
         registration = null;
      }
      
      if (jnpServer != null)
      {
         jnpServer.stop();
         jnpServer = null;
      }
   }
}