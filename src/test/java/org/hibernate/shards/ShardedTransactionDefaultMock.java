/**
 * Copyright (C) 2007 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.hibernate.shards;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import javax.transaction.Synchronization;

/**
 * @author Tomislav Nad
 */
public class ShardedTransactionDefaultMock implements ShardedTransaction {

  public void setupTransaction(Session session) {
    throw new UnsupportedOperationException();
  }

  public void begin() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public void commit() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public void rollback() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public boolean wasRolledBack() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public boolean wasCommitted() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public boolean isActive() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public void registerSynchronization(Synchronization synchronization)
      throws HibernateException {
    throw new UnsupportedOperationException();
  }

  public void setTimeout(int seconds) {
    throw new UnsupportedOperationException();
  }
}
