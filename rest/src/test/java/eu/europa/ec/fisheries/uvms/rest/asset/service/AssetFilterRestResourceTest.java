package eu.europa.ec.fisheries.uvms.rest.asset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilter;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterQuery;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterValue;
import eu.europa.ec.fisheries.uvms.rest.asset.AbstractAssetRestTest;
import eu.europa.ec.fisheries.uvms.rest.asset.AssetHelper;

@RunWith(Arquillian.class)
@RunAsClient
public class AssetFilterRestResourceTest extends AbstractAssetRestTest{
	
	private String testName;
	private AssetFilter assetFilter;
	private AssetFilterQuery assetFilterQuery;
	private Jsonb jsonb;
	
   @Before
    public void setup() {
	   testName = "Test name";
	   JsonbConfig config = new JsonbConfig().withAdapters(new AssetFilterRestResponseAdapter(), new AssetFilterListRestResourceAdapter());
       jsonb = JsonbBuilder.create(config);
	   assetFilter = AssetHelper.createBasicAssetFilter(testName);
	   assetFilter = createAssetFilter(assetFilter);
	   assetFilterQuery = AssetHelper.createBasicAssetFilterQuery(assetFilter);
	   AssetHelper.createBasicAssetFilterValue(assetFilterQuery);
	   assetFilterQuery = createAssetFilterQuery(assetFilter);
	   createAssetFilterValue(assetFilterQuery);
    }
 
    @After
    public void tearDown() {
    	deleteAssetFilter(assetFilter);
    }
	
	@Test
    @OperateOnDeployment("normal")
    public void createAssetFilterFromJsonTest() {
		
		String afjson = "{\"name\":\"båtar\",\"filter\": [{\"values\":[{\"value\":23, \"operator\":\"operator 2 test\"}],\"type\": \"dsad\", \"inverse\": false,\"isNumber\": true}] }";
		
        String assetFilterCreateResp = getWebTargetExternal()
            .path("filter")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .post(Entity.json(afjson), String.class);
        
		AssetFilter assetFilter2 = jsonb.fromJson(assetFilterCreateResp, AssetFilter.class);
        
		assertNotNull(assetFilter2.getId().toString());
		
        Response deleteresp = getWebTargetExternal()
	        .path("filter")
	        .path(assetFilter2.getId().toString())
	        .request(MediaType.APPLICATION_JSON)
	        .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
	        .delete();
        
        assertTrue(deleteresp.getStatus() == Status.OK.getStatusCode() );
    }
	
	@Test
    @OperateOnDeployment("normal")
    public void createAssetFilterTest() {
		
		AssetFilter testAssetFilter = new AssetFilter();
		testAssetFilter = createAssetFilter(testAssetFilter);
        assertNotNull(testAssetFilter.getId());
        
        Response deleteresp = getWebTargetExternal()
	        .path("filter")
	        .path(testAssetFilter.getId().toString())
	        .request(MediaType.APPLICATION_JSON)
	        .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
	        .delete();
        assertTrue(deleteresp.getStatus() == Status.OK.getStatusCode() );
    }
	
