package eu.europa.ec.fisheries.uvms.asset.message.consumer.event.bean;

import javax.ejb.EJB;
import javax.jms.TextMessage;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.asset.message.AbstractMessageTest;
import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetException;
import eu.europa.ec.fisheries.uvms.asset.service.AssetService;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetId;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetIdType;


@RunWith(Arquillian.class)
public class GetAssetEventBeanIntTest extends AbstractMessageTest {

    @EJB
    private GetAssetEventBean getAssetEventBean;

    @EJB
    private AssetService assetService;

//    @Inject
//    InterceptorForTest interceptorForTest;

    @After
    public void teardown() {
//        interceptorForTest.recycle();
    }


    @Test
    @OperateOnDeployment("normal")
    public void testSetup() {
        Assert.assertNotNull(getAssetEventBean);
    }

    @Test
    @OperateOnDeployment("normal")
    @Ignore
    public void testBadUUIDGetAsset() {
//        TextMessage textMessage = null;
//        AssetId assetId = new AssetId();
//        assetId.setType(AssetIdType.GUID);
//        assetId.setValue("<BAD UUID>");
//
//        getAssetEventBean.getAsset(textMessage, assetId);
//
//        Assert.assertTrue(interceptorForTest.isFailed());
        //Assert.assertTrue(interceptorForTest.getAssetFault().getFault().contains("Exception when getting asset from source : INTERNAL Error message: No asset found for <BAD UUID>"));

    }

    @Test
    @OperateOnDeployment("normal")
    public void testGetAsset() throws AssetException {


        /*
        AssetDTO createdAsset = assetService.createAsset(AssetHelper.helper_createAsset(AssetIdType.GUID), "test");

        TextMessage textMessage = null;
        AssetId assetId = new AssetId();
        assetId.setType(GUID);
        assetId.setValue(createdAsset.getAssetId().getValue());

        getAssetEventBean.getAsset(textMessage, assetId);

        Assert.assertFalse(interceptorForTest.isFailed());
        Assert.assertTrue(interceptorForTest.getSuccessfulTestEvent().getMessage().contains(createdAsset.getAssetId().getValue()));

        */


    }

}

