package com.phudnguyen.dusttracker.model;

public class JoinResponse extends BaseResponse {
    Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
