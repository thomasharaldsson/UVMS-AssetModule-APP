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
package eu.europa.ec.fisheries.uvms.asset.message;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.asset.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.wsdl.asset.module.AssetModuleMethod;
import eu.europa.ec.fisheries.wsdl.asset.module.PingRequest;
import eu.europa.ec.fisheries.wsdl.asset.types.Asset;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetIdType;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetListCriteriaPair;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetListQuery;
import eu.europa.ec.fisheries.wsdl.asset.types.CarrierSource;
import eu.europa.ec.fisheries.wsdl.asset.types.ConfigSearchField;
import eu.europa.fisheries.uvms.tests.BuildAssetServiceDeployment;

@RunWith(Arquillian.class)
public class AssetEventQueueTest extends BuildAssetServiceDeployment {

    private JMSHelper jmsHelper = new JMSHelper();

    @Test
    @RunAsClient
    public void pingTest() throws Exception {
        PingRequest request = new PingRequest();
        request.setMethod(AssetModuleMethod.PING);
        String requestString = JAXBMarshaller.marshallJaxBObjectToString(request);
        String correlationId = jmsHelper.sendAssetMessage(requestString);
        Message response = jmsHelper.listenForResponse(correlationId);
        assertThat(response, is(notNullValue()));
    }
    
    @Test
    @RunAsClient
    public void getAssetByCFRTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        // TODO Find better solution, this is needed due to async jms call
        Thread.sleep(5000);
        Asset assetById = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(asset.getName()));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }
    
    @Test
    @RunAsClient
    public void getAssetByIRCSTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);
        Asset assetById = jmsHelper.getAssetById(asset.getIrcs(), AssetIdType.IRCS);

        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(asset.getName()));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));

        assertEquals(AssetIdType.GUID, assetById.getAssetId().getType());
        assertEquals(assetById.getAssetId().getGuid(), assetById.getAssetId().getValue()); //since guid and value are supposed t obe the same
    }
    
    @Test
    @RunAsClient
    public void getAssetByMMSITest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);
        Asset assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(asset.getName()));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }
    
    @Test
    @RunAsClient
    public void getAssetListByQueryTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);
        
        AssetListQuery assetListQuery = AssetTestHelper.createBasicAssetQuery();
        AssetListCriteriaPair assetListCriteriaPair = new AssetListCriteriaPair();
        assetListCriteriaPair.setKey(ConfigSearchField.FLAG_STATE);
        assetListCriteriaPair.setValue(asset.getCountryCode());
        assetListQuery.getAssetSearchCriteria().getCriterias().add(assetListCriteriaPair);
        
        List<Asset> assets = jmsHelper.getAssetByAssetListQuery(assetListQuery);
        assertTrue(assets.stream().filter(a -> asset.getCfr() == asset.getCfr()).count() > 0);
    }
    
    @Test
    @RunAsClient
    public void upsertAssetTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);

        String newName = "Name upserted";
        asset.setName(newName);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);
        
        Asset assetById = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(newName));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }
    
    @Test
    @RunAsClient
    public void assetSourceTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setSource(CarrierSource.INTERNAL);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);
        
        Asset fetchedAsset = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        assertThat(fetchedAsset.getSource(), is(asset.getSource()));
    }

    @Test
    @RunAsClient
    public void assetInformationTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setName(null);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(5000);

        Asset assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertTrue(assetById.getName() == null);
        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(asset.getMmsiNo());
        newAsset.setName("namebyassetinfo");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(5000);
        assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertTrue(assetById.getName() != null);
        assertTrue(assetById.getName().equals("namebyassetinfo"));

    }
}
