/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.jsfunit.facade;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import org.jboss.jsfunit.framework.FacesContextBridge;

/**
 * The JSFServerSession provides a simplified API that wraps parts of the JSF API 
 * for things that you would commonly do in testing.
 * 
 * 
 * @author Stan Silvert
 */
public class JSFServerSession
{
   private JSFClientSession client;
   
   /**
    * Create a new JSFServerSession.
    * 
    * 
    * 
    * @param client The JSFClientSession for the current web conversation.
    */
   public JSFServerSession(JSFClientSession client)
   {
      if (client == null) throw new NullPointerException("client can not be null");
      this.client = client;
   }
   
   /**
    * Return the current view Id from the component tree.
    *
    * @return The current View Id.
    */
   public String getCurrentViewId()
   {
      return getFacesContext().getViewRoot().getViewId();
   }
   
   /**
    * Get the FacesContext object used in the last request.
    *
    * @return The FacesContext object used in the last request.
    */
   public FacesContext getFacesContext()
   {
      return FacesContextBridge.getCurrentInstance();
   }
   
   /**
    * Find a component in the JSF component tree.  
    *
    * @param componentID The JSF component ID or client ID suffix.
    *
    * @return The component.
    *
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the componentID suffix
    */
   public UIComponent findComponent(String componentID)
   {
      return client.getClientIDs().findComponent(componentID);
   }
   
   /**
    * Find a clientID given the componentID.  
    *
    * @param componentID The JSF component ID or client ID suffix.
    *
    * @return The clientID.
    *
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the componentID suffix
    */
   public String findClientID(String componentID)
   {
      return client.getClientIDs().findClientID(componentID);
   }
   
   /**
    * Find a component in the JSF component tree and return its value.
    * Note that the found component must implement ValueHolder.
    *
    * @param componentID The JSF component ID or client ID suffix.
    *
    * @return The value contained in the component.
    *
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the componentID suffix
    * @throws ClassCastException if the found component does not implement ValueHolder
    */
   public Object getComponentValue(String componentID)
   {
      UIComponent component = findComponent(componentID);
      if (!(component instanceof ValueHolder))
      {
         throw new ClassCastException(componentID + " must be an instance of ValueHolder.");
      }
      
      return ((ValueHolder)component).getValue();
   }
   
   /**
    * Evaluate an EL ValueExpression and return the value.
    *
    * @param elExpression The expression.
    *
    * @return The value.
    */
   public Object getManagedBeanValue(String elExpression)
   {
      FacesContext facesContext = getFacesContext();
      return facesContext.getApplication()
                         .createValueBinding(elExpression)
                         .getValue(facesContext);
   }
}
