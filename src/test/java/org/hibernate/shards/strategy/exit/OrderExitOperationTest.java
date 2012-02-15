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

package org.hibernate.shards.strategy.exit;

import junit.framework.TestCase;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.shards.criteria.InMemoryOrderBy;
import org.hibernate.shards.defaultmock.ClassMetadataDefaultMock;
import org.hibernate.shards.defaultmock.EntityPersisterDefaultMock;
import org.hibernate.shards.defaultmock.SessionFactoryDefaultMock;
import org.hibernate.shards.util.Lists;
import org.hibernate.shards.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author Maulik Shah
 */
public class OrderExitOperationTest extends TestCase {

  private class MyInt {
    private final Integer i;

    private final String name;

    private final MyInt innerMyInt;

    public MyInt(int i, String name, MyInt inner) {
      this.i = i;
      this.name = name;
      this.innerMyInt = inner;
    }

    public MyInt getInnerMyInt() {
      return innerMyInt;
    }

    public Number getValue() {
      return i;
    }

    public String getName() {
      return name;
    }

    public boolean equals(Object obj) {
      MyInt myInt = (MyInt) obj;
      return
          this.getName().equals(myInt.getName()) &&
          this.getValue().equals(myInt.getValue());
    }
  }

  public void testApplySingleOrdering() throws Exception {
    List<Object> data = Lists.newArrayList();
    data.add(new MyInt(3, "tomislav", null));
    data.add(new MyInt(1, "bomb", null));
    data.add(new MyInt(27, "max", null));
    data.add(new MyInt(2, "maulik", null));
    data.add(new MyInt(5, "gut", null));
    
    InMemoryOrderBy imob = new InMemoryOrderBy(null, Order.asc("value"));
    OrderExitOperation oeo = new OrderExitOperation(Collections.singletonList(imob));
    List<MyInt> sortedList = (List<MyInt>)(List)oeo.apply(data);

    assertEquals(1, sortedList.get(0).getValue());
    assertEquals(2, sortedList.get(1).getValue());
    assertEquals(3, sortedList.get(2).getValue());
    assertEquals(5, sortedList.get(3).getValue());
    assertEquals(27, sortedList.get(4).getValue());

    imob = new InMemoryOrderBy(null, Order.desc("value"));
    oeo = new OrderExitOperation(Collections.singletonList(imob));
    sortedList = (List<MyInt>)(List)oeo.apply(data);

    assertEquals(27, sortedList.get(0).getValue());
    assertEquals(5, sortedList.get(1).getValue());
    assertEquals(3, sortedList.get(2).getValue());
    assertEquals(2, sortedList.get(3).getValue());
    assertEquals(1, sortedList.get(4).getValue());
  }

  public void testApplySingleNestedOrdering() throws Exception {
    List<Object> data = Lists.newArrayList();
    data.add(new MyInt(3, "tomislav", new MyInt(3, "tomislav", null)));
    data.add(new MyInt(1, "bomb", new MyInt(1, "bomb", null)));
    data.add(new MyInt(27, "max", new MyInt(27, "max", null)));
    data.add(new MyInt(2, "maulik", new MyInt(2, "maulik", null)));
    data.add(new MyInt(5, "gut", new MyInt(5, "gut", null)));

    InMemoryOrderBy imob = new InMemoryOrderBy(null, Order.asc("innerMyInt.value"));
    OrderExitOperation oeo = new OrderExitOperation(Collections.singletonList(imob));
    List<MyInt> sortedList = (List<MyInt>)(List)oeo.apply(data);

    assertEquals(1, sortedList.get(0).getValue());
    assertEquals(2, sortedList.get(1).getValue());
    assertEquals(3, sortedList.get(2).getValue());
    assertEquals(5, sortedList.get(3).getValue());
    assertEquals(27, sortedList.get(4).getValue());

    imob = new InMemoryOrderBy(null, Order.desc("innerMyInt.value"));
    oeo = new OrderExitOperation(Collections.singletonList(imob));
    sortedList = (List<MyInt>)(List)oeo.apply(data);

    assertEquals(27, sortedList.get(0).getValue());
    assertEquals(5, sortedList.get(1).getValue());
    assertEquals(3, sortedList.get(2).getValue());
    assertEquals(2, sortedList.get(3).getValue());
    assertEquals(1, sortedList.get(4).getValue());
  }

