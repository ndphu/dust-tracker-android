package com.phudnguyen.dusttracker.model;

public class GroupDetailsResponse extends BaseResponse {
    private Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
