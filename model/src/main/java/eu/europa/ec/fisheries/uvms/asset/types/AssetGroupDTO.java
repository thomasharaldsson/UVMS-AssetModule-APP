//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.08 at 10:11:39 AM CET 
//


package eu.europa.ec.fisheries.uvms.asset.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java class for AssetGroupWSDL complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssetGroupWSDL"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dynamic" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="global" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="searchFields" type="{types.asset.wsdl.fisheries.ec.europa.eu}AssetGroupSearchField" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssetGroupDTO", propOrder = {
    "guid",
    "name",
    "user",
    "dynamic",
    "global",
    "searchFields"
})
public class AssetGroupDTO implements Equals, HashCode
{

    @XmlElement(required = true)
    protected String guid;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String user;
    protected boolean dynamic;
    protected boolean global;
    protected List<AssetGroupSearchField> searchFields;

    /**
     * Default no-arg constructor
     * 
     */
    public AssetGroupDTO() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public AssetGroupDTO(final String guid, final String name, final String user, final boolean dynamic, final boolean global, final List<AssetGroupSearchField> searchFields) {
        this.guid = guid;
        this.name = name;
        this.user = user;
        this.dynamic = dynamic;
        this.global = global;
        this.searchFields = searchFields;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the dynamic property.
     * 
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Sets the value of the dynamic property.
     * 
     */
    public void setDynamic(boolean value) {
        this.dynamic = value;
    }

    /**
     * Gets the value of the global property.
     * 
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Sets the value of the global property.
     * 
     */
    public void setGlobal(boolean value) {
        this.global = value;
    }

    /**
     * Gets the value of the searchFields property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the searchFields property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSearchFields().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssetGroupSearchField }
     * 
     * 
     */
    public List<AssetGroupSearchField> getSearchFields() {
        if (searchFields == null) {
            searchFields = new ArrayList<AssetGroupSearchField>();
        }
        return this.searchFields;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof AssetGroupDTO)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final AssetGroupDTO that = ((AssetGroupDTO) object);
        {
            String lhsGuid;
            lhsGuid = this.getGuid();
            String rhsGuid;
            rhsGuid = that.getGuid();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "guid", lhsGuid), LocatorUtils.property(thatLocator, "guid", rhsGuid), lhsGuid, rhsGuid)) {
                return false;
            }
        }
        {
            String lhsName;
            lhsName = this.getName();
            String rhsName;
            rhsName = that.getName();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "name", lhsName), LocatorUtils.property(thatLocator, "name", rhsName), lhsName, rhsName)) {
                return false;
            }
        }
        {
            String lhsUser;
            lhsUser = this.getUser();
            String rhsUser;
            rhsUser = that.getUser();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "user", lhsUser), LocatorUtils.property(thatLocator, "user", rhsUser), lhsUser, rhsUser)) {
                return false;
            }
        }
        {
            boolean lhsDynamic;
            lhsDynamic = this.isDynamic();
            boolean rhsDynamic;
            rhsDynamic = that.isDynamic();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "dynamic", lhsDynamic), LocatorUtils.property(thatLocator, "dynamic", rhsDynamic), lhsDynamic, rhsDynamic)) {
                return false;
            }
        }
        {
            boolean lhsGlobal;
            lhsGlobal = this.isGlobal();
            boolean rhsGlobal;
            rhsGlobal = that.isGlobal();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "global", lhsGlobal), LocatorUtils.property(thatLocator, "global", rhsGlobal), lhsGlobal, rhsGlobal)) {
                return false;
            }
        }
        {
            List<AssetGroupSearchField> lhsSearchFields;
            lhsSearchFields = (((this.searchFields!= null)&&(!this.searchFields.isEmpty()))?this.getSearchFields():null);
            List<AssetGroupSearchField> rhsSearchFields;
            rhsSearchFields = (((that.searchFields!= null)&&(!that.searchFields.isEmpty()))?that.getSearchFields():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "searchFields", lhsSearchFields), LocatorUtils.property(thatLocator, "searchFields", rhsSearchFields), lhsSearchFields, rhsSearchFields)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = 1;
        {
            String theGuid;
            theGuid = this.getGuid();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "guid", theGuid), currentHashCode, theGuid);
        }
        {
            String theName;
            theName = this.getName();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "name", theName), currentHashCode, theName);
        }
        {
            String theUser;
            theUser = this.getUser();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "user", theUser), currentHashCode, theUser);
        }
        {
            boolean theDynamic;
            theDynamic = this.isDynamic();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "dynamic", theDynamic), currentHashCode, theDynamic);
        }
        {
            boolean theGlobal;
            theGlobal = this.isGlobal();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "global", theGlobal), currentHashCode, theGlobal);
        }
        {
            List<AssetGroupSearchField> theSearchFields;
            theSearchFields = (((this.searchFields!= null)&&(!this.searchFields.isEmpty()))?this.getSearchFields():null);
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "searchFields", theSearchFields), currentHashCode, theSearchFields);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}