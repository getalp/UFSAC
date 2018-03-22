package getalp.wsd.ufsac.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicalEntity
{
    private Map<String, Annotation> annotationsAsMap;

    private List<Annotation> annotationsAsList;

    private ParentLexicalEntity parent;
    
    public LexicalEntity()
    {
        annotationsAsList = new ArrayList<>();
        annotationsAsMap = new HashMap<>();
        parent = null;
    }

    public List<Annotation> getAnnotations()
    {
        return Collections.unmodifiableList(annotationsAsList);
    }

    public String getAnnotationValue(String annotationName)
    {
        if (!annotationsAsMap.containsKey(annotationName)) return "";
        return annotationsAsMap.get(annotationName).getAnnotationValue();
    }

    public List<String> getAnnotationValues(String annotationName, String delimiter)
    {
        if (!annotationsAsMap.containsKey(annotationName)) return Collections.emptyList();
        return annotationsAsMap.get(annotationName).getAnnotationValues(delimiter);
    }

    public void setAnnotation(String annotationName, String annotationValue)
    {
    	if (annotationName == null || annotationName.equals("")) return;
    	if (annotationValue == null) annotationValue = "";
        if (hasAnnotation(annotationName))
        {
            annotationsAsMap.get(annotationName).setAnnotationValue(annotationValue);
        }
        else
        {
            Annotation a = new Annotation(annotationName, annotationValue);
            annotationsAsList.add(a);
            annotationsAsMap.put(annotationName, a);
        }
    }

    public void setAnnotation(String annotationName, List<String> annotationValues, String delimiter)
    {
        if (annotationName == null || annotationName.equals("")) return;
        if (annotationValues == null) annotationValues = Collections.emptyList();
        if (hasAnnotation(annotationName))
        {
            annotationsAsMap.get(annotationName).setAnnotationValues(annotationValues, delimiter);
        }
        else
        {
            Annotation a = new Annotation(annotationName, annotationValues, delimiter);
            annotationsAsList.add(a);
            annotationsAsMap.put(annotationName, a);
        }
    }

    public void removeAnnotation(String annotationName)
    {
        annotationsAsList.removeIf(a -> a.getAnnotationName().equals(annotationName));
        annotationsAsMap.remove(annotationName);
    }

    public void removeAllAnnotations()
    {
        annotationsAsList.clear();
        annotationsAsMap.clear();
    }
    
    public boolean hasAnnotation(String annotationName)
    {
        return !getAnnotationValue(annotationName).isEmpty();
    }
    
    public void transfertAnnotationsToCopy(LexicalEntity copy)
    {
    	for (Annotation a : this.annotationsAsList)
    	{
    		copy.setAnnotation(a.getAnnotationName(), a.getAnnotationValue());
    	}
    }

    protected void setParent(ParentLexicalEntity parent)
    {
        if (this.parent == parent) return;
        if (this.parent != null)
        {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        if (this.parent != null)
        {
            parent.addChild(this);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends ParentLexicalEntity> T getParent()
    {
        return (T) parent;
    }
}
