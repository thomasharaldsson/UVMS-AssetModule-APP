package eu.europa.ec.fisheries.uvms.asset.util;

import eu.europa.ec.fisheries.uvms.asset.remote.dto.search.SearchFields;
import eu.europa.ec.fisheries.uvms.asset.remote.dto.search.SearchLeaf;
import eu.europa.ec.fisheries.uvms.asset.remote.dto.search.SearchBranch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.*;

public class SearchBranchDeserializer implements JsonbDeserializer<SearchBranch> {

    private static final Logger LOG = LoggerFactory.getLogger(SearchBranchDeserializer.class);
    private static final List<String> OPERATOR_WHITE_LIST = new ArrayList<>(Arrays.asList(">=", "<=", "!=", "="));
    private static final Map<String,SearchFields> MAP_OF_SEARCH_FIELDS = SearchFields.getMapOfEnums();
    private static final String JSON_KEY_SEARCH = "searchValue";
    private static final String JSON_KEY_OPERATOR = "operator";
    private static final String JSON_KEY_FIELDS = "fields";

    @Override
    public SearchBranch deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {

        JsonObject object = parser.getObject();
        return recurse(object);
    }

    private SearchBranch recurse(JsonObject object){
        try {
            SearchBranch trunk = new SearchBranch();
            trunk.setLogicalAnd(object.getBoolean("logicalAnd",true));
            JsonArray fields = object.getJsonArray(JSON_KEY_FIELDS);
            for (JsonValue jsonValue : fields) {
                if (jsonValue.asJsonObject().containsKey(JSON_KEY_FIELDS)) {
                    trunk.getFields().add(recurse(jsonValue.asJsonObject()));
                } else {
                    AbstractMap.SimpleEntry<SearchFields, String> searchKeyValuePair = getSearchKeyValuePair(jsonValue);
                    String operator = null;
                    if (jsonValue.asJsonObject().containsKey(JSON_KEY_OPERATOR)) {
                    	String operatorFromJson = jsonValue.asJsonObject().getJsonString(JSON_KEY_OPERATOR).getString();
                        operator = OPERATOR_WHITE_LIST.contains(operatorFromJson) ? operatorFromJson : "=";
                    }
                    trunk.getFields().add(new SearchLeaf(searchKeyValuePair.getKey(), searchKeyValuePair.getValue(), operator));
                }
            }
            return trunk;
        }catch (Exception e){
            LOG.error("Unparsable input string for asset list: {}", object);
            throw new IllegalArgumentException("Unparsable input string for asset list: " + object, e);
        }
    }

    private AbstractMap.SimpleEntry<SearchFields, String> getSearchKeyValuePair(JsonValue jsonValue) {
        String jsonSearchFieldValue = jsonValue.asJsonObject().getJsonString("searchField").getString();
        SearchFields mappedValue = MAP_OF_SEARCH_FIELDS.get(jsonSearchFieldValue.toLowerCase());
        SearchFields key =  mappedValue != null ? mappedValue : SearchFields.valueOf(jsonSearchFieldValue);
        String value;
        if (jsonValue.asJsonObject().get(JSON_KEY_SEARCH).getValueType() == ValueType.STRING) {
            value = jsonValue.asJsonObject().getJsonString(JSON_KEY_SEARCH).getString();
        } else {
            value = jsonValue.asJsonObject().get(JSON_KEY_SEARCH).toString();
        }
        return new AbstractMap.SimpleEntry<>(key, value);
    }

}
