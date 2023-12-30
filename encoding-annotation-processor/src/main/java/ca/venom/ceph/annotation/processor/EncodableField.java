package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephMarker;

import java.util.List;

public class EncodableField {
    private String name;
    private String type;
    private List<String> interfaces;
    private int order;
    private ByteOrderPreference byteOrderPreference;
    private boolean includeSize;
    private int sizeLength;
    private CephMarker marker;
    private Integer encodingSize;
    private Integer typeSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ByteOrderPreference getByteOrderPreference() {
        return byteOrderPreference;
    }

    public void setByteOrderPreference(ByteOrderPreference byteOrderPreference) {
        this.byteOrderPreference = byteOrderPreference;
    }

    public boolean isIncludeSize() {
        return includeSize;
    }

    public void setIncludeSize(boolean includeSize) {
        this.includeSize = includeSize;
    }

    public int getSizeLength() {
        return sizeLength;
    }

    public void setSizeLength(int sizeLength) {
        this.sizeLength = sizeLength;
    }

    public CephMarker getMarker() {
        return marker;
    }

    public void setMarker(CephMarker marker) {
        this.marker = marker;
    }

    public Integer getEncodingSize() {
        return encodingSize;
    }

    public void setEncodingSize(Integer encodingSize) {
        this.encodingSize = encodingSize;
    }

    public Integer getTypeSize() {
        return typeSize;
    }

    public void setTypeSize(Integer typeSize) {
        this.typeSize = typeSize;
    }
}
