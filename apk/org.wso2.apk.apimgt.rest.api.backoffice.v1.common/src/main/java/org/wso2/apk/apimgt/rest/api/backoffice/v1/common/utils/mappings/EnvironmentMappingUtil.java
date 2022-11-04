/*
 *
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings;

import org.wso2.apk.apimgt.api.model.AsyncProtocolEndpoint;
import org.wso2.apk.apimgt.api.model.Environment;
import org.wso2.apk.apimgt.api.model.VHost;
import org.wso2.apk.apimgt.impl.ExternalEnvironment;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.AdditionalPropertyDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.EnvironmentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.GatewayEnvironmentProtocolURIDTO;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class used to map Environment DTO to model.
 */
public class EnvironmentMappingUtil {

    /**
     * Converts an Environment object into EnvironmentDTO.
     *
     * @param environment Environment object
     * @return EnvironmentDTO object corresponding to the given Environment object
     */
    public static EnvironmentDTO fromEnvironmentToDTO(Environment environment) {

        EnvironmentDTO environmentDTO = new EnvironmentDTO();
        environmentDTO.setId(environment.getUuid());
        environmentDTO.setName(environment.getName());
        environmentDTO.setDisplayName(environment.getDisplayName());
        environmentDTO.setType(environment.getType());
        environmentDTO.setServerUrl(environment.getServerURL());
        environmentDTO.setShowInApiConsole(environment.isShowInConsole());
        environmentDTO.setProvider(environment.getProvider());

        environmentDTO.setAdditionalProperties(fromAdditionalPropertiesToAdditionalPropertiesDTO
                (environment.getAdditionalProperties()));

        ExternalEnvironment parser = APIUtil.getExternalEnvironment(environment.getProvider());
        if (parser != null) {
            List<GatewayEnvironmentProtocolURIDTO> endpointsList = new ArrayList<>();
            List<AsyncProtocolEndpoint> endpointUrlsList = parser.getExternalEndpointURLs(environment);
            if (endpointUrlsList != null) {
                for (AsyncProtocolEndpoint asyncProtocolEndpoint : endpointUrlsList) {
                    GatewayEnvironmentProtocolURIDTO gatewayEnvironmentProtocolURIDTO =
                            new GatewayEnvironmentProtocolURIDTO();
                    gatewayEnvironmentProtocolURIDTO.setProtocol(asyncProtocolEndpoint.getProtocol());
                    gatewayEnvironmentProtocolURIDTO.setEndpointURI(asyncProtocolEndpoint.getProtocolUrl());
                    endpointsList.add(gatewayEnvironmentProtocolURIDTO);
                }
                environmentDTO.setEndpointURIs(endpointsList);
            }
        }
        return environmentDTO;
    }

//    /**
//     * Converts a List object of SubscribedAPIs into a DTO.
//     *
//     * @param environmentCollection a collection of Environment objects
//     * @return EnvironmentListDTO object containing EnvironmentDTOs
//     */
//    public static EnvironmentListDTO fromEnvironmentCollectionToDTO(Collection<Environment> environmentCollection) {
//
//        EnvironmentListDTO environmentListDTO = new EnvironmentListDTO();
//        List<EnvironmentDTO> environmentDTOs = environmentListDTO.getList();
//        if (environmentDTOs == null) {
//            environmentDTOs = new ArrayList<>();
//            environmentListDTO.setList(environmentDTOs);
//        }
//
//        for (Environment environment : environmentCollection) {
//            environmentDTOs.add(fromEnvironmentToDTO(environment));
//        }
//        environmentListDTO.setCount(environmentDTOs.size());
//        return environmentListDTO;
//    }


    /**
     * Converts AdditionalProperties into a AdditionalPropertiesDTO.
     *
     * @param additionalProperties Set of additional properties
     * @return List<AdditionalPropertyDTO>
     */
    public static List<AdditionalPropertyDTO> fromAdditionalPropertiesToAdditionalPropertiesDTO(Map<String,
            String> additionalProperties) {
        List<AdditionalPropertyDTO> additionalPropertyDTOList = new ArrayList<>();
        for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
            AdditionalPropertyDTO additionalPropertyDTO = new AdditionalPropertyDTO();
            additionalPropertyDTO.setKey(entry.getKey());
            additionalPropertyDTO.setValue(entry.getValue());
            additionalPropertyDTOList.add(additionalPropertyDTO);
        }
        return additionalPropertyDTOList;
    }

    /**
     * Check whether given url is a HTTP url.
     *
     * @param url url to check
     * @return true if the given url is HTTP, false otherwise
     */
    private static boolean isHttpURL(String url) {

        return url.matches("^http://.*");
    }

    /**
     * Check whether given url is a HTTPS url.
     *
     * @param url url to check
     * @return true if the given url is HTTPS, false otherwise
     */
    private static boolean isHttpsURL(String url) {

        return url.matches("^https://.*");
    }

    /**
     * Check whether given url is a WS url.
     *
     * @param url url to check
     * @return true if the given url is WS, false otherwise
     */
    private static boolean isWebSocketURL(String url) {

        return url.matches("^ws://.*");
    }

    /**
     * Check whether given url is a WSS url.
     *
     * @param url url to check
     * @return true if the given url is WSS, false otherwise
     */
    private static boolean isSecureWebsocketURL(String url) {

        return url.matches("^wss://.*");
    }

}
