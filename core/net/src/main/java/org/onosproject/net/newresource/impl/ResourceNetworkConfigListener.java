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
package org.onosproject.net.newresource.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.net.newresource.Resource.continuous;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.onlab.util.Bandwidth;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.newresource.BandwidthCapacity;
import org.onosproject.net.newresource.ResourceAdminService;
import org.slf4j.Logger;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;

// TODO Consider merging this with ResourceDeviceListener.
/**
 * Handler for NetworkConfiguration changes.
 */
@Beta
final class ResourceNetworkConfigListener implements NetworkConfigListener {

    /**
     *  Config classes relevant to this listener.
     */
    private static final Set<Class<?>> CONFIG_CLASSES = ImmutableSet.of(BandwidthCapacity.class);

    private final Logger log = getLogger(getClass());

    private final ResourceAdminService adminService;
    private final NetworkConfigService cfgService;
    private final ExecutorService executor;

    /**
     * Creates an instance of listener.
     *
     * @param adminService {@link ResourceAdminService}
     * @param cfgService {@link NetworkConfigService}
     * @param executor Executor to use.
     */
    ResourceNetworkConfigListener(ResourceAdminService adminService, NetworkConfigService cfgService,
                                         ExecutorService executor) {
        this.adminService = checkNotNull(adminService);
        this.cfgService = checkNotNull(cfgService);
        this.executor = checkNotNull(executor);
    }

    @Override
    public boolean isRelevant(NetworkConfigEvent event) {
        return CONFIG_CLASSES.contains(event.configClass());
    }

    @Override
    public void event(NetworkConfigEvent event) {
        if (event.configClass() == BandwidthCapacity.class) {
            executor.submit(() -> {
            try {
                handleBandwidthCapacity(event);
            } catch (Exception e) {
                log.error("Exception handling BandwidthCapacity", e);
            }
            });
        }
    }

    private void handleBandwidthCapacity(NetworkConfigEvent event) {
        checkArgument(event.configClass() == BandwidthCapacity.class);

        ConnectPoint cp = (ConnectPoint) event.subject();
        BandwidthCapacity bwCapacity = cfgService.getConfig(cp, BandwidthCapacity.class);

        switch (event.type()) {
        case CONFIG_ADDED:
            if (!adminService.registerResources(continuous(cp.deviceId(),
                                                           cp.port(), Bandwidth.class)
                    .resource(bwCapacity.capacity().bps()))) {
                log.info("Failed to register Bandwidth for {}, attempting update", cp);

                // Bandwidth based on port speed, was probably already registered.
                // need to update to the valued based on configuration

                if (!updateRegistration(cp, bwCapacity)) {
                    log.warn("Failed to update Bandwidth for {}", cp);
                }
            }
            break;

        case CONFIG_UPDATED:
            if (!updateRegistration(cp, bwCapacity)) {
                log.warn("Failed to update Bandwidth for {}", cp);
            }
            break;

        case CONFIG_REMOVED:
            // FIXME Following should be an update to the value based on port speed
            if (!adminService.unregisterResources(continuous(cp.deviceId(),
                                                             cp.port(),
                                                             Bandwidth.class).resource(0))) {
                log.warn("Failed to unregister Bandwidth for {}", cp);
            }
            break;

        case CONFIG_REGISTERED:
        case CONFIG_UNREGISTERED:
            // no-op
            break;

        default:
            break;
        }
    }

    private boolean updateRegistration(ConnectPoint cp, BandwidthCapacity bwCapacity) {
        // FIXME workaround until replace/update semantics become available
        // this potentially blows up existing registration
        // or end up as no-op
        //
        // Current code end up in situation like below:
        //        PortNumber: 2
        //        MplsLabel: [[16‥240)]
        //        VlanId: [[0‥4095)]
        //        Bandwidth: 2000000.000000
        //        Bandwidth: 20000000.000000
        //
        // but both unregisterResources(..) and  registerResources(..)
        // returns true (success)

        if (!adminService.unregisterResources(continuous(cp.deviceId(), cp.port(), Bandwidth.class).resource(0))) {
            log.warn("unregisterResources for {} failed", cp);
        }
        return adminService.registerResources(continuous(cp.deviceId(),
                                                         cp.port(),
                                                         Bandwidth.class).resource(bwCapacity.capacity().bps()));
    }

}
