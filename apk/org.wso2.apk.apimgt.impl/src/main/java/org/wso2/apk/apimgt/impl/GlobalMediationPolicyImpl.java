package org.wso2.apk.apimgt.impl;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.Mediation;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.user.exceptions.UserException;
import org.wso2.apk.apimgt.user.mgt.internal.UserManagerHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GlobalMediationPolicyImpl {
    private static final Log log = LogFactory.getLog(GlobalMediationPolicyImpl.class);
    /**
     * Returns list of global mediation policies available
     *
     * @return List of Mediation objects of global mediation policies
     * @throws APIManagementException If failed to get global mediation policies
     */
    protected Registry registry;

    public GlobalMediationPolicyImpl(String organization) throws APIManagementException {
        String internalOrganizationDomain = APIUtil.getInternalOrganizationDomain(organization);
        try {
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(internalOrganizationDomain)) {
                int id = UserManagerHolder.getUserManager().getTenantId(internalOrganizationDomain);
                registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(-1234);
            }
        } catch (UserException e) {
            throw new APIManagementException("Error while retrieving Tenant id for organization" + organization,
                    ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while retrieving Registry for organization" + organization,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public List<Mediation> getAllGlobalMediationPolicies() throws APIManagementException {

        List<Mediation> mediationList = new ArrayList<Mediation>();
        Mediation mediation;
        String resourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        try {
            //Resource : customsequences
            Resource resource = registry.get(resourcePath);
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    //Resource : in / out / fault
                    Resource typeResource = registry.get(type);
                    if (typeResource instanceof Collection) {
                        String[] sequenceArray = ((Collection) typeResource).getChildren();
                        if (sequenceArray.length > 0) {
                            for (String sequence : sequenceArray) {
                                //Resource : actual resource eg : log_in_msg.xml
                                Resource sequenceResource = registry.get(sequence);
                                String resourceId = sequenceResource.getUUID();
                                try {
                                    String contentString = IOUtils.toString
                                            (sequenceResource.getContentStream(), StandardCharsets.UTF_8);
                                    OMElement omElement = AXIOMUtil.stringToOM(contentString);
                                    OMAttribute attribute = omElement.getAttribute(new QName
                                            (PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                                    String mediationPolicyName = attribute.getAttributeValue();
                                    mediation = new Mediation();
                                    mediation.setUuid(resourceId);
                                    mediation.setName(mediationPolicyName);
                                    //Extract sequence type from the registry resource path
                                    String resourceType = type.substring(type.lastIndexOf("/") + 1);
                                    mediation.setType(resourceType);
                                    //Add mediation to the mediation list
                                    mediationList.add(mediation);
                                } catch (XMLStreamException e) {
                                    //If any exception been caught flow may continue with the next mediation policy
                                    log.error("Error occurred while getting omElement out of " +
                                            "mediation content from " + sequence, e);
                                } catch (IOException e) {
                                    log.error("Error occurred while converting resource " +
                                            "contentStream in to string in " + sequence, e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get global mediation policies";
            throw new APIManagementException(msg, e, ExceptionCodes.INTERNAL_ERROR);
        }
        return mediationList;
    }

    protected GlobalMediationPolicyImpl() {
    }
    /**
     * Return mediation policy corresponds to the given identifier
     *
     * @param mediationPolicyId uuid of the registry resource
     * @return Mediation object related to the given identifier or null
     * @throws APIManagementException If failed to get specified mediation policy
     */
    public Mediation getGlobalMediationPolicy(String mediationPolicyId) throws APIManagementException {

        Mediation mediation = null;
        //Get registry resource correspond to identifier
        Resource mediationResource = this.getCustomMediationResourceFromUuid(mediationPolicyId);
        if (mediationResource != null) {
            //Get mediation config details
            try {
                //extracting content stream of mediation policy in to  string
                String contentString = IOUtils.toString(mediationResource.getContentStream(), StandardCharsets.UTF_8);
                //Get policy name from the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName
                        (PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                String mediationPolicyName = attribute.getAttributeValue();
                mediation = new Mediation();
                mediation.setUuid(mediationResource.getUUID());
                mediation.setName(mediationPolicyName);
                String resourcePath = mediationResource.getPath();
                //Extracting mediation type from the registry resource path
                String[] path = resourcePath.split(RegistryConstants.PATH_SEPARATOR);
                String resourceType = path[(path.length - 2)];
                mediation.setType(resourceType);
                mediation.setConfig(contentString);

            } catch (RegistryException e) {
                log.error("Error occurred while getting content stream of the ,mediation policy ", e);
            } catch (IOException e) {
                log.error("Error occurred while converting content stream of mediation policy " +
                        "into string ", e);
            } catch (XMLStreamException e) {
                log.error("Error occurred while getting omElement out of mediation content ", e);
            }
        }
        return mediation;
    }
    /**
     * Returns the mediation policy registry resource correspond to the given identifier
     *
     * @param mediationPolicyId uuid of the mediation resource
     * @return Registry resource of given identifier or null
     * @throws APIManagementException If failed to get the registry resource of given uuid
     */
    public Resource getCustomMediationResourceFromUuid(String mediationPolicyId)
            throws APIManagementException {

        String resourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        try {
            Resource resource = registry.get(resourcePath);
            //resource : customsequences
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    Resource typeResource = registry.get(type);
                    //typeResource: in/ out/ fault
                    if (typeResource instanceof Collection) {
                        String[] policyArray = ((Collection) typeResource).getChildren();
                        if (policyArray.length > 0) {
                            for (String policy : policyArray) {
                                Resource mediationResource = registry.get(policy);
                                //mediationResource: eg .log_in_msg.xml
                                String resourceId = mediationResource.getUUID();
                                if (resourceId.equals(mediationPolicyId)) {
                                    //If registry resource id matches given identifier returns that
                                    // registry resource
                                    return mediationResource;
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while accessing registry objects";
            throw new APIManagementException(msg, e, ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

}