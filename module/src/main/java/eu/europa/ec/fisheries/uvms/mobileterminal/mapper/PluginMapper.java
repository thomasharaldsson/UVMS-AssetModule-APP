/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.mobileterminal.mapper;

import eu.europa.ec.fisheries.schema.mobileterminal.config.v1.*;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.Plugin;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.PluginCapability;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.PluginService;
import eu.europa.ec.fisheries.uvms.mobileterminal.constants.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminalPlugin;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminalPluginCapability;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.constants.MobileTerminalTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.OceanRegionEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.TerminalCapability;

import java.time.Instant;
import java.util.*;

public class PluginMapper {

    private PluginMapper () {}

    static Plugin mapEntityToModel(MobileTerminalPlugin entity) {
        Plugin model = new Plugin();
        model.setSatelliteType(entity.getPluginSatelliteType());
        model.setInactive(entity.getPluginInactive());
        model.setLabelName(entity.getName());
        model.setServiceName(entity.getPluginServiceName());
        return model;
    }

    public static MobileTerminalPlugin mapModelToEntity(MobileTerminalPlugin entity, PluginService model) {
        entity.setDescription(model.getLabelName());
        entity.setPluginServiceName(model.getServiceName());
        entity.setPluginSatelliteType(model.getSatelliteType());
        entity.setName(model.getLabelName());
        entity.setPluginInactive(model.isInactive());
        entity.setUpdatedBy(MobileTerminalConstants.UPDATE_USER);
        entity.setUpdateTime(Instant.now());
        if (entity.getCapabilities() == null) {
            entity.setCapabilities(new HashSet<>());
        }
        entity.getCapabilities().addAll(mapCapabilityModelToEntity(entity, model.getCapability()));
        return entity;
    }

    private static List<MobileTerminalPluginCapability> mapCapabilityModelToEntity(MobileTerminalPlugin parent, List<PluginCapability> capabilities) {
        List<MobileTerminalPluginCapability> capabilityList = new ArrayList<>();
        for (PluginCapability capability : capabilities) {
            MobileTerminalPluginCapability entity = new MobileTerminalPluginCapability();
            entity.setPlugin(parent.getId());
            entity.setName(capability.getName().name());
            entity.setValue(capability.getValue());
            entity.setUpdatedBy(MobileTerminalConstants.UPDATE_USER);
            entity.setUpdateTime(Instant.now());
            capabilityList.add(entity);
        }
        return capabilityList;
    }

    public static MobileTerminalPlugin mapModelToEntity(PluginService model) {
        MobileTerminalPlugin plugin = new MobileTerminalPlugin();
        return mapModelToEntity(plugin, model);
    }

    public static boolean equals(MobileTerminalPlugin entity, PluginService plugin) {
        if (!entity.getName().equalsIgnoreCase(plugin.getLabelName())) {
            return false;
        }
        if (entity.getPluginInactive() != plugin.isInactive()) {
            return false;
        }
        if (!entity.getPluginServiceName().equalsIgnoreCase(plugin.getServiceName())) {
            return false;
        }
        if (plugin.getCapability() != null && entity.getCapabilities() != null) {
            return checkCapabilityEquality(entity, plugin);
        } else if (plugin.getCapability() == null && entity.getCapabilities() != null) {
            return false;
        } else if (entity.getCapabilities() == null && plugin.getCapability() != null) {
            return false;
        }
        return true;
    }

    private static boolean checkCapabilityEquality(MobileTerminalPlugin entity, PluginService plugin) {
        boolean equals = true;
        if (plugin.getCapability().size() != entity.getCapabilities().size()) {
            equals = false;
        }
        for (PluginCapability capability : plugin.getCapability()) {
            for (MobileTerminalPluginCapability entityCapability : entity.getCapabilities()) {
                if (entityCapability.getName().equalsIgnoreCase(capability.getName().name())
                        && !entityCapability.getValue().equalsIgnoreCase(capability.getValue())) {
                        equals = false;
                }
            }
        }
        return equals;
    }

