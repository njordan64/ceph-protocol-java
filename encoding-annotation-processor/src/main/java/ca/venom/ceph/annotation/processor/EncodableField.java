package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephMarker;

import java.util.List;

/**
 * A field of a class that is annotated with @CephType. This field is to be encoded/decoded
 * with the encosing class. Fields are annotated with @CephField. Metadata is stored here
 * to enable generating code for encoding or decoding the field.
 */
public class EncodableField {
    private String name;
    private String type;
    private List<String> interfaces;
    private int order;
    private ByteOrderPreference byteOrderPreference;
    private boolean includeSize;
    private int sizeLength;
    private Integer encodingSize;

    /**
     * Get the name of the field
     * @return name of the field
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of the field
     * @param name name of the field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the type of the field
     * @return type of the field
     */
    public String getType() {
        return type;
    }

    /**
     * Update the type of the field
     * @param type type of the field
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the implemented interfaces of the field's type
     * @return implemented interfaces of the field's type
     */
    public List<String> getInterfaces() {
        return interfaces;
    }

    /**
     * Update the implemented interfaces of the field's type
     * @param interfaces implemented interfaces of the field's type
     */
    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * Get the order of the field
     * @return order of the field
     */
    public int getOrder() {
        return order;
    }

    /**
     * Update the order of the field
     * @param order order of the field
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the byte order preference of the field
     * @return byte order preference of the field
     */
    public ByteOrderPreference getByteOrderPreference() {
        return byteOrderPreference;
    }

    /**
     * Update the byte order of the field
     * @param byteOrderPreference byte order of the field
     */
    public void setByteOrderPreference(ByteOrderPreference byteOrderPreference) {
        this.byteOrderPreference = byteOrderPreference;
    }

    /**
     * Get whether to prepend the encoded size
     * @return whether to prepend the encoded size
     */
    public boolean isIncludeSize() {
        return includeSize;
    }

    /**
     * Update whether to prepend the encoded size
     * @param includeSize whether to prepend the encoded size
     */
    public void setIncludeSize(boolean includeSize) {
        this.includeSize = includeSize;
    }

    /**
     * Get the number of bytes to use for encoded size
     * @return number of bytes to use for encoded size
     */
    public int getSizeLength() {
        return sizeLength;
    }

    /**
     * Update the number of bytes to use for encoded size
     * @param sizeLength number of bytes to use for encoded size
     */
    public void setSizeLength(int sizeLength) {
        this.sizeLength = sizeLength;
    }

    /**
     * Get the encoded size of the field
     * @return encoded size of the field
     */
    public Integer getEncodingSize() {
        return encodingSize;
    }

    /**
     * Update the encoded size of the field
     * @param encodingSize encoded size of the field. Use null to leave calculated.
     */
    public void setEncodingSize(Integer encodingSize) {
        this.encodingSize = encodingSize;
    }
}
