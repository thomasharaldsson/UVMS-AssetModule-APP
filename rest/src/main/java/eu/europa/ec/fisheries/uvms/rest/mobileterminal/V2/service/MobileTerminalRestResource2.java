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
package eu.europa.ec.fisheries.uvms.rest.mobileterminal.V2.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset;
import eu.europa.ec.fisheries.uvms.mobileterminal.bean.MobileTerminalServiceBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dao.MobileTerminalPluginDaoBean;
import eu.europa.ec.fisheries.uvms.mobileterminal.dto.MTListResponse;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminal;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminalPlugin;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.MobileTerminalStatus;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.TerminalSourceEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.MTSearchKeyValue;
import eu.europa.ec.fisheries.uvms.rest.asset.ObjectMapperContextResolver;
import eu.europa.ec.fisheries.uvms.rest.asset.mapper.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.rest.mobileterminal.dto.MTQuery;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/mobileterminal2")
@Stateless
@Consumes(value = { MediaType.APPLICATION_JSON })
@Produces(value = { MediaType.APPLICATION_JSON })
public class MobileTerminalRestResource2 {

    private final static Logger LOG = LoggerFactory.getLogger(MobileTerminalRestResource2.class);

    @EJB
    private MobileTerminalServiceBean mobileTerminalService;

    @Inject
    private MobileTerminalPluginDaoBean pluginDao;

    @Context
    private HttpServletRequest request;


