/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.lwm2m.server;

import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static org.thingsboard.server.transport.lwm2m.server.LwM2MTransportHandler.*;

@Slf4j
@Service("LwM2MTransportRequest")
@ConditionalOnExpression("'${service.type:null}'=='tb-transport' || ('${service.type:null}'=='monolith' && '${transport.lwm2m.enabled}'=='true')")
public class LwM2MTransportRequest {

//    @Autowired
//    private LeshanServer lwServer;

    @Autowired
    private LwM2MTransportContextServer context;


    @PostConstruct
    public void init() {
    }

    public Collection<Registration> doGetRegistrations(LeshanServer lwServer) {
        Collection<Registration> registrations = new ArrayList<>();
        for (Iterator<Registration> iterator = lwServer.getRegistrationService().getAllRegistrations(); iterator
                .hasNext(); ) {
            registrations.add(iterator.next());
        }
        return registrations;
    }

    @SneakyThrows
    public LwM2mResponse doGet(LeshanServer lwServer, String clientEndpoint, String target, String typeOper, String contentFormatParam) {
        /** all registered clients */
        Registration registration = lwServer.getRegistrationService().getByEndpoint(clientEndpoint);
        if (registration != null) {
           if (typeOper.equals(GET_TYPE_OPER_DISCOVER)) {
                try {
                    /** create & process request */
                    DiscoverRequest request = new DiscoverRequest(target);
                    return lwServer.send(registration, request, context.getTimeout());
                } catch (RuntimeException | InterruptedException e) {
                    log.error("EndPoint: get client/discover: with id [{}]: [{}]", clientEndpoint, e);
                }
            }
            else if (typeOper.equals(GET_TYPE_OPER_READ)){
               try {
                   /** get content format */
                   ContentFormat contentFormat = contentFormatParam != null ? ContentFormat.fromName(contentFormatParam.toUpperCase()) : null;
                   /** create & process request */
                   ReadRequest request = new ReadRequest(contentFormat, target);
                   return lwServer.send(registration, request, context.getTimeout());
               } catch (RuntimeException | InterruptedException e) {
                   log.error("EndPoint: get client/read: with id [{}]: [{}]", clientEndpoint, e);
               }
           }
        } else {
            log.warn("EndPoint: get: no registered client with id [{}]", clientEndpoint);
        }
        return null;
    }

    @SneakyThrows
    public LwM2mResponse doPost(LeshanServer lwServer, String clientEndpoint, String target, String typeOper, String contentFormatParam, String params) {
        Registration registration = lwServer.getRegistrationService().getByEndpoint(clientEndpoint);
        if (registration != null) {
            /** Execute */
            if (typeOper.equals(POST_TYPE_OPER_EXECUTE)) {
                ExecuteRequest request = new ExecuteRequest(target, params);
                return lwServer.send(registration, request, context.getTimeout());
            }
        }
        return null;
    }

    @SneakyThrows
    public LwM2mResponse doPut(LeshanServer lwServer, String clientEndpoint, String target, String typeOper, String contentFormatParam, String params) {
        Registration registration = lwServer.getRegistrationService().getByEndpoint(clientEndpoint);
        if (registration != null) {
            /** Update */
            if (typeOper.equals(PUT_TYPE_OPER_UPDATE)) {
                ExecuteRequest request = new ExecuteRequest(target, params);
                return lwServer.send(registration, request, context.getTimeout());
            }
            /** Wright */
            else if (typeOper.equals(PUT_TYPE_OPER_WRIGHT)) {

            }
        }
        return null;
    }


//    private LwM2mNode createLwM2mNode(String target, HttpServletRequest req) throws IOException {
//        String contentType = StringUtils.substringBefore(req.getContentType(), ";");
//        if ("application/json".equals(contentType)) {
//            String content = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
//            LwM2mNode node;
//            try {
//                node = gson.fromJson(content, LwM2mNode.class);
//            } catch (JsonSyntaxException e) {
//                throw new InvalidRequestException(e, "unable to parse json to tlv:%s", e.getMessage());
//            }
//            return node;
//        } else if ("text/plain".equals(contentType)) {
//            String content = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
//            int rscId = Integer.valueOf(target.substring(target.lastIndexOf("/") + 1));
//            return LwM2mSingleResource.newStringResource(rscId, content);
//        }
//        throw new InvalidRequestException("content type %s not supported", req.getContentType());
//    }

}