	@Test
    @OperateOnDeployment("normal")
    public void getAssetFilterTest() throws InterruptedException {
	   
    	String fetchedAssetFilter = getWebTargetExternal()
            .path("filter")
            .path(assetFilter.getId().toString())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .get(String.class);

    	AssetFilter fetchedAssetFilterJsonAdapter = jsonb.fromJson(fetchedAssetFilter, AssetFilter.class);
        assertEquals(fetchedAssetFilterJsonAdapter.getName(), assetFilter.getName());
        assertNotNull(fetchedAssetFilter);
        assertEquals(fetchedAssetFilterJsonAdapter.getId(), assetFilter.getId());
        assertEquals(fetchedAssetFilterJsonAdapter.getName(), assetFilter.getName());
    }  
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetFilterListByUserNoUserParamTest() {
    	
    	Response response = getWebTargetExternal()
            .path("filter")
            .path("list")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .get(Response.class);
    	
    	assertTrue(response.getStatus() == Status.OK.getStatusCode() );
    }
    
    
    @Test
    @OperateOnDeployment("normal")
    public void getAssetFilterListByUserTest() {
        
        Response response = getWebTargetExternal()
            .path("filter")
            .path("listAssetFiltersByUser")
            .queryParam("user", assetFilter.getOwner())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .get(Response.class);
        
        assertNotNull(response);
        assertTrue(response.getStatus() == Status.OK.getStatusCode());
        assertTrue(response.getEntity().toString().length() > 1);
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void createAssetFilterQueryTest() {
       
        AssetFilterQuery assetQuery = new AssetFilterQuery();
        assetQuery.setType("GUID");
        assetQuery.setIsNumber(false);
        assetQuery.setAssetFilter(assetFilter);
        
        assetQuery = getWebTargetExternal()
            .path("filter")
            .path(assetFilter.getId().toString())
            .path("query")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .post(Entity.json(assetQuery), AssetFilterQuery.class);
        
        assertNotNull(assetQuery.getId());
        
        getWebTargetExternal()
        .path("filter")
        .path(assetQuery.getId().toString())
        .path("query")
        .request(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
        .delete();
    }
	
	@Test
    @OperateOnDeployment("normal")
    public void getAssetFilterByIdTest() {
    	
		String fetchedAssetFilterJsonString = getWebTargetExternal()
            .path("filter")
            .path(assetFilter.getId().toString())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .get(String.class);
		
		AssetFilter fetchedAssetFilter = jsonb.fromJson(fetchedAssetFilterJsonString, AssetFilter.class);
		
		assertEquals(assetFilter.getName(),  fetchedAssetFilter.getName());
		assertEquals(assetFilter.getId(),  fetchedAssetFilter.getId());
	 }
	
	@Test
    @OperateOnDeployment("normal")
    public void updateAssetFilterFromJson() {
		
		String afId = assetFilter.getId().toString();
		String owner = assetFilter.getOwner();
		String afjson = "{\"id\":\""+afId+"\",\"name\":\"Nya Båtar och Update Test\", \"filter\": [{\"values\":[{\"value\":23, \"operator\":\"this is a operator\"}],\"type\": \"dsad\", \"inverse\": false,\"isNumber\": true}] }";
		
		getWebTargetExternal()
            .path("filter")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .put(Entity.json(afjson), String.class);
		 
		String assetFilterResp = getWebTargetExternal()
			.path("filter")
			.path(afId)
    		.request(MediaType.APPLICATION_JSON)
    		.header(HttpHeaders.AUTHORIZATION, getTokenExternal())
    		.get(String.class);
		
		 assetFilter = jsonb.fromJson(assetFilterResp, AssetFilter.class);
		 assertNotNull(assetFilter.getId());
		 assertEquals(assetFilter.getOwner(), owner);
		 assertEquals("Nya Båtar och Update Test", assetFilter.getName());
		 assertTrue(assetFilterResp.contains(afId));
	 }
	
	
	 private AssetFilter createAssetFilter(AssetFilter assetFilterToCreate) {
		 String assetFilterString = "{\"name\":\"båtar\",\"filter\": [{\"values\":[{\"value\":23, \"operator\":\"operator 2 test\"}],\"type\": \"dsad\", \"inverse\": false,\"isNumber\": true}] }";
		 assetFilterToCreate = jsonb.fromJson(assetFilterString, AssetFilter.class);
		String assetFilterJson =  getWebTargetExternal()
		            .path("filter")
		            .request(MediaType.APPLICATION_JSON)
		            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
		            .post(Entity.json(assetFilterString), String.class);
		 assetFilterToCreate = jsonb.fromJson(assetFilterJson, AssetFilter.class);
		 return assetFilterToCreate;
	}
	 
	 private AssetFilterQuery createAssetFilterQuery(AssetFilter assetFilterforQuery) {
		 AssetFilterQuery assetFilterQuery = AssetHelper.createBasicAssetFilterQuery(assetFilterforQuery);
			return getWebTargetExternal()
	                .path("filter")
	                .path(assetFilterforQuery.getId().toString())
	                .path("query")
	                .request(MediaType.APPLICATION_JSON)
	                .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
	                .post(Entity.json(assetFilterQuery), AssetFilterQuery.class);
	 }
	 
	 private AssetFilterValue createAssetFilterValue(AssetFilterQuery assetFilterQueryForValue) {
		 AssetFilterValue assetFilterValue = AssetHelper.createBasicAssetFilterValue(assetFilterQueryForValue);
			return getWebTargetExternal()
	                .path("filter")
	                .path(assetFilterQueryForValue.getId().toString())
	                .path("value")
	                .request(MediaType.APPLICATION_JSON)
	                .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
	                .post(Entity.json(assetFilterValue), AssetFilterValue.class);
	 }
	 private void deleteAssetFilter(AssetFilter assetFilter) {
		getWebTargetExternal()
            .path("filter")
            .path(assetFilter.getId().toString())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getTokenExternal())
            .delete();
	}
	 
	
}
