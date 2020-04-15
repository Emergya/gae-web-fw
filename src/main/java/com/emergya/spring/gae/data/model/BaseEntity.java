package com.emergya.spring.gae.data.model;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Id;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author lroman
 */
public abstract class BaseEntity implements Serializable {

    @Id
    private Long id;

    /**
     * @return the id
     */
    public final Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(Long id) {
        this.id = id;
    }

    /**
     * Converts the entity's data to a SearchIndex document ready to be indexed.
     *
     * @return the Document instance.
     */
    public final Document toDocument() {
        Document.Builder documentBuilder
                = Document.newBuilder().setId(getId().toString());

        try {
            PropertyDescriptor[] objectProperties = PropertyUtils.getPropertyDescriptors(this);

            for (PropertyDescriptor property : objectProperties) {
                if (property.getWriteMethod() != null) {
                    Object value = PropertyUtils.getProperty(this, property.getName());
                    createField(property.getName(), property.getReadMethod().getReturnType(), value, documentBuilder);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Logger.getLogger(BaseEntity.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return documentBuilder.build();
    }

    private void createField(String fieldName, Class type, Object value, Document.Builder docBuilder) {

        Class resultClass = type;
        while (resultClass.getSuperclass() != null
                && resultClass.getSuperclass() != Object.class
                && resultClass.getSuperclass() != Number.class) {
            resultClass = resultClass.getSuperclass();
        }

        if (Collection.class.isAssignableFrom(resultClass)) {
            resultClass = Collection.class;
        }

        if (value == null && resultClass != Ref.class && resultClass != BaseEntity.class) {
            return;
        }

        Field.Builder fieldBuilder = Field.newBuilder();
        fieldBuilder.setName(fieldName);

        String resultType = resultClass.getSimpleName();
        switch (resultType.toLowerCase(Locale.UK)) {
            case "string":
                fieldBuilder.setText((String) value).setLocale(Locale.UK);
                break;
            case "enum":
                fieldBuilder.setAtom(((Enum) value).name());
                break;
            case "long":
                fieldBuilder.setAtom(((Long) value).toString());
                break;
            case "double":
                fieldBuilder.setNumber((Double) value);
                break;
            case "float":
                fieldBuilder.setNumber((Float) value);
                break;
            case "int":
            case "integer":
                fieldBuilder.setNumber((Integer) value);
                break;
            case "date":
                fieldBuilder.setDate((Date) value);
                break;
            case "boolean":
                fieldBuilder.setAtom(((boolean) value) ? "true" : "false");
                break;
            case "collection":
            case "abstractcollection":
                Collection c = (Collection) value;
                for (Object o : c) {
                    createField(fieldName, o.getClass(), o, docBuilder);
                }
                return;
            case "ref":
                if (value == null) {
                    fieldBuilder.setAtom("undefined");
                } else {
                    Ref r = (Ref) value;
                    Long refId = r.getKey().getId();
                    fieldBuilder.setAtom(refId.toString());
                }

                break;
            case "baseentity":
                if (value == null) {
                    fieldBuilder.setAtom("undefined");
                } else {
                    BaseEntity entity = (BaseEntity) value;
                    fieldBuilder.setAtom(entity.getId().toString());
                }

                break;
            case "geopt":
                break;
            default:
                throw new UnsupportedOperationException(String.format(
                        "Type %s is not supported for conversion to document field (%s).", resultType, fieldName));
        }
        docBuilder.addField(fieldBuilder.build());

    }
}
