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

import java.net.InetAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.naming.NamingException;

import org.jboss.net.sockets.DefaultSocketFactory;
import org.jboss.osgi.common.log.LogServiceTracker;
import org.jnp.interfaces.Naming;
import org.jnp.server.Main;
import org.jnp.server.NamingBean;
import org.jnp.server.NamingServer;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Start/Stop the {@link NamingServer}
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class JNPServer
{
   private LogService log;
   private String host;
   private int jndiPort;
   private int rmiPort;
   
   private Main namingMain;
   private Naming namingServer;
   
   private boolean shutdownRegistry;
   private Registry rmiRegistry;

   public JNPServer(BundleContext context, String host, int jndiPort, int rmiPort)
   {
      this.log = new LogServiceTracker(context);
      this.host = host;
      this.jndiPort = jndiPort;
      this.rmiPort = rmiPort;
   }

   public void start()
   {
      try
      {
         // check to see if registry already created
         rmiRegistry = LocateRegistry.getRegistry(host, rmiPort);
         try
         {
            rmiRegistry.list();
            log.log(LogService.LOG_DEBUG, "RMI registry running at host=" + host + ",port=" + rmiPort);
         }
         catch (RemoteException e)
         {
            log.log(LogService.LOG_DEBUG, "No RMI registry running at host=" + host + ",port=" + rmiPort + ".  Will create one.");
            rmiRegistry = LocateRegistry.createRegistry(rmiPort, null, new DefaultSocketFactory(InetAddress.getByName(host)));
            shutdownRegistry = true;
         }
         
         namingMain = new Main();
         namingMain.setNamingInfo(getNamingBean());

         namingMain.setBindAddress(host);
         namingMain.setPort(jndiPort);

         namingMain.setRmiBindAddress(host);
         namingMain.setRmiPort(rmiPort);

         namingMain.start();
         log.log(LogService.LOG_INFO, "JNDI started: JNP=" + host + ":" + jndiPort + ", RMI=" + host + ":" + rmiPort);
      }
      catch (Exception ex)
      {
         log.log(LogService.LOG_ERROR, "Cannot start Naming server", ex);
      }
   }

   public void stop()
   {
      if (namingMain != null)
      {
         namingMain.stop();
         namingMain = null;
         namingServer = null;
         log.log(LogService.LOG_DEBUG, "Naming server stopped");
      }
      
      // Shutdown the registry if this service created it
      if (shutdownRegistry == true)
      {
         try
         {
            log.log(LogService.LOG_DEBUG, "Shutdown RMI registry");
            UnicastRemoteObject.unexportObject(rmiRegistry, true);
         }
         catch (NoSuchObjectException ex)
         {
            log.log(LogService.LOG_WARNING, "Cannot shutdown RMI registry", ex);
         }
      }
   }

   private NamingBean getNamingBean()
   {
      NamingBean namingBean = new NamingBean()
      {
         public Naming getNamingInstance()
         {
            if (namingServer == null)
            {
               try
               {
                  namingServer = new NamingServer();
               }
               catch (NamingException ex)
               {
                  throw new IllegalStateException("Cannot create NamingServer", ex);
               }
            }
            return namingServer;
         }
      };
      return namingBean;
   }
}