  public void testApplyMultipleOrderings() {
    List<Object> data = Lists.newArrayList();
    data.add(new MyInt(2, "tomislav", null));
    data.add(new MyInt(1, "bomb", null));
    data.add(new MyInt(27, "max", null));
    data.add(new MyInt(2, "maulik", null));
    data.add(new MyInt(5, "gut", null));

    List<InMemoryOrderBy> list = Lists.newArrayList(
        new InMemoryOrderBy(null, Order.asc("value")),
        new InMemoryOrderBy(null, Order.desc("name"))
    );

    OrderExitOperation operation = new OrderExitOperation(list);
    List<MyInt> sortedList = (List<MyInt>)(List)operation.apply(data);

    assertEquals(1, sortedList.get(0).getValue());
    assertEquals(2, sortedList.get(1).getValue());
    assertEquals("tomislav", sortedList.get(1).getName());
    assertEquals(2, sortedList.get(2).getValue());
    assertEquals("maulik", sortedList.get(2).getName());
    assertEquals(5, sortedList.get(3).getValue());
    assertEquals(27, sortedList.get(4).getValue());
  }

  public void testApplyMultipleNestedOrderings() {
    List<Object> data = Lists.newArrayList();
    data.add(new MyInt(2, "tomislav", new MyInt(2, "tomislav", null)));
    data.add(new MyInt(1, "bomb", new MyInt(1, "bomb", null)));
    data.add(new MyInt(27, "max", new MyInt(27, "max", null)));
    data.add(new MyInt(2, "maulik", new MyInt(2, "maulik", null)));
    data.add(new MyInt(5, "gut", new MyInt(5, "gut", null)));

    List<InMemoryOrderBy> list = Lists.newArrayList(
        new InMemoryOrderBy(null, Order.asc("innerMyInt.value")),
        new InMemoryOrderBy(null, Order.desc("innerMyInt.name"))
    );

    OrderExitOperation operation = new OrderExitOperation(list);
    List<MyInt> sortedList = (List<MyInt>)(List)operation.apply(data);

    assertEquals(1, sortedList.get(0).getValue());
    assertEquals(2, sortedList.get(1).getValue());
    assertEquals("tomislav", sortedList.get(1).getName());
    assertEquals(2, sortedList.get(2).getValue());
    assertEquals("maulik", sortedList.get(2).getName());
    assertEquals(5, sortedList.get(3).getValue());
    assertEquals(27, sortedList.get(4).getValue());
  }

  public void testApplyMultipleMixedOrderings() {
    List<Object> data = Lists.newArrayList();
    data.add(new MyInt(2, "tomislav", new MyInt(2, "tomislav", null)));
    data.add(new MyInt(1, "bomb", new MyInt(1, "bomb", null)));
    data.add(new MyInt(27, "max", new MyInt(27, "max", null)));
    data.add(new MyInt(2, "maulik", new MyInt(2, "maulik", null)));
    data.add(new MyInt(5, "gut", new MyInt(5, "gut", null)));

    List<InMemoryOrderBy> list = Lists.newArrayList(
        new InMemoryOrderBy(null, Order.asc("innerMyInt.value")),
        new InMemoryOrderBy(null, Order.desc("name"))
    );

    OrderExitOperation operation = new OrderExitOperation(list);
    List<MyInt> sortedList = (List<MyInt>)(List)operation.apply(data);

    assertEquals(1, sortedList.get(0).getValue());
    assertEquals(2, sortedList.get(1).getValue());
    assertEquals("tomislav", sortedList.get(1).getName());
    assertEquals(2, sortedList.get(2).getValue());
    assertEquals("maulik", sortedList.get(2).getName());
    assertEquals(5, sortedList.get(3).getValue());
    assertEquals(27, sortedList.get(4).getValue());
  }

  static class SessionFactoryMock extends SessionFactoryDefaultMock {

    public ClassMetadata getClassMetadata(Class persistentClass)
        throws HibernateException {
      return new ClassMetadataMock();
    }

    public EntityPersister getEntityPersister(String entityName)
        throws MappingException {
      return new EntityPersisterMock();
    }
  }

  static class ClassMetadataMock extends ClassMetadataDefaultMock {

    public String getEntityName() {
      return "";
    }
  }

  static class EntityPersisterMock extends EntityPersisterDefaultMock {

    public Object getPropertyValue(Object object, String propertyName,
        EntityMode entityMode) throws HibernateException {
      Class clazz = object.getClass();
      propertyName = StringUtil.capitalize(propertyName);
      try {
        Method m = clazz.getMethod("get" + propertyName);
        return m.invoke(object);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
