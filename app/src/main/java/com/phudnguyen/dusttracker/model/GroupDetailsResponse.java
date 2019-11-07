package com.phudnguyen.dusttracker.model;

import java.util.List;

public class GroupDetailsResponse extends BaseResponse {
    private Group group;
    private List<LocationInfo> locations;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<LocationInfo> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationInfo> locations) {
        this.locations = locations;
    }
}
