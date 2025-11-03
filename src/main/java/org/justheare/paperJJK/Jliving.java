package org.justheare.paperJJK;

import org.bukkit.entity.Entity;

public class Jliving extends Jobject{
    public Jliving(Entity entity) {
        super(entity);
        max_curseenergy=200;
        curseenergy=1;
        max_cursecurrent=1;
        cursecurrent=0;
    }

}
