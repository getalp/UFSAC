package getalp.wsd.ufsac.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParentLexicalEntity extends LexicalEntity
{
    private List<LexicalEntity> children;
    
    protected ParentLexicalEntity()
    {
        children = new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends LexicalEntity> List<T> getChildren()
    {
        return (List<T>) Collections.unmodifiableList(children);
    }
    
    protected void addChild(LexicalEntity child)
    {
        if (children.contains(child)) return;
        children.add(child);
        child.setParent(this);
    }
    
    protected <T extends LexicalEntity> void addChildren(List<T> children)
    {
    	List<T> childrenCopy = new ArrayList<T>(children);
        for (LexicalEntity child : childrenCopy)
        {
            addChild(child);
        }
    }
    
    protected void removeChild(LexicalEntity child)
    {
        if (!children.contains(child)) return;
        children.remove(child);
        child.setParent(null);
    }
    
    protected void removeAllChildren()
    {
        List<LexicalEntity> childrenBefore = new ArrayList<>(this.children);
        for (LexicalEntity child : childrenBefore)
        {
            removeChild(child);
        }
    }
}
