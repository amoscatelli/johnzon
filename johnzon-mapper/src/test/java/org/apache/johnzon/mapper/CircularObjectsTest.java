/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.johnzon.mapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test serialising objects which contain the same Object multiple times,
 * sometimes even with cycles.
 */
public class CircularObjectsTest {

    @Test
    public void testSimpleCyclicPerson() {
        Person john = new Person("John");
        Person marry = new Person("Marry");

        john.setMarriedTo(marry);
        marry.setMarriedTo(john);

        Mapper mapper = new MapperBuilder().setAccessModeName("field").build();
        String ser = mapper.writeObjectAsString(john);

        assertNotNull(ser);
        assertTrue(ser.contains("\"name\":\"John\""));
        assertTrue(ser.contains("\"marriedTo\":\"/\""));
        assertTrue(ser.contains("\"name\":\"Marry\""));

        // and now de-serialise it back
        Person john2 = mapper.readObject(ser, Person.class);
        assertNotNull(john2);
        assertEquals("John", john2.getName());

        Person marry2 = john2.getMarriedTo();
        assertNotNull(marry2);
        assertEquals("Marry", marry2.getName());

        assertEquals(john2, marry2.getMarriedTo());
    }

    @Test
    public void testComplexCyclicPerson() {
        Person karl = new Person("Karl");
        Person andrea = new Person("Andrea");
        Person lu = new Person("Lu");
        Person sue = new Person("Sue");

        karl.setMarriedTo(andrea);
        karl.getKids().add(lu);
        karl.getKids().add(sue);

        andrea.setMarriedTo(karl);
        andrea.getKids().add(lu);
        andrea.getKids().add(sue);

        lu.setFather(karl);
        lu.setMother(andrea);

        sue.setFather(karl);
        sue.setMother(andrea);

        Mapper mapper = new MapperBuilder().setAccessModeName("field").build();
        String karlJson = mapper.writeObjectAsString(karl);
        Person karl2 = mapper.readObject(karlJson, Person.class);
        assertEquals("Karl", karl2.getName());
        assertEquals("Andrea", karl2.getMarriedTo().getName());
        assertEquals(karl2, karl2.getMarriedTo().getMarriedTo());
        assertEquals(2, karl2.getKids().size());
        assertEquals("Lu", karl2.getKids().get(0).getName());
        assertEquals("Sue", karl2.getKids().get(1).getName());
        assertEquals(2, karl2.getMarriedTo().getKids().size());
        assertEquals("Lu", karl2.getMarriedTo().getKids().get(0).getName());
        assertEquals("Sue", karl2.getMarriedTo().getKids().get(1).getName());
    }

    public static class Person {
        private String name;
        private Person marriedTo;
        private Person mother;
        private Person father;
        private List<Person> kids = new ArrayList<>();

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Person getMarriedTo() {
            return marriedTo;
        }

        public void setMarriedTo(Person marriedTo) {
            this.marriedTo = marriedTo;
        }

        public Person getMother() {
            return mother;
        }

        public void setMother(Person mother) {
            this.mother = mother;
        }

        public Person getFather() {
            return father;
        }

        public void setFather(Person father) {
            this.father = father;
        }

        public List<Person> getKids() {
            return kids;
        }

        public void setKids(List<Person> kids) {
            this.kids = kids;
        }
    }

}