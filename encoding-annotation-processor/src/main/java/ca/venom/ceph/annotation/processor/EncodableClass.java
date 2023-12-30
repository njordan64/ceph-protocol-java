package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.encoding.annotations.CephParentType;

import java.util.ArrayList;
import java.util.List;

public class EncodableClass {
    private String packageName;
    private String className;
    private boolean includeSize;
    private Byte marker;
    private Byte version;
    private Byte compatVersion;
    private CephParentType parentType;
    private List<ChildTypeSimple> childTypes;
    private List<EncodableField> fields = new ArrayList<>();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean isIncludeSize() {
        return includeSize;
    }

    public void setIncludeSize(Boolean includeSize) {
        this.includeSize = includeSize;
    }

    public Byte getMarker() {
        return marker;
    }

    public void setMarker(Byte marker) {
        this.marker = marker;
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public Byte getCompatVersion() {
        return compatVersion;
    }

    public void setCompatVersion(Byte compatVersion) {
        this.compatVersion = compatVersion;
    }

    public CephParentType getParentType() {
        return parentType;
    }

    public void setParentType(CephParentType parentType) {
        this.parentType = parentType;
    }

    public List<ChildTypeSimple> getChildTypes() {
        return childTypes;
    }

    public void setChildTypes(List<ChildTypeSimple> childTypes) {
        this.childTypes = childTypes;
    }

    public List<EncodableField> getFields() {
        return fields;
    }

    public void setFields(List<EncodableField> fields) {
        this.fields = fields;
    }
}
