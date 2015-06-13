//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.10-b140310.1920 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.13 at 02:05:50 PM EDT 
//


package gov.nih.nci.hpc.domain.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HpcDatasetRegistrationMetadata complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HpcDatasetRegistrationMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataContainsPII" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dataContainsPHI" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dataEncrypted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dataCompressed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fundingOrganization" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="metadataItems" type="{http://hpc.nci.nih.gov/domain/metadata}HpcMetadataItem" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HpcDatasetRegistrationMetadata", propOrder = {
    "dataContainsPII",
    "dataContainsPHI",
    "dataEncrypted",
    "dataCompressed",
    "description",
    "fundingOrganization",
    "metadataItems"
})
public class HpcDatasetRegistrationMetadata
    implements Serializable
{

    protected boolean dataContainsPII;
    protected boolean dataContainsPHI;
    protected boolean dataEncrypted;
    protected boolean dataCompressed;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    protected String fundingOrganization;
    protected List<HpcMetadataItem> metadataItems;

    /**
     * Gets the value of the dataContainsPII property.
     * 
     */
    public boolean isDataContainsPII() {
        return dataContainsPII;
    }

    /**
     * Sets the value of the dataContainsPII property.
     * 
     */
    public void setDataContainsPII(boolean value) {
        this.dataContainsPII = value;
    }

    /**
     * Gets the value of the dataContainsPHI property.
     * 
     */
    public boolean isDataContainsPHI() {
        return dataContainsPHI;
    }

    /**
     * Sets the value of the dataContainsPHI property.
     * 
     */
    public void setDataContainsPHI(boolean value) {
        this.dataContainsPHI = value;
    }

    /**
     * Gets the value of the dataEncrypted property.
     * 
     */
    public boolean isDataEncrypted() {
        return dataEncrypted;
    }

    /**
     * Sets the value of the dataEncrypted property.
     * 
     */
    public void setDataEncrypted(boolean value) {
        this.dataEncrypted = value;
    }

    /**
     * Gets the value of the dataCompressed property.
     * 
     */
    public boolean isDataCompressed() {
        return dataCompressed;
    }

    /**
     * Sets the value of the dataCompressed property.
     * 
     */
    public void setDataCompressed(boolean value) {
        this.dataCompressed = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the fundingOrganization property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFundingOrganization() {
        return fundingOrganization;
    }

    /**
     * Sets the value of the fundingOrganization property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFundingOrganization(String value) {
        this.fundingOrganization = value;
    }

    /**
     * Gets the value of the metadataItems property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadataItems property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadataItems().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HpcMetadataItem }
     * 
     * 
     */
    public List<HpcMetadataItem> getMetadataItems() {
        if (metadataItems == null) {
            metadataItems = new ArrayList<HpcMetadataItem>();
        }
        return this.metadataItems;
    }

}