    public static TerminalSystemConfiguration mapTerminalFieldConfiguration(MobileTerminalTypeEnum type) {
        TerminalSystemConfiguration configuration = new TerminalSystemConfiguration();

        switch (type) {
            default:
            case INMARSAT_C:
                for (AttributeInmarsatC attribute : AttributeInmarsatC.values()) {
                    configuration.getAttribute().add(attribute.toString());
                }
                break;
            case IRIDIUM:
                for (AttributeIridium attribute : AttributeIridium.values()) {
                    configuration.getAttribute().add(attribute.toString());
                }
                break;
        }
        return configuration;
    }

    public static TerminalSystemConfiguration mapComchannelFieldConfiguration(MobileTerminalTypeEnum type) {
        TerminalSystemConfiguration configuration = new TerminalSystemConfiguration();

        switch (type) {
            default:
            case INMARSAT_C:
                for (ChannelFieldInmarsatC attribute : ChannelFieldInmarsatC.values()) {
                    configuration.getAttribute().add(attribute.toString());
                }
                break;
            case IRIDIUM:
                for (ChannelFieldIridium attribute : ChannelFieldIridium.values()) {
                    configuration.getAttribute().add(attribute.toString());
                }
                break;
        }
        return configuration;
    }

    public static CapabilityConfiguration mapCapabilityConfiguration(MobileTerminalTypeEnum type, List<MobileTerminalPlugin> plugins) {
        CapabilityConfiguration capabilityConfiguration = new CapabilityConfiguration();
        List<Capability> capabilities = new ArrayList<>();
        List<OceanRegionEnum> oceanRegionList = Arrays.asList(OceanRegionEnum.values());
        for (TerminalCapability capabilityType : TerminalCapability.values()) {
            boolean hasCapability;
            switch (type) {
                default:
                case INMARSAT_C:
                    hasCapability = CapabilitiesInmarsatC.getCapability(capabilityType);
                    break;
                case IRIDIUM:
                    hasCapability = CapabilitiesIridium.getCapability(capabilityType);
                    oceanRegionList = null;
                    break;
            }
            if (hasCapability) {
                Capability capability = new Capability();
                capability.setName(capabilityType.name());
                List<? extends CapabilityOption> options = mapCapabilityOption(capabilityType, oceanRegionList, plugins);
                if (options != null) {
                    capability.getOptions().addAll(options);
                }
                capabilities.add(capability);
            }
        }
        capabilityConfiguration.getCapability().addAll(capabilities);
        return capabilityConfiguration;
    }

    private static List<? extends CapabilityOption> mapCapabilityOption(TerminalCapability capabilityValue, List<OceanRegionEnum> oceanRegionList, List<MobileTerminalPlugin> lesList) {
        switch (capabilityValue) {
            case SUPPORT_SINGLE_OCEAN:
            case SUPPORT_MULTIPLE_OCEAN:
                return mapOceanRegions(oceanRegionList);
            case PLUGIN:
                return mapLandEarthStation(lesList);
            default:
                return Collections.emptyList();
        }
    }

    private static List<? extends CapabilityOption> mapLandEarthStation(List<MobileTerminalPlugin> lesList) {
        List<LandEarthStationType> landEarthStations = new ArrayList<>();
        if (lesList != null) {
            for (MobileTerminalPlugin les : lesList) {
                LandEarthStationType type = new LandEarthStationType();
                type.setLabelName(les.getName());
                type.setServiceName(les.getPluginServiceName());
                landEarthStations.add(type);
            }
        }
        return landEarthStations;
    }

    private static List<? extends CapabilityOption> mapOceanRegions(List<OceanRegionEnum> oceanRegionList) {
        List<OceanRegionType> oceanRegions = new ArrayList<>();
        if (oceanRegionList != null) {
            for (OceanRegionEnum oceanRegion : oceanRegionList) {
                OceanRegionType type = new OceanRegionType();
                type.setCode(oceanRegion.getCode());
                type.setName(oceanRegion.getName());
                oceanRegions.add(type);
            }
        }
        return oceanRegions;
    }
}
