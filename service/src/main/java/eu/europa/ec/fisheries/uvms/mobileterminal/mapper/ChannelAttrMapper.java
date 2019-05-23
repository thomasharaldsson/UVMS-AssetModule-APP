/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 © European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.mobileterminal.mapper;

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.ComChannelAttribute;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.Channel;
import eu.europa.ec.fisheries.uvms.mobileterminal.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osdjup on 2016-11-16.
 */
public class ChannelAttrMapper {

    static List<ComChannelAttribute> mapEntityToModel(Channel channel) {
        List<ComChannelAttribute> attributeList = new ArrayList<>();
        attributeList.add(mapAttr("DNID",channel.getDNID()));
        attributeList.add(mapAttr("FREQUENCY_EXPECTED", String.valueOf(channel.getExpectedFrequency().getSeconds())));
        attributeList.add(mapAttr("FREQUENCY_IN_PORT", String.valueOf(channel.getExpectedFrequencyInPort().getSeconds())));
        attributeList.add(mapAttr("LES_DESCRIPTION", channel.getLesDescription()));
        attributeList.add(mapAttr("FREQUENCY_GRACE_PERIOD", String.valueOf(channel.getFrequencyGracePeriod().getSeconds())));
        attributeList.add(mapAttr("MEMBER_NUMBER", channel.getMemberNumber()));
        attributeList.add(mapAttr("INSTALLED_BY", channel.getInstalledBy()));
        attributeList.add(mapAttr("INSTALLED_ON", DateUtils.parseOffsetDateTimeToString(channel.getInstallDate())));
        attributeList.add(mapAttr("UNINSTALLED_ON", DateUtils.parseOffsetDateTimeToString(channel.getUninstallDate())));
        attributeList.add(mapAttr("START_DATE", DateUtils.parseOffsetDateTimeToString(channel.getStartDate())));
        attributeList.add(mapAttr("END_DATE", DateUtils.parseOffsetDateTimeToString(channel.getEndDate())));
        return attributeList;
    }

    private static ComChannelAttribute mapAttr(String key, String value){
        ComChannelAttribute attr = new ComChannelAttribute();
        attr.setType(key);
        attr.setValue(value);
        return attr;
    }
}
