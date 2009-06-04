/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jsfunit.spy.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import org.jboss.jsfunit.spy.context.SpyHttpServletRequest;

/**
 *
 * @author Stan Silvert
 * @since 1.1
 */
public class RequestData 
{
   private int sequenceNumber = -1;
   private HttpServletRequest request;
   private List<Snapshot> snapshots = new ArrayList<Snapshot>();
   private String fromView = null;
   private String toView = null;
   private List<PhaseData> phaseData;
   
   private boolean[][] snapshotTaken = new boolean[PhaseId.VALUES.size()][BeforeOrAfter.values().length];
   
   RequestData(FacesContext facesContext)
   {
      ExternalContext extCtx = facesContext.getExternalContext();
      this.request = new SpyHttpServletRequest((HttpServletRequest)extCtx.getRequest());
   }
   
   void setSequenceNumber(int sequenceNumber)
   {
      this.sequenceNumber = sequenceNumber;
   }
   
   public int getSequenceNumber()
   {
      return this.sequenceNumber;
   }

   void takeSnapshotBefore(PhaseEvent event)
   {
      this.snapshots.add(new Snapshot(BeforeOrAfter.BEFORE, event));
      this.snapshotTaken[event.getPhaseId().getOrdinal() - 1][BeforeOrAfter.BEFORE.ordinal()] = true;
      
      if (event.getPhaseId() == PhaseId.RENDER_RESPONSE)
      {
         String view = event.getFacesContext().getViewRoot().getViewId();
         this.toView = view;
      }
   }
   
   void takeSnapshotAfter(PhaseEvent event)
   {
      this.snapshots.add(new Snapshot(BeforeOrAfter.AFTER, event));
      this.snapshotTaken[event.getPhaseId().getOrdinal() - 1][BeforeOrAfter.AFTER.ordinal()] = true;
      
      if (event.getPhaseId() == PhaseId.RESTORE_VIEW)
      {
         UIViewRoot root = event.getFacesContext().getViewRoot();
         if (root == null) return;
         
         this.fromView = root.getViewId();
         this.toView = this.fromView;
      }
   }
   
   public boolean[][] getSnapshotsTaken()
   {
      return this.snapshotTaken;
   }
   
   public HttpServletRequest getHttpRequest()
   {
      return this.request;
   }
   
   public List<Snapshot> getSnapshots()
   {
      return Collections.unmodifiableList(this.snapshots);
   }
   
   public Snapshot getFirstSnapshot()
   {
      if (snapshots.size() == 0) throw new IllegalStateException("No Snapshots have been taken for this request.");
      return this.snapshots.get(0);
   }
   
   public Snapshot getLastSnapshot()
   {
      if (snapshots.size() == 0) throw new IllegalStateException("No Snapshots have been taken for this request.");
      return this.snapshots.get(snapshots.size() - 1);
   }
   
   public long getElapsedTime()
   {
      return getLastSnapshot().getTimestamp() - getFirstSnapshot().getTimestamp();
   }
   
   public String getFromView()
   {
      return this.fromView;
   }
   
   public String getToView()
   {
      return this.toView;
   }
   
   public List<PhaseData> getPhaseData()
   {
      if (this.phaseData == null)
      {
         List<PhaseData> phaseDataList = new ArrayList();
         List<Snapshot> snapshotList = getSnapshots();
         for (PhaseId phaseId : PhaseId.VALUES)
         {
            if (phaseId == PhaseId.ANY_PHASE) continue;
            phaseDataList.add(new PhaseData(phaseId, snapshotList));
         }
         this.phaseData = phaseDataList;
      }
      
      return this.phaseData;
   }
   
}