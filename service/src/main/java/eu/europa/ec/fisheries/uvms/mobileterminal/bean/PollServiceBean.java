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
package eu.europa.ec.fisheries.uvms.mobileterminal.bean;

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.*;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.PluginCapabilityType;
import eu.europa.ec.fisheries.uvms.asset.message.AuditProducer;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.ChannelDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.PollDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.PollProgramDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.TerminalDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.PollChannelDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.PollChannelListDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.PollDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.MobileTerminalTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.mapper.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.PollSearchKeyValue;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.poll.PollSearchMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;

@Stateless
@LocalBean
public class PollServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(PollServiceBean.class);

    @Inject
    private AuditProducer auditProducer;

    @Inject
    private MobileTerminalServiceBean mobileTerminalServiceBean;

    @EJB
    private PluginServiceBean sendPollService;

    @EJB
    private PollDaoBean pollDao;

    @EJB
    private PollProgramDaoBean pollProgramDao;

    @EJB
    private TerminalDaoBean terminalDao;

    @EJB
    private ChannelDaoBean channelDao;

    public CreatePollResultDto createPollForAsset(UUID assetId, PollType pollType, String username, String comment){
        MobileTerminal mt = mobileTerminalServiceBean.getActiveMTForAsset(assetId);

        if(mt == null){
            throw new IllegalArgumentException("No active MT for this asset, unable to poll");    //if we dont have an MT it is very hard to poll it.....
        }
        Channel channel = mobileTerminalServiceBean.getPollableChannel(mt);
        if(channel == null){
            throw new IllegalArgumentException("No pollable channel for this active MT, unable to poll");    //if we dont have a channel it is very hard to poll it.....
        }

        PollRequestType prt = new PollRequestType();
        prt.setPollType(pollType);
        PollMobileTerminal pollMobileTerminal = new PollMobileTerminal();
        pollMobileTerminal.setConnectId(assetId.toString());
        pollMobileTerminal.setMobileTerminalId(mt.getId().toString());
        pollMobileTerminal.setComChannelId(channel.getId().toString());
        prt.setUserName(username);
        prt.setComment(comment);
        prt.getMobileTerminals().add(pollMobileTerminal);

        return createPoll(prt);
    }

    public CreatePollResultDto createPoll(PollRequestType poll) {
        List<PollResponseType> createdPolls = validateAndCreatePolls(poll);
        List<String> unsentPolls = new ArrayList<>();
        List<String> sentPolls = new ArrayList<>();
        for (PollResponseType createdPoll : createdPolls) {
            if (PollType.PROGRAM_POLL.equals(createdPoll.getPollType())) {
                unsentPolls.add(createdPoll.getPollId().getGuid());
            } else {
                AcknowledgeTypeType ack = sendPollService.sendPoll(createdPoll);
                switch (ack) {
                    case NOK:
                        unsentPolls.add(createdPoll.getPollId().getGuid());
                        break;
                    case OK:
                        sentPolls.add(createdPoll.getPollId().getGuid());
                        break;
                }
            }
            try {
                String auditData = AuditModuleRequestMapper.mapAuditLogPollCreated(createdPoll.getPollType(), createdPoll.getPollId().getGuid(), createdPoll.getComment(), createdPoll.getUserName());
                auditProducer.sendModuleMessage(auditData);
            } catch (Exception e) {
                LOG.error("Failed to send audit log message! Poll with guid {} was created", createdPoll.getPollId().getGuid());
            }
        }

        CreatePollResultDto result = new CreatePollResultDto();
        result.setSentPolls(sentPolls);
        result.setUnsentPolls(unsentPolls);
        result.setUnsentPoll(!unsentPolls.isEmpty());
        return result;
    }

    public List<PollDto> getRunningProgramPolls() {
        List<PollProgram> pollPrograms = pollProgramDao.getProgramPollsAlive();
        List<PollResponseType> pollResponse = getResponseList(pollPrograms);

        return PollMapper.mapPolls(pollResponse);
    }

    public PollResponseType startProgramPoll(String pollId, String username) {

        PollId pollIdType = new PollId();
        pollIdType.setGuid(pollId);
        PollResponseType startedPoll = setStatusPollProgram(pollIdType, PollStatus.STARTED);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogProgramPollStarted(startedPoll.getPollId().getGuid(), username);
            auditProducer.sendModuleMessage(auditData);
        } catch (Exception e) {
            LOG.error("Failed to send audit log message due tue: " + e + "! Poll with guid {} was started", startedPoll.getPollId().getGuid());
        }
        return startedPoll;
    }

    public PollResponseType stopProgramPoll(String pollId, String username){

        PollId pollIdType = new PollId();
        pollIdType.setGuid(pollId);
        PollResponseType stoppedPoll = setStatusPollProgram(pollIdType, PollStatus.STOPPED);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogProgramPollStopped(stoppedPoll.getPollId().getGuid(), username);
            auditProducer.sendModuleMessage(auditData);
        } catch (Exception e) {
            LOG.error("Failed to send audit log message due tue: " + e + "! Poll with guid {} was stopped", stoppedPoll.getPollId().getGuid());
        }
        return stoppedPoll;
    }

    public PollResponseType inactivateProgramPoll(String pollId, String username){

        PollId pollIdType = new PollId();
        pollIdType.setGuid(pollId);
        PollResponseType inactivatedPoll = setStatusPollProgram(pollIdType, PollStatus.ARCHIVED);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogProgramPollInactivated(inactivatedPoll.getPollId().getGuid(), username);
            auditProducer.sendModuleMessage(auditData);
        } catch (Exception e) {
            LOG.error("Failed to send audit log message due tue: " + e + "! Poll with guid {} was inactivated", inactivatedPoll.getPollId().getGuid());
        }
        return inactivatedPoll;
    }

    public PollChannelListDto getPollBySearchCriteria(PollListQuery query) {

        PollChannelListDto channelListDto = new PollChannelListDto();
        PollListResponse pollResponse = getPollList(query);    //this is where the magic happens, rest of the method is just a mapper
        channelListDto.setCurrentPage(pollResponse.getCurrentPage());
        channelListDto.setTotalNumberOfPages(pollResponse.getTotalNumberOfPages());

        ArrayList<PollChannelDto> pollChannelList = new ArrayList<>();
        for(PollResponseType responseType : pollResponse.getPollList()) {
            PollChannelDto terminal = PollMapper.mapPollChannel(responseType.getMobileTerminal());
            terminal.setPoll(PollMapper.mapPoll(responseType));
            pollChannelList.add(terminal);
        }
        channelListDto.setPollableChannels(pollChannelList);
        return channelListDto;
    }

    public List<PollResponseType> timer() {
        return getPollProgramRunningAndStarted();
    }

    private MobileTerminal mapPollableTerminal(MobileTerminalTypeEnum type, UUID guid) {
        MobileTerminal terminal = terminalDao.getMobileTerminalById(guid);
        return terminal;
    }

    private void checkPollable(MobileTerminal terminal){
        if (terminal.getArchived()) {
            throw new IllegalStateException("Terminal is archived");
        }
        if (!terminal.getActive()) {
            throw new IllegalStateException("Terminal is inactive");
        }
        if (terminal.getPlugin() != null && terminal.getPlugin().getPluginInactive()) {
            throw new IllegalStateException("Terminal connected to no longer active Plugin (LES)");
        }
    }

    private List<PollResponseType> validateAndCreatePolls(PollRequestType pollRequest) {
        validatePollRequest(pollRequest);
        List<PollResponseType> responseList;
        Map<Poll, MobileTerminal> pollMobileTerminalMap;
        switch (pollRequest.getPollType()) {
            case PROGRAM_POLL:
                Map<PollProgram, MobileTerminal> pollProgramMobileTerminalTypeMap = validateAndMapToProgramPolls(pollRequest);
                responseList = createPollPrograms(pollProgramMobileTerminalTypeMap);
                break;
            case CONFIGURATION_POLL:
            case MANUAL_POLL:
            case AUTOMATIC_POLL:
            case SAMPLING_POLL:
                pollMobileTerminalMap = validateAndMapToPolls(pollRequest);
                responseList = createPolls(pollMobileTerminalMap, pollRequest.getPollType());
                break;
            default:
                LOG.error("[ Could not decide poll type ] {}", pollRequest.getPollType());
                throw new IllegalArgumentException("Could not decide Poll Type when creating polls");
        }
        return responseList;
    }

    private void validatePollRequest(PollRequestType pollRequest) {
        if (pollRequest == null || pollRequest.getPollType() == null) {
            throw new NullPointerException("No polls to create");
        }
        if (pollRequest.getComment() == null || pollRequest.getUserName() == null) {
            throw new NullPointerException("Cannot create without comment and user");
        }
        if (pollRequest.getMobileTerminals().isEmpty()) {
            throw new IllegalArgumentException("No mobile terminals for " + pollRequest.getPollType());
        }
    }

    private Map<PollProgram, MobileTerminal> validateAndMapToProgramPolls(PollRequestType pollRequest) {
        Map<PollProgram, MobileTerminal> map = new HashMap<>();

        for (PollMobileTerminal pollTerminal : pollRequest.getMobileTerminals()) {
            MobileTerminal mobileTerminalEntity = terminalDao.getMobileTerminalById(UUID.fromString(pollTerminal.getMobileTerminalId()));
            if(mobileTerminalEntity == null){
                throw new IllegalArgumentException("No mobile terminal connected to this poll request or the mobile terminal can not be found, for mobile terminal id: " + pollTerminal.getMobileTerminalId());
            }
            String connectId = mobileTerminalEntity.getAsset().getId().toString();
            if (!pollTerminal.getConnectId().equals(connectId)) {
                throw new IllegalStateException("Terminal " + mobileTerminalEntity.getId() + " can not be polled, because it is not linked to asset " + connectId);
            }
            checkPollable(mobileTerminalEntity);
            PollProgram pollProgram = PollModelToEntityMapper.mapToProgramPoll(mobileTerminalEntity, pollTerminal.getComChannelId(), pollRequest);
            map.put(pollProgram, mobileTerminalEntity);
        }
        return map;
    }

    private Map<Poll, MobileTerminal> validateAndMapToPolls(PollRequestType pollRequest) {
        Map<Poll, MobileTerminal> map = new HashMap<>();

        for (PollMobileTerminal pollTerminal : pollRequest.getMobileTerminals()) {
            MobileTerminal mobileTerminalEntity = terminalDao.getMobileTerminalById(UUID.fromString(pollTerminal.getMobileTerminalId()));
            if(mobileTerminalEntity == null){
                throw new IllegalArgumentException("No mobile terminal connected to this poll request or the mobile terminal can not be found, for mobile terminal id: " + pollTerminal.getMobileTerminalId());
            }
            String connectId = mobileTerminalEntity.getAsset().getId().toString();
            if (pollTerminal.getConnectId() == null || !pollTerminal.getConnectId().equals(connectId)) {
                throw new IllegalStateException("Terminal " + mobileTerminalEntity.getId() + " can not be polled, because it is not linked to asset " + connectId);
            }

            if (pollRequest.getPollType() != PollType.MANUAL_POLL && pollRequest.getPollType() != PollType.AUTOMATIC_POLL) {
                validateMobileTerminalPluginCapability(mobileTerminalEntity.getPlugin().getCapabilities(), pollRequest.getPollType(), mobileTerminalEntity.getPlugin().getPluginServiceName());
            }
            checkPollable(mobileTerminalEntity);
            Poll poll = PollModelToEntityMapper.mapToPoll(mobileTerminalEntity, pollTerminal.getComChannelId(), pollRequest);
            map.put(poll, mobileTerminalEntity);
        }
        return map;
    }

    private void validateMobileTerminalPluginCapability (Set<MobileTerminalPluginCapability> capabilities, PollType pollType, String pluginServiceName) {
        PluginCapabilityType pluginCapabilityType;
        switch (pollType) {
            case CONFIGURATION_POLL:
                pluginCapabilityType = PluginCapabilityType.CONFIGURABLE;
                break;
            case SAMPLING_POLL:
                pluginCapabilityType = PluginCapabilityType.SAMPLING;
                break;
            default:
                throw new IllegalArgumentException("Cannot create " + pollType.name() + "  poll when plugin: " + pluginServiceName);
        }
        if (!validatePluginHasCapabilityConfigurable(capabilities, pluginCapabilityType)) {
            throw new IllegalArgumentException("Cannot create " + pollType.name() + "  poll when plugin: " + pluginServiceName + " has not capability " + pluginCapabilityType.name() + " set");
        }
    }

    private boolean validatePluginHasCapabilityConfigurable (Set<MobileTerminalPluginCapability> capabilities, PluginCapabilityType pluginCapability) {
        for (MobileTerminalPluginCapability pluginCap : capabilities) {
            if (pluginCapability.name().equalsIgnoreCase(pluginCap.getName())) {
                return true;
            }
        }
        return false;
    }

    private List<PollResponseType> createPollPrograms (Map<PollProgram, MobileTerminal> map) {
        List<PollResponseType> responseList = new ArrayList<>();
        for (Map.Entry<PollProgram, MobileTerminal> next : map.entrySet()) {
            PollProgram pollProgram = next.getKey();
            MobileTerminal mobileTerminalType = next.getValue();
            pollProgramDao.createPollProgram(pollProgram);
            responseList.add(PollEntityToModelMapper.mapToPollResponseType(pollProgram, mobileTerminalType));
        }
        return responseList;
    }

    private List<PollResponseType> createPolls(Map<Poll, MobileTerminal> map, PollType pollType) {
        List<PollResponseType> responseList = new ArrayList<>();
        for (Map.Entry<Poll, MobileTerminal> next : map.entrySet()) {
            Poll poll = next.getKey();
            MobileTerminal mobileTerminal = next.getValue();
            pollDao.createPoll(poll);
            PollResponseType pollResponseType = PollEntityToModelMapper.mapToPollResponseType(poll, mobileTerminal, pollType);
            responseList.add(pollResponseType);
        }
        return responseList;
    }

    public PollListResponse getPollList(PollListQuery query) {
        if (query == null) {
            throw new NullPointerException("Cannot get poll list because no query.");
        }

        if (query.getPagination() == null) {
            throw new NullPointerException("Cannot get poll list because no list pagination.");
        }

        if (query.getPollSearchCriteria() == null || query.getPollSearchCriteria().getCriterias() == null) {
            throw new NullPointerException("Cannot get poll list because criteria are null.");
        }
        PollListResponse response = new PollListResponse();
        List<PollResponseType> pollResponseList = new ArrayList<>();

        Integer page = query.getPagination().getPage();
        Integer listSize = query.getPagination().getListSize();
        boolean isDynamic = query.getPollSearchCriteria().isIsDynamic();
        List<PollSearchKeyValue> searchKeys = PollSearchMapper.createSearchFields(query.getPollSearchCriteria().getCriterias());

        String countSql = PollSearchMapper.createCountSearchSql(searchKeys, isDynamic);
        String sql = PollSearchMapper.createSelectSearchSql(searchKeys, isDynamic);

        Long numberMatches = pollDao.getPollListSearchCount(countSql, searchKeys);
        List<Poll> pollList = pollDao.getPollListSearchPaginated(page, listSize, sql, searchKeys);

        for (Poll poll : pollList) {
            try {
                MobileTerminal mobileTerminalEntity = poll.getPollBase().getMobileterminal();
                MobileTerminal mobileTerminal = mapPollableTerminal(mobileTerminalEntity.getMobileTerminalType(), mobileTerminalEntity.getId());
                PollResponseType pollType = PollEntityToModelMapper.mapToPollResponseType(poll, mobileTerminal, EnumMapper.getPollModelFromType(poll.getPollType()));
                pollResponseList.add(pollType);
            } catch (RuntimeException e) {
                LOG.error("[ Poll " + poll.getId() + "  couldn't map type ]");
                throw new RuntimeException(e);
            }
        }

        int numberOfPages = (int) (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        response.setTotalNumberOfPages(numberOfPages);
        response.setCurrentPage(query.getPagination().getPage());
        response.getPollList().addAll(pollResponseList);
        return response;
    }

    public List<PollResponseType> getPollProgramRunningAndStarted() {
        List<PollProgram> pollPrograms = pollProgramDao.getPollProgramRunningAndStarted();
        return getResponseList(pollPrograms);
    }

    public PollResponseType setStatusPollProgram(PollId id, PollStatus state) {
        if (id == null || id.getGuid() == null || id.getGuid().isEmpty()) {
            throw new NullPointerException("No poll id given");
        }
        if (state == null) {
            throw new NullPointerException("No status to set");
        }

        PollProgram program = pollProgramDao.getPollProgramByGuid(id.getGuid());
        MobileTerminal terminal = program.getPollBase().getMobileterminal();
        MobileTerminal mobileTerminal = mapPollableTerminal(terminal.getMobileTerminalType(), terminal.getId());

        switch (program.getPollState()) {
            case ARCHIVED:
                throw new IllegalArgumentException("Can not change status of archived program poll, id: [ " + id.getGuid() + " ]");
            case STARTED:
            case STOPPED:
        }

        // TODO: check terminal/comchannel?

        program.setPollState(EnumMapper.getPollStateTypeFromModel(state));

        return PollEntityToModelMapper.mapToPollResponseType(program, mobileTerminal);
    }

    public ListResponseDto getMobileTerminalPollableList(PollableQuery query) {
        if (query == null) {
            throw new NullPointerException("No query");
        }

        if (query.getPagination() == null) {
            throw new NullPointerException("No list pagination");
        }

        ListResponseDto response = new ListResponseDto();
        List<MobileTerminalType> mobileTerminalList = new ArrayList<>();

        int page = query.getPagination().getPage();
        int listSize = query.getPagination().getListSize();
        int startIndex = (page - 1) * listSize;
        int stopIndex = startIndex + listSize;
        LOG.debug("page: " + page + ", listSize: " + listSize + ", startIndex: " + startIndex);

        List<String> idList = query.getConnectIdList();
        long in = System.currentTimeMillis();

        List<Channel> channels = channelDao.getPollableListSearch(idList);

        for (Channel comchannel : channels) {
            //TODO slim response from Pollable
            MobileTerminalType terminal = MobileTerminalEntityToModelMapper.mapToMobileTerminalType(comchannel.getMobileTerminal());
            mobileTerminalList.add(terminal);
        }

        int numberMatches = mobileTerminalList.size();

        int numberOfPages = (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        if ((numberMatches - 1) <= 0) {
            response.setMobileTerminalList(mobileTerminalList);
        } else {
            if (stopIndex >= numberMatches) {
                stopIndex = numberMatches;
            }
            LOG.debug("stopIndex: " + stopIndex);
            List<MobileTerminalType> newList = new ArrayList<>(mobileTerminalList.subList(startIndex, stopIndex));
            response.setMobileTerminalList(newList);
        }

        response.setTotalNumberOfPages(numberOfPages);
        response.setCurrentPage(query.getPagination().getPage());

        long out = System.currentTimeMillis();
        LOG.debug("Get pollable channels " + (out - in) + " ms");
        return response;
    }

    private List<PollResponseType> getResponseList(List<PollProgram> pollPrograms)  {
        List<PollResponseType> responseList = new ArrayList<>();
        for (PollProgram pollProgram : pollPrograms) {
                MobileTerminal terminal = pollProgram.getPollBase().getMobileterminal();
            MobileTerminal mobileTerminal = mapPollableTerminal(terminal.getMobileTerminalType(), terminal.getId());
            responseList.add(PollEntityToModelMapper.mapToPollResponseType(pollProgram, mobileTerminal));
        }
        return responseList;
    }
}