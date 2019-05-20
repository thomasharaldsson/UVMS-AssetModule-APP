package eu.europa.ec.fisheries.uvms.rest.asset.service;

import eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetGroup;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetGroupField;
import eu.europa.ec.fisheries.uvms.rest.asset.AbstractAssetRestTest;
import eu.europa.ec.fisheries.uvms.rest.asset.AssetHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@RunAsClient
public class AssetGroupResourceTest extends AbstractAssetRestTest {

    @Test
    @OperateOnDeployment("normal")
    public void createAssetGroupCheckResponseCodeTest() {
        
        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        
        Response response = getWebTarget()
                .path("/group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup));

        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void createAssetGroupTest() {
        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("/group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        assertNotNull(assetGroup.getId());
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupListByUserNoUserParamTest() {
        Response response = getWebTarget()
                .path("group")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // You really could argue that this should be a bad request but the server was returning 400 for everything,
        // if there is only one thing returned for every error it is better if it is a 500
        assertThat(response.getStatus(), is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupListByUserTest() {

        Response responseBefore = getWebTarget()
                .path("group")
                .path("list")
                .queryParam("user", "MOCK_USER") // From mock filter
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        List<AssetGroup> groupsBefore = responseBefore.readEntity(new GenericType<List<AssetGroup>>() {});

        getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(AssetHelper.createBasicAssetGroup()), AssetGroup.class);
        
        Response response = getWebTarget()
                .path("group")
                .path("list")
                .queryParam("user", "MOCK_USER") // From mock filter
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        List<AssetGroup> fetchedAssetGroups = response.readEntity(new GenericType<List<AssetGroup>>() {});

        assertNotNull(fetchedAssetGroups);
        assertThat(fetchedAssetGroups.size(), is(groupsBefore.size() + 1));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupListByUserTwoGroupsTest() {

        Response responseBefore = getWebTarget()
                .path("group")
                .path("list")
                .queryParam("user", "MOCK_USER") // From mock filter
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        List<AssetGroup> groupsBefore = responseBefore.readEntity(new GenericType<List<AssetGroup>>() {});

        getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(AssetHelper.createBasicAssetGroup()), AssetGroup.class);

        getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(AssetHelper.createBasicAssetGroup()), AssetGroup.class);
        
        Response response = getWebTarget()
                .path("group")
                .path("list")
                .queryParam("user", "MOCK_USER")
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        List<AssetGroup> fetchedAssetGroups = response.readEntity(new GenericType<List<AssetGroup>>() {});

        assertNotNull(fetchedAssetGroups);
        assertThat(fetchedAssetGroups.size(), is(groupsBefore.size() + 2));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupByIdTest() {

        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        AssetGroup createdAssetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);
        
        AssetGroup fetchedAssetGroup = getWebTarget()
                .path("group")
                .path(createdAssetGroup.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(AssetGroup.class);


        assertNotNull(fetchedAssetGroup);
        assertThat(fetchedAssetGroup.getId(), is(createdAssetGroup.getId()));
        assertThat(fetchedAssetGroup.getName(), is(createdAssetGroup.getName()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void createAssetGroupFieldTest() {
        Asset asset = AssetHelper.createBasicAsset();
        asset = getWebTarget()
                .path("asset")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(asset), Asset.class);

        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(asset.getId().toString());

        field = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field))
                .readEntity(AssetGroupField.class);

        assertNotNull(field.getId());;
    }

    @Test
    @OperateOnDeployment("normal")
    public void retrieveFieldsForGroupTest() {
        Asset asset = AssetHelper.createBasicAsset();
        asset = getWebTarget()
                .path("asset")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(asset), Asset.class);

        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(asset.getId().toString());

        field = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field))
                .readEntity(AssetGroupField.class);

        List<AssetGroupField> fieldList = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("fieldsForGroup")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<AssetGroupField>>() {
                });

        assertEquals(field.getId(), fieldList.get(0).getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void deleteAssetGroupFieldTest() {

        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        UUID randomUUID = UUID.randomUUID();

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(randomUUID.toString());

        field = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field))
                .readEntity(AssetGroupField.class);

        assertNotNull(field.getId());

        field = getWebTarget()
                .path("group")
                .path("field")
                .path(field.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .delete()
                .readEntity(AssetGroupField.class);

        assertNotNull(field);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupFieldTest() {
        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        UUID randomUUID = UUID.randomUUID();

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(randomUUID.toString());

        field = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field))
                .readEntity(AssetGroupField.class);

        AssetGroupField fetched = getWebTarget()
                .path("group")
                .path("field")
                .path(field.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(AssetGroupField.class);

        assertNotNull(fetched.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateAssetGroupFieldTest() {
        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        assetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        UUID randomUUID = UUID.randomUUID();

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(randomUUID.toString());

        field = getWebTarget()
                .path("group")
                .path(assetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field))
                .readEntity(AssetGroupField.class);

        assertEquals(randomUUID.toString(), field.getValue());

        UUID newRandomUUID = UUID.randomUUID();
        field.setValue(newRandomUUID.toString());

        AssetGroupField fetchedField = getWebTarget()
                .path("group")
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(field))
                .readEntity(AssetGroupField.class);

        assertEquals(newRandomUUID.toString(), fetchedField.getValue());
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetGroupListByAssetId() {
        Asset asset = AssetHelper.createBasicAsset();
        Asset createdAsset = getWebTarget()
                .path("asset")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(asset), Asset.class);
        
        AssetGroup assetGroup = AssetHelper.createBasicAssetGroup();
        AssetGroup createdAssetGroup = getWebTarget()
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(assetGroup), AssetGroup.class);

        AssetGroupField field = new AssetGroupField();
        field.setKey("GUID");
        field.setValue(createdAsset.getId().toString());

        getWebTarget()
                .path("group")
                .path(createdAssetGroup.getId().toString())
                .path("field")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(field));
        
        Response response = getWebTarget()
                .path("group")
                .path("asset")
                .path(createdAsset.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        List<AssetGroup> fetchedAssetGroups = response.readEntity(new GenericType<List<AssetGroup>>() {});

        assertNotNull(fetchedAssetGroups);
        assertThat(fetchedAssetGroups.size(), is(1));
        assertThat(fetchedAssetGroups.get(0).getId(), is(createdAssetGroup.getId()));
        assertThat(fetchedAssetGroups.get(0).getName(), is(createdAssetGroup.getName()));
    }
}
