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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.jms.Message;

import eu.europa.ec.fisheries.uvms.asset.bean.AssetServiceBean;
import eu.europa.ec.fisheries.uvms.asset.domain.constant.AssetIdentifier;
import eu.europa.ec.fisheries.uvms.asset.message.event.AssetModelMapper;
import eu.europa.ec.fisheries.uvms.tests.asset.service.arquillian.arquillian.AssetTestsHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
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
import eu.europa.ec.fisheries.uvms.tests.BuildAssetServiceDeployment;

@RunWith(Arquillian.class)
public class AssetEventQueueTest extends BuildAssetServiceDeployment {

    private JMSHelper jmsHelper = new JMSHelper();

    @Inject
    AssetModelMapper assetModelMapper;

    @Inject
    AssetServiceBean assetServiceBean;

    @Test
    @OperateOnDeployment("normal")
    public void pingTest() throws Exception {
        PingRequest request = new PingRequest();
        request.setMethod(AssetModuleMethod.PING);
        String requestString = JAXBMarshaller.marshallJaxBObjectToString(request);
        String correlationId = jmsHelper.sendAssetMessage(requestString);
        Message response = jmsHelper.listenForResponse(correlationId);
        assertThat(response, is(notNullValue()));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetByCFRTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        // TODO Find better solution, this is needed due to async jms call
        Thread.sleep(2000);
        Asset assetById = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(asset.getName()));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetByIRCSTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);
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
    @OperateOnDeployment("normal")
    public void getAssetByMMSITest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);
        Asset assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(asset.getName()));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertAssetTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);

        String newName = "Name upserted";
        asset.setName(newName);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);
        
        Asset assetById = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        
        assertThat(assetById, is(notNullValue()));
        assertThat(assetById.getCfr(), is(asset.getCfr()));
        assertThat(assetById.getName(), is(newName));
        assertThat(assetById.getExternalMarking(), is(asset.getExternalMarking()));
        assertThat(assetById.getIrcs(), is(asset.getIrcs()));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void assetSourceTest() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setSource(CarrierSource.INTERNAL);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);
        
        Asset fetchedAsset = jmsHelper.getAssetById(asset.getCfr(), AssetIdType.CFR);
        assertThat(fetchedAsset.getSource(), is(asset.getSource()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest1() throws Exception {
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setName(null);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);

        Asset assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertNull(assetById.getName());
        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(asset.getMmsiNo());
        newAsset.setName("namebyassetinfo");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);
        assetById = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(assetById.getName());
        assertEquals("namebyassetinfo", assetById.getName());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest2() throws Exception {

        Asset assetWithsMMSI = AssetTestHelper.createBasicAsset();
        assetWithsMMSI.setIrcs(null);
        assetWithsMMSI.setName("ShouldNotBeThis");
        jmsHelper.upsertAsset(assetWithsMMSI);
        Thread.sleep(2000);


        Asset assetWithsIRCS = AssetTestHelper.createBasicAsset();
        assetWithsIRCS.setMmsiNo(null);
        assetWithsIRCS.setName("namnetestfall2");
        assetWithsIRCS.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsIRCS);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(assetWithsMMSI.getMmsiNo());
        newAsset.setIrcs(assetWithsIRCS.getIrcs());
        newAsset.setName("ShouldNotBeThis");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsMMSI.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), fetchedAsset.getName(), assetWithsIRCS.getName());
        assertNotNull(fetchedAsset.getMmsiNo());
        assertNotNull(fetchedAsset.getIrcs());
    }

    @Test
    @OperateOnDeployment("normal")
    public void NationalAssetWithCorrectIrcsAndMmsiAndInternalAssetWithCorrectButFaultyFormatedIrcs() throws Exception {

        String correctIrcs = "SFC-" + AssetTestsHelper.getRandomIntegers(4);
        String correctMmsi = AssetTestsHelper.getRandomIntegers(9);

        Asset internalAsset = AssetTestHelper.createBasicAsset();
        internalAsset.setIrcs(correctIrcs.replace("-", ""));
        internalAsset.setMmsiNo(null);
        internalAsset.setName("ShouldNotBeThis");
        jmsHelper.upsertAsset(internalAsset);
        Thread.sleep(2000);


        Asset nationalAsset = AssetTestHelper.createBasicAsset();
        nationalAsset.setMmsiNo(correctMmsi);
        nationalAsset.setIrcs(correctIrcs);
        nationalAsset.setName("namnetestfall2");
        nationalAsset.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(nationalAsset);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(correctMmsi);
        newAsset.setIrcs(internalAsset.getIrcs());
        newAsset.setName("ShouldNotBeThis");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(nationalAsset.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), fetchedAsset.getName(), nationalAsset.getName());
        assertNotNull(fetchedAsset.getMmsiNo());
        assertEquals(fetchedAsset.getMmsiNo(), correctMmsi);
        assertNotNull(fetchedAsset.getIrcs());
        assertEquals(fetchedAsset.getIrcs(), correctIrcs);
    }


    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest3() throws Exception {


        Asset assetWithsIRCS = AssetTestHelper.createBasicAsset();
        assetWithsIRCS.setMmsiNo(null);
        assetWithsIRCS.setName(null);
        assetWithsIRCS.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsIRCS);
        Thread.sleep(2000);

        // an ais with a "random" mmsi
        String mmsi = UUID.randomUUID().toString().substring(0,10);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(mmsi);
        newAsset.setName("namnetestfall3");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(mmsi, AssetIdType.MMSI);
        assertNull(fetchedAsset);

    }


    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest4() throws Exception {


        Asset assetWithsIRCS = AssetTestHelper.createBasicAsset();
        assetWithsIRCS.setMmsiNo(null);
        assetWithsIRCS.setName("namnetestfall4");
        assetWithsIRCS.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsIRCS);
        Thread.sleep(2000);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setIrcs(assetWithsIRCS.getIrcs());
        newAsset.setName("ShouldNotBeThis");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsIRCS.getIrcs(), AssetIdType.IRCS);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), assetWithsIRCS.getName());
        assertNotNull(fetchedAsset.getIrcs());
        assertEquals(fetchedAsset.getIrcs(), assetWithsIRCS.getIrcs());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest5() throws Exception {


        Asset assetWithsMMSI = AssetTestHelper.createBasicAsset();
        assetWithsMMSI.setIrcs(null);
        assetWithsMMSI.setName(null);
        assetWithsMMSI.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsMMSI);
        Thread.sleep(2000);

        // an ais with a "random" ircs
        String ircs = UUID.randomUUID().toString().substring(0,9);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setIrcs(ircs);
        newAsset.setName("namnetestfall5");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(ircs, AssetIdType.IRCS);
        assertNull(fetchedAsset);

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest6() throws Exception {


        Asset assetWithsMMSI = AssetTestHelper.createBasicAsset();
        assetWithsMMSI.setIrcs(null);
        assetWithsMMSI.setName(null);
        assetWithsMMSI.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsMMSI);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(assetWithsMMSI.getMmsiNo());
        newAsset.setName("namnetestfall6");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsMMSI.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAsset);
        assertNull(fetchedAsset.getName());
        assertNotNull(fetchedAsset.getMmsiNo());
        assertEquals(fetchedAsset.getMmsiNo(), assetWithsMMSI.getMmsiNo());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest6XEUSource() throws Exception {


        Asset assetWithsMMSI = AssetTestHelper.createBasicAsset();
        assetWithsMMSI.setIrcs(null);
        assetWithsMMSI.setName(null);
        assetWithsMMSI.setSource(CarrierSource.XEU);
        jmsHelper.upsertAsset(assetWithsMMSI);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(assetWithsMMSI.getMmsiNo());
        newAsset.setName("namnetestfall6");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsMMSI.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), newAsset.getName());
        assertNotNull(fetchedAsset.getMmsiNo());
        assertEquals(fetchedAsset.getMmsiNo(), assetWithsMMSI.getMmsiNo());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest7() throws Exception {


        Asset assetWithsIRCS = AssetTestHelper.createBasicAsset();
        assetWithsIRCS.setMmsiNo(null);
        assetWithsIRCS.setName(null);
        assetWithsIRCS.setSource(CarrierSource.NATIONAL);
        jmsHelper.upsertAsset(assetWithsIRCS);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setIrcs(assetWithsIRCS.getIrcs());
        newAsset.setName("namnetestfall7");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsIRCS.getIrcs(), AssetIdType.IRCS);
        assertNotNull(fetchedAsset);
        assertNull(fetchedAsset.getName());
        assertNotNull(fetchedAsset.getIrcs());
        assertEquals(fetchedAsset.getIrcs(), assetWithsIRCS.getIrcs());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationTest7ThirdCountrySource() throws Exception {


        Asset assetWithsIRCS = AssetTestHelper.createBasicAsset();
        assetWithsIRCS.setMmsiNo(null);
        assetWithsIRCS.setName(null);
        assetWithsIRCS.setSource(CarrierSource.THIRD_COUNTRY);
        jmsHelper.upsertAsset(assetWithsIRCS);
        Thread.sleep(2000);


        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setIrcs(assetWithsIRCS.getIrcs());
        newAsset.setName("namnetestfall7");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(assetWithsIRCS.getIrcs(), AssetIdType.IRCS);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), newAsset.getName());
        assertNotNull(fetchedAsset.getIrcs());
        assertEquals(fetchedAsset.getIrcs(), assetWithsIRCS.getIrcs());

    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationIrcsWithSpaceTest() throws Exception {
        String randomSuffix = AssetTestsHelper.getRandomIntegers(6);
        String ircs = "I" + randomSuffix;
        String testIrcs = "I " + randomSuffix;
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setMmsiNo(null);
        asset.setIrcs(ircs);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);

        Asset assetById = jmsHelper.getAssetById(asset.getIrcs(), AssetIdType.IRCS);
        assertNull(assetById.getMmsiNo());
        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(AssetTestsHelper.getRandomIntegers(9));
        newAsset.setIrcs(testIrcs);
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);
        assetById = jmsHelper.getAssetById(asset.getIrcs(), AssetIdType.IRCS);
        assertNotNull(assetById.getMmsiNo());
        assertEquals(assetById.getMmsiNo(), newAsset.getMmsi());
    }

    @Test
    @OperateOnDeployment("normal")
    public void assetInformationMergeIrcsWithSpaceTest() throws Exception {
        String randomSuffix = AssetTestsHelper.getRandomIntegers(6);
        String ircs = "I" + randomSuffix;
        String testIrcs = "I " + randomSuffix;
        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setSource(CarrierSource.NATIONAL);
        asset.setMmsiNo(null);
        asset.setIrcs(ircs);
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);

        Asset asset2 = AssetTestHelper.createBasicAsset();
        String mmsi = AssetTestsHelper.getRandomIntegers(9);
        asset2.setMmsiNo(mmsi);
        asset2.setIrcs(null);
        jmsHelper.upsertAsset(asset2);
        Thread.sleep(2000);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();
        newAsset.setMmsi(mmsi);
        newAsset.setIrcs(testIrcs);
        jmsHelper.assetInfo(Arrays.asList(newAsset));
        Thread.sleep(2000);
        Asset assetById = jmsHelper.getAssetById(asset.getIrcs(), AssetIdType.IRCS);
        assertNotNull(assetById.getMmsiNo());
        assertEquals(assetById.getMmsiNo(), newAsset.getMmsi());
        Asset assetByMmsi = jmsHelper.getAssetById(mmsi, AssetIdType.MMSI);
        assertEquals(assetByMmsi.getAssetId().getGuid(), assetById.getAssetId().getGuid());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateAssetWithEmptyStringIRCS() throws Exception {

        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setIrcs("");
        asset.setName("updateAssetWithEmptyStringIRCS");
        jmsHelper.upsertAsset(asset);
        Thread.sleep(2000);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();

        newAsset.setMmsi(AssetTestHelper.getRandomIntegers(9));
        newAsset.setIrcs(asset.getIrcs());
        newAsset.setName("shouldNotBeThis");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAsset = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAsset);
        assertNotNull(fetchedAsset.getName());
        assertEquals(fetchedAsset.getName(), fetchedAsset.getName(), asset.getName());
        assertNotNull(fetchedAsset.getMmsiNo());
        assertNull(fetchedAsset.getIrcs());
    }


    @Test
    @OperateOnDeployment("normal")
    public void createSeveralAssetsWithEmptyStringIRCSAndUpdateOneOfThemTest() throws Exception {

        Asset asset = AssetTestHelper.createBasicAsset();
        asset.setIrcs("");
        asset.setName("createAssetWithEmptyStringIRCS");
        jmsHelper.upsertAsset(asset);

        Asset asset2 = AssetTestHelper.createBasicAsset();
        asset2.setIrcs("");
        asset2.setName("createAssetWithEmptyStringIRCS2");
        jmsHelper.upsertAsset(asset2);
        Thread.sleep(2000);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset newAsset = new eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset();

        newAsset.setMmsi(asset2.getMmsiNo());
        newAsset.setIrcs(asset2.getIrcs());
        newAsset.setName("createAssetWithEmptyStringIRCS2NewName");
        List<eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset> assetList = new ArrayList<>();
        assetList.add(newAsset);
        jmsHelper.assetInfo(assetList);
        Thread.sleep(2000);

        Asset fetchedAssetNotUpdated = jmsHelper.getAssetById(asset.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAssetNotUpdated);
        assertNotNull(fetchedAssetNotUpdated.getName());
        assertEquals(fetchedAssetNotUpdated.getName(), fetchedAssetNotUpdated.getName(), asset.getName());
        assertNotNull(fetchedAssetNotUpdated.getMmsiNo());
        assertNull(fetchedAssetNotUpdated.getIrcs());

        Asset fetchedAssetUpdated = jmsHelper.getAssetById(asset2.getMmsiNo(), AssetIdType.MMSI);
        assertNotNull(fetchedAssetUpdated);
        assertNotNull(fetchedAssetUpdated.getName());
        assertEquals(fetchedAssetUpdated.getName(), fetchedAssetUpdated.getName(), newAsset.getName());
        assertNotNull(fetchedAssetUpdated.getMmsiNo());
        assertNull(fetchedAssetUpdated.getIrcs());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createAssetWithNationalId() throws Exception {
        Asset assetType = AssetTestHelper.createBasicAsset();
        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset asset = assetModelMapper.toAssetEntity(assetType);
        asset.setName("Create with national id");
        Long nationalId = (long)(Math.random() * 10000000d);
        asset.setNationalId(nationalId);

        jmsHelper.upsertAssetUsingMethod(asset);
        Thread.sleep(1000);

        eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset assetById = assetServiceBean.getAssetById(AssetIdentifier.NATIONAL, nationalId.toString());
        assertEquals(nationalId, assetById.getNationalId());
        assertEquals(asset.getName(), assetById.getName());

    }



}
