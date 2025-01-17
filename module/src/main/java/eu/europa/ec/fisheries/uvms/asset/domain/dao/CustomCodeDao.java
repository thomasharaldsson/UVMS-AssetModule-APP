package eu.europa.ec.fisheries.uvms.asset.domain.dao;

import eu.europa.ec.fisheries.uvms.asset.domain.entity.CustomCode;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.CustomCodesPK;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Stateless
public class CustomCodeDao {

    @PersistenceContext
    private EntityManager em;

    private static final String PARAMETER_NAME_CONSTANT = "constant";
    private static final String PARAMETER_NAME_CODE = "code";
    private static final String PARAMETER_NAME_A_DATE = "aDate";

    public CustomCode create(CustomCode daoRecord) {
        em.persist(daoRecord);
        return daoRecord;
    }

    public CustomCode get(CustomCodesPK primaryKey) {
        try {
            return em.find(CustomCode.class, primaryKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public CustomCode update(CustomCodesPK primaryKey, String newDescription) {

        CustomCode customCode = get(primaryKey);
        if (customCode != null && newDescription != null) {
            customCode.setDescription(newDescription);
        }
        return customCode;
    }

    public void delete(CustomCodesPK primaryKey) {

        CustomCode daoRecord = get(primaryKey);
        if (daoRecord != null) {
            em.remove(daoRecord);
        }
    }

    public Boolean exists(CustomCodesPK primaryKey) {

        try {
            CustomCode customCode = get(primaryKey);
            return customCode != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public List<CustomCode> getAllFor(String constant) {
        TypedQuery<CustomCode> query = em.createNamedQuery(CustomCode.CUSTOMCODES_GETALLFOR, CustomCode.class);
        query.setParameter(PARAMETER_NAME_CONSTANT, constant);
        return query.getResultList();
    }

    public void deleteAllFor(String constant) {

        List<CustomCode> rs = getAllFor(constant);
        if (rs != null) {
            for (CustomCode customCode : rs) {
                em.remove(customCode);
            }
        }
    }

    public List<String> getAllConstants() {
        try {
            TypedQuery<String> query = em.createNamedQuery(CustomCode.CUSTOMCODES_GETALLCONSTANTS, String.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public List<CustomCode> getForDate(String constant, String code, Instant aDate) {

        TypedQuery<CustomCode> query = em.createNamedQuery(CustomCode.CUSTOMCODES_GETCUSTOMCODE_FOR_SPECIFIC_DATE, CustomCode.class);
        query.setParameter(PARAMETER_NAME_CONSTANT, constant);
        query.setParameter(PARAMETER_NAME_CODE, code);
        query.setParameter(PARAMETER_NAME_A_DATE, aDate);
        return query.getResultList();
    }

    public Boolean verify(String constant, String code, Instant aDate) {
        TypedQuery<CustomCode> query = em.createNamedQuery(CustomCode.CUSTOMCODES_GETCUSTOMCODE_FOR_SPECIFIC_DATE, CustomCode.class);
        query.setParameter(PARAMETER_NAME_CONSTANT, constant);
        query.setParameter(PARAMETER_NAME_CODE, code);
        query.setParameter(PARAMETER_NAME_A_DATE, aDate);
        List<CustomCode> customCodes = query.getResultList();
        return !customCodes.isEmpty();
    }

    // delets old and adds new
    public CustomCode replace(CustomCode customCode) {
        CustomCodesPK primaryKey = customCode.getPrimaryKey();
        Boolean primaryKeyExists = exists(primaryKey);

        if (primaryKeyExists != null && primaryKeyExists) {
            delete(primaryKey);
        }
        return em.merge(customCode);
    }
}