    @POST
    @Path("/")
    @RequiresFeature(UnionVMSFeature.manageMobileTerminals)
    public Response createMobileTerminal(MobileTerminal terminal) {
        LOG.info("Create mobile terminal invoked in rest layer.");
        LOG.info("MobileTerminalType: SHORT_PREFIX_STYLE", terminal.toString());
        try {
            if(terminal.getId() != null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Given MobileTerminal is already persisted in DB.").build();
            }

            terminal.setSource(TerminalSourceEnum.INTERNAL);

            MobileTerminalPlugin plugin = pluginDao.getPluginByServiceName(terminal.getPlugin().getPluginServiceName());
            terminal.setPlugin(plugin);
            terminal = mobileTerminalService.populateAssetInMT(terminal);

            MobileTerminal mobileTerminal = mobileTerminalService.createMobileTerminal(terminal, request.getRemoteUser());
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when creating mobile terminal ] {}", ex, ex.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @GET
    @Path("/{id}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getMobileTerminalById(@PathParam("id") UUID guid) {
        LOG.info("Get mobile terminal by id invoked in rest layer.");
        try {
            MobileTerminal mobileTerminal = mobileTerminalService.getMobileTerminalEntityById(guid);
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when fetching mobile terminal ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @PUT
    @Path("/")
    @RequiresFeature(UnionVMSFeature.manageMobileTerminals)
    public Response updateMobileTerminal(MobileTerminal terminal) {
        LOG.info("Update mobile terminal by id invoked in rest layer.");
        try {
            terminal.setSource(TerminalSourceEnum.INTERNAL);
            mobileTerminalService.assertTerminalHasSerialNumber(terminal);
            MobileTerminalPlugin plugin = pluginDao.getPluginByServiceName(terminal.getPlugin().getPluginServiceName());
            terminal = mobileTerminalService.populateAssetInMT(terminal);

            MobileTerminal mobileTerminal = mobileTerminalService.updateMobileTerminal(terminal, terminal.getComment(), request.getRemoteUser());
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when updating mobile terminal ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getMobileTerminalList(MTQuery query,
                                          @DefaultValue("1") @QueryParam("page") int page,
                                          @DefaultValue("1000000") @QueryParam("size") int size,
                                          @DefaultValue("true") @QueryParam("dynamic") boolean dynamic,
                                          @DefaultValue("false") @QueryParam("includeArchived") boolean includeArchived) {
        LOG.info("Get mobile terminal list invoked in rest layer.");
        try {
            List<MTSearchKeyValue> searchFields = SearchFieldMapper.createSearchFields(query);
            MTListResponse mobileTerminalList = mobileTerminalService.getMobileTerminalList(searchFields, page, size, dynamic, includeArchived);
            String returnJson = objectMapper().writeValueAsString(mobileTerminalList);
            LOG.debug(returnJson);
            return Response.ok(returnJson).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting mobile terminal list ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @PUT
    @Path("/{mtId}/assign/{assetId}")
    @RequiresFeature(UnionVMSFeature.manageMobileTerminals)
    public Response assignMobileTerminal(@QueryParam("comment") String comment,
                                         @PathParam("assetId") UUID assetId,
                                         @PathParam("mtId") UUID mobileTerminalId) {
        LOG.info("Assign mobile terminal invoked in rest layer.");
        try {
            MobileTerminal mobileTerminal = mobileTerminalService.assignMobileTerminal(assetId, mobileTerminalId, comment, request.getRemoteUser());
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when assigning mobile terminal ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @PUT
    @Path("/{mtId}/unassign/{assetId}")
    @RequiresFeature(UnionVMSFeature.manageMobileTerminals)
    public Response unAssignMobileTerminal(@QueryParam("comment") String comment,
                                           @PathParam("assetId") UUID assetId,
                                           @PathParam("mtId") UUID mtId) {
        LOG.info("Unassign mobile terminal invoked in rest layer.");
        try {
            MobileTerminal mobileTerminal = mobileTerminalService.unAssignMobileTerminal(assetId, mtId, comment, request.getRemoteUser());
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when unassigning mobile terminal ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @PUT
    @Path("/{mtId}/status")
    @RequiresFeature(UnionVMSFeature.manageMobileTerminals)
    public Response setStatusActive(@QueryParam("comment") String comment, @PathParam("mtId") UUID mtId, MobileTerminalStatus status) {
        LOG.info("Set mobile terminal status active invoked in rest layer.");
        try {
            MobileTerminal mobileTerminal = mobileTerminalService.setStatusMobileTerminal(mtId, comment, status, request.getRemoteUser());
            String returnString = objectMapper().writeValueAsString(mobileTerminal);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when activating mobile terminal ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @GET
    @Path("/{mtId}/history/")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getMobileTerminalHistoryListByMobileTerminalId(@PathParam("mtId") UUID id, @DefaultValue("100") @QueryParam("maxNbr") Integer maxNbr) {
        LOG.info("Get mobile terminal history by mobile terminal id invoked in rest layer.");
        try {
            List<MobileTerminal> mobileTerminalRevisions = mobileTerminalService.getMobileTerminalRevisions(id, maxNbr);
            String returnString = objectMapper().writeValueAsString(mobileTerminalRevisions);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting mobile terminal history by terminalId ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @GET
    @Path("/history/getMtHistoryForAsset/{assetId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getMobileTerminalHistoryByAssetId(@PathParam("assetId") UUID assetId,
                                                      @DefaultValue("100") @QueryParam("maxNbr") Integer maxNbr) {

        try {
            List<Map<UUID, List<MobileTerminal>>> mobileTerminalRevisionMap = mobileTerminalService.getMobileTerminalRevisionsByAssetId(assetId, maxNbr);
            String returnString = objectMapper().writeValueAsString(mobileTerminalRevisionMap);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting mobile terminal history by assetId ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }



    @GET
    @Path("history/getAssetHistoryForMT/{mobileTerminalId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getAssetRevisionByMobileTerminalId(@PathParam("mobileTerminalId") UUID mobileTerminalId,
                                                       @DefaultValue("100") @QueryParam("maxNbr") Integer maxNbr) {
        try{
            List<Asset> assetRevisions = mobileTerminalService.getAssetRevisionsByMobileTerminalId(mobileTerminalId);
            String returnString = objectMapper().writeValueAsString(assetRevisions);
            return Response.ok(returnString).header("MDC", MDC.get("requestId")).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting Asset history by mobileTerminalId ] {}", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    //needed since eager fetch is not supported by AuditQuery et al, so workaround is to serialize while we still have a DB session active
    private ObjectMapper objectMapper(){
        ObjectMapperContextResolver omcr = new ObjectMapperContextResolver();
        ObjectMapper objectMapper = omcr.getContext(MobileTerminal.class);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return objectMapper;
    }
}
