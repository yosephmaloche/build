/*
 * Copyright 2016 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.segmentrouting.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.TestApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.ConfigApplyDelegate;
import org.onosproject.segmentrouting.SegmentRoutingManager;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Tests for class {@link SegmentRoutingAppConfig}.
 */
public class SegmentRoutingAppConfigTest {
    private static final ApplicationId APP_ID =
            new TestApplicationId(SegmentRoutingManager.SR_APP_ID);

    private SegmentRoutingAppConfig config;
    private SegmentRoutingAppConfig invalidConfig;

    private static final MacAddress ROUTER_MAC_1 = MacAddress.valueOf("00:00:00:00:00:01");
    private static final MacAddress ROUTER_MAC_2 = MacAddress.valueOf("00:00:00:00:00:02");
    private static final MacAddress ROUTER_MAC_3 = MacAddress.valueOf("00:00:00:00:00:03");
    private static final ConnectPoint PORT_1 = ConnectPoint.deviceConnectPoint("of:1/1");
    private static final ConnectPoint PORT_2 = ConnectPoint.deviceConnectPoint("of:1/2");
    private static final ConnectPoint PORT_3 = ConnectPoint.deviceConnectPoint("of:1/3");
    private static final DeviceId VROUTER_ID_1 = DeviceId.deviceId("of:1");
    private static final DeviceId VROUTER_ID_2 = DeviceId.deviceId("of:2");

    /**
     * Initialize test related variables.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        InputStream jsonStream = SegmentRoutingAppConfigTest.class
                .getResourceAsStream("/sr-app-config.json");
        InputStream invalidJsonStream = SegmentRoutingAppConfigTest.class
                .getResourceAsStream("/sr-app-config-invalid.json");

        ApplicationId subject = APP_ID;
        String key = SegmentRoutingManager.SR_APP_ID;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonStream);
        JsonNode invalidJsonNode = mapper.readTree(invalidJsonStream);
        ConfigApplyDelegate delegate = new MockDelegate();

        config = new SegmentRoutingAppConfig();
        config.init(subject, key, jsonNode, mapper, delegate);
        invalidConfig = new SegmentRoutingAppConfig();
        invalidConfig.init(subject, key, invalidJsonNode, mapper, delegate);
    }

    /**
     * Tests config validity.
     *
     * @throws Exception
     */
    @Test
    public void testIsValid() throws Exception {
        assertTrue(config.isValid());
        assertFalse(invalidConfig.isValid());
    }

    /**
     * Tests vRouterMacs getter.
     *
     * @throws Exception
     */
    @Test
    public void testVRouterMacs() throws Exception {
        Set<MacAddress> vRouterMacs = config.vRouterMacs();
        assertNotNull("vRouterMacs should not be null", vRouterMacs);
        assertThat(vRouterMacs.size(), is(2));
        assertTrue(vRouterMacs.contains(ROUTER_MAC_1));
        assertTrue(vRouterMacs.contains(ROUTER_MAC_2));
    }

    /**
     * Tests vRouterMacs setter.
     *
     * @throws Exception
     */
    @Test
    public void testSetVRouterMacs() throws Exception {
        ImmutableSet.Builder<MacAddress> builder = ImmutableSet.builder();
        builder.add(ROUTER_MAC_3);
        config.setVRouterMacs(builder.build());

        Set<MacAddress> vRouterMacs = config.vRouterMacs();
        assertThat(vRouterMacs.size(), is(1));
        assertTrue(vRouterMacs.contains(ROUTER_MAC_3));
    }

    /**
     * Tests vRouterId getter.
     *
     * @throws Exception
     */
    @Test
    public void testVRouterId() throws Exception {
        Optional<DeviceId> vRouterId = config.vRouterId();
        assertTrue(vRouterId.isPresent());
        assertThat(vRouterId.get(), is(VROUTER_ID_1));
    }

    /**
     * Tests vRouterId setter.
     *
     * @throws Exception
     */
    @Test
    public void testSetVRouterId() throws Exception {
        config.setVRouterId(VROUTER_ID_2);

        Optional<DeviceId> vRouterId = config.vRouterId();
        assertTrue(vRouterId.isPresent());
        assertThat(vRouterId.get(), is(VROUTER_ID_2));
    }

    /**
     * Tests suppressSubnet getter.
     *
     * @throws Exception
     */
    @Test
    public void testSuppressSubnet() throws Exception {
        Set<ConnectPoint> suppressSubnet = config.suppressSubnet();
        assertNotNull("suppressSubnet should not be null", suppressSubnet);
        assertThat(suppressSubnet.size(), is(2));
        assertTrue(suppressSubnet.contains(PORT_1));
        assertTrue(suppressSubnet.contains(PORT_2));
    }

    /**
     * Tests suppressSubnet setter.
     *
     * @throws Exception
     */
    @Test
    public void testSetSuppressSubnet() throws Exception {
        ImmutableSet.Builder<ConnectPoint> builder = ImmutableSet.builder();
        builder.add(PORT_3);
        config.setSuppressSubnet(builder.build());

        Set<ConnectPoint> suppressSubnet = config.suppressSubnet();
        assertNotNull("suppressSubnet should not be null", suppressSubnet);
        assertThat(suppressSubnet.size(), is(1));
        assertTrue(suppressSubnet.contains(PORT_3));
    }

    /**
     * Tests suppressHost getter.
     *
     * @throws Exception
     */
    @Test
    public void testSuppressHost() throws Exception {
        Set<ConnectPoint> suppressHost = config.suppressHost();
        assertNotNull("suppressHost should not be null", suppressHost);
        assertThat(suppressHost.size(), is(2));
        assertTrue(suppressHost.contains(PORT_1));
        assertTrue(suppressHost.contains(PORT_2));
    }

    /**
     * Tests suppressHost setter.
     *
     * @throws Exception
     */
    @Test
    public void testSetSuppressHost() throws Exception {
        ImmutableSet.Builder<ConnectPoint> builder = ImmutableSet.builder();
        builder.add(PORT_3);
        config.setSuppressHost(builder.build());

        Set<ConnectPoint> suppressHost = config.suppressHost();
        assertNotNull("suppressHost should not be null", suppressHost);
        assertThat(suppressHost.size(), is(1));
        assertTrue(suppressHost.contains(PORT_3));
    }

    private class MockDelegate implements ConfigApplyDelegate {
        @Override
        public void onApply(Config config) {
        }
    }
}