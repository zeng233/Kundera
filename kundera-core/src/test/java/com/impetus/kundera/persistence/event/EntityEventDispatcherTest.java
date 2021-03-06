/*******************************************************************************
 * * Copyright 2013 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.kundera.persistence.event;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PostLoad;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.impetus.kundera.metadata.KunderaMetadataManager;
import com.impetus.kundera.metadata.model.EntityMetadata;
import com.impetus.kundera.metadata.model.KunderaMetadata;
import com.impetus.kundera.query.Person;
//import com.sun.jndi.cosnaming.IiopUrl.Address;

/**
 * Test case for {@link EntityEventDispatcher}
 * 
 * @author amresh.singh
 * 
 */
public class EntityEventDispatcherTest
{

    private EntityEventDispatcher eventDispatcher;

    private EntityManager em;

    private EntityManagerFactory emf;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        eventDispatcher = new EntityEventDispatcher();
        KunderaMetadata.INSTANCE.setApplicationMetadata(null);
        emf = Persistence.createEntityManagerFactory("kunderatest");
        em = emf.createEntityManager();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        eventDispatcher = null;
        em.close();
        emf.close();
    }

    /**
     * Test method for
     * {@link com.impetus.kundera.persistence.event.EntityEventDispatcher#fireEventListeners(com.impetus.kundera.metadata.model.EntityMetadata, java.lang.Object, java.lang.Class)}
     * .
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    @Test
    public void testExternalFireEventListeners() throws NoSuchMethodException, SecurityException
    {
        PersonEventDispatch person = new PersonEventDispatch("1", "John", "Smith");
        EntityMetadata m = KunderaMetadataManager.getEntityMetadata(person.getClass());
        eventDispatcher.fireEventListeners(m, person, PrePersist.class);
        System.out.println(person.getFirstName());
        Assert.assertEquals("Amresh", person.getFirstName());
        Assert.assertEquals("Smith", person.getLastName());
        eventDispatcher.fireEventListeners(m, person, PostPersist.class);
        System.out.println(person.getLastName());
        Assert.assertEquals("Amresh", person.getFirstName());
        Assert.assertEquals("Singh", person.getLastName());
        
        eventDispatcher.fireEventListeners(m, person, PostLoad.class);
        System.out.println(person.getLastName());
        Assert.assertEquals("Amresh", person.getFirstName());
        Assert.assertEquals("Post Load", person.getLastName());
        
        try
        {
        ExternalCallbackMethod callback = new ExternalCallbackMethod(PersonEventDispatch.class,PersonEventDispatch.class.getDeclaredMethod("getFirstName",null));
        AddressEntity address = new AddressEntity();
        address.setAddressId("addr1");
        address.setStreet("street");
        address.setCity("noida");
        callback.invoke(address);
        Assert.fail("Should have gone to catch block!");
        }catch(EventListenerException elex)
        {
            Assert.assertNotNull(elex);
        }
    }

    /**
     * Test method for
     * {@link com.impetus.kundera.persistence.event.EntityEventDispatcher#fireEventListeners(com.impetus.kundera.metadata.model.EntityMetadata, java.lang.Object, java.lang.Class)}
     * .
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    @Test
    public void testInternalFireEventListeners() throws NoSuchMethodException, SecurityException
    {
        AddressEntity address = new AddressEntity();
        address.setAddressId("addr1");
        address.setStreet("street");
        address.setCity("noida");
        
        EntityMetadata m = KunderaMetadataManager.getEntityMetadata(address.getClass());
        eventDispatcher.fireEventListeners(m, address, PrePersist.class);
        Assert.assertEquals("addr1", address.getAddressId());
        Assert.assertEquals("noida", address.getCity());
        Assert.assertEquals("street,noida", address.getFullAddress());
        
        try
        {
            InternalCallbackMethod callback = new InternalCallbackMethod(m, AddressEntity.class.getDeclaredMethod(
                    "getStreet", null));
            PersonEventDispatch person = new PersonEventDispatch("1", "John", "Smith");
            callback.invoke(person);
            Assert.fail("Should have gone to catch block!");
        }
        catch (EventListenerException elex)
        {
            Assert.assertNotNull(elex);
        }
    }

}
