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
package eu.europa.ec.fisheries.uvms.mobileterminal.timer;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollAttributeType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollResponseType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.mobileterminal.bean.PollServiceBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.MobileTerminalGenericMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.PollDataSourceRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class PollTimerTask implements Runnable{

    private final static Logger LOG = LoggerFactory.getLogger(PollTimerTask.class);
    private PollServiceBean pollService;

    public PollTimerTask(PollServiceBean pollService){
        this.pollService = pollService;
    }

    @Override
    public void run() {
        LOG.debug("PollProgram collected from DB at " + DateUtils.dateToEpochMilliseconds(Instant.now()));
        try {
            List<PollResponseType> pollPrograms = pollService.timer();

            for (PollResponseType pollProgram : pollPrograms) {
                String guid = pollProgram.getPollId().getGuid();
                Instant endDate = DateUtils.stringToDate(MobileTerminalGenericMapper.getPollAttributeTypeValue(
                        pollProgram.getAttributes(), PollAttributeType.END_DATE));

                // If the program has expired, archive it
                if (Instant.now().isAfter(endDate)) {
                    pollService.inactivateProgramPoll(guid, "MobileTerminalPollTimer");
                    LOG.info("Poll program {} has expired. Status set to ARCHIVED.", guid);
                } else {
                    pollService.createPoll(PollDataSourceRequestMapper.mapCreatePollRequest(pollProgram));
                    LOG.info("Poll created by poll program {}", guid);
                }
            }
        } catch (Exception e) {
            LOG.error("[ Poll scheduler failed. ] " + e);
        }
    }
}
