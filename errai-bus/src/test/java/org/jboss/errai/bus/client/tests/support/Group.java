package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ExposeEntity
public class Group {
    private int groupId;
    private String name;
    private List<User> usersInGroup;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsersInGroup() {
        return usersInGroup;
    }

    public void setUsersInGroup(List<User> usersInGroup) {
        this.usersInGroup = usersInGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (groupId != group.groupId) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        if (usersInGroup != null ? !usersInGroup.equals(group.usersInGroup) : group.usersInGroup != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
//        result = 31 * result + (usersInGroup != null ? usersInGroup.hashCode() : 0);
        return result;
    }
}