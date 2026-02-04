package org.justheare.paperJJK;

import org.bukkit.entity.Entity;

public class Jliving extends Jobject{
    public Jliving(Entity entity) {
        super(entity);
        max_curseenergy=5;
        curseenergy=1;
        max_cursecurrent=0;
        cursecurrent=0;
    }

}
