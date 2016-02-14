package com.eis.ssit.api.v1.dao.sprngmongo;

import com.eis.core.api.v1.dao.ScriptDAO;
import com.eis.core.api.v1.dao.ScriptSecurityPolicyDAO;
import com.eis.core.api.v1.dao.ScriptTypeDAO;
import com.eis.core.api.v1.dao.SiteDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Test that is going to be used to create the Release Sales Order Script Objects.
 *
 * COMMENTED OUT CREATION FOR NOW -TC
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/cantataContext.xml" })
public class TestReleaseSalesOrderScript {

    @Autowired
    ScriptDAO scriptDAO;

    @Autowired
    ScriptTypeDAO scriptTypeDAO;

    @Autowired
    ScriptSecurityPolicyDAO scriptSecurityPolicyDAO;

    @Test
    public void testReleaseSalesOrderScript() throws B2BTransactionFailed, B2BNotFoundException,
            ValidationException {

        Script script = null;
        ScriptType scriptType = null;
        ScriptSecurityPolicy scriptSecurityPolicy = null;
        String refName = "releaseSalesOrder";
        String dataDomain = "com.mycompanyxyz";

        if(scriptTypeDAO.getByRefName(refName, dataDomain) ==  null) {
            scriptType = new ScriptType();
            scriptType.setDataDomain(dataDomain);
            scriptType.setId(UUID.randomUUID().toString());
            scriptType.setRefName(refName);

            DynamicAttribute att = new DynamicAttribute();
            att.setRefName("salesOrderIds");
            att.setType(DynamicAttributeType.Object);
            att.setLabel("salesOrderIds");
            DynamicAttributeSet inputs = new DynamicAttributeSet();
            inputs.getAttributes().put("salesOrderIds", att);
            scriptType.setInputs(inputs);

            ScriptContextObject contextObject = new ScriptContextObject();
            contextObject.setName("siteService");
            contextObject.setType("serviceBean");
            contextObject.setServiceName("siteService");
            scriptType.getScriptContextObjects().put("siteService", contextObject);

            ScriptContextObject contextObject1 = new ScriptContextObject();
            contextObject1.setName("siteClusterService");
            contextObject1.setType("serviceBean");
            contextObject1.setServiceName("siteClusterService");
            scriptType.getScriptContextObjects().put("siteClusterService", contextObject1);

            ScriptContextObject contextObject2 = new ScriptContextObject();
            contextObject2.setName("siteGroupService");
            contextObject2.setType("serviceBean");
            contextObject2.setServiceName("siteGroupService");
            scriptType.getScriptContextObjects().put("siteGroupService", contextObject2);

            ScriptContextObject contextObject3 = new ScriptContextObject();
            contextObject3.setName("salesOrderService");
            contextObject3.setType("serviceBean");
            contextObject3.setServiceName("salesOrderService");
            scriptType.getScriptContextObjects().put("salesOrderService", contextObject3);

            ScriptContextObject contextObject4 = new ScriptContextObject();
            contextObject4.setName("inventoryAllocationEngine");
            contextObject4.setType("javaBean");
            scriptType.getScriptContextObjects().put("inventoryAllocationEngine", contextObject4);

            ScriptContextObject contextObject5 = new ScriptContextObject();
            contextObject5.setName("mailboxService");
            contextObject5.setType("serviceBean");
            contextObject5.setServiceName("mailboxService");
            scriptType.getScriptContextObjects().put("mailboxService", contextObject5);

            ScriptContextObject contextObject6 = new ScriptContextObject();
            contextObject6.setName("mailboxEntryService");
            contextObject6.setType("serviceBean");
            contextObject6.setServiceName("mailboxEntryService");
            scriptType.getScriptContextObjects().put("mailboxEntryService", contextObject6);

            ScriptContextObject contextObject7 = new ScriptContextObject();
            contextObject7.setName("blobStore");
            contextObject7.setType("serviceBean");
            contextObject7.setServiceName("blobStore");
            scriptType.getScriptContextObjects().put("blobStore", contextObject7);

            ScriptContextObject contextObject8 = new ScriptContextObject();
            contextObject8.setName("salesOrderIds");
            contextObject8.setType("scriptInputVariable");
            contextObject8.setServiceName("salesOrderIds");
            scriptType.getScriptContextObjects().put("salesOrderIds", contextObject8);


            ScriptContextObject contextObject9 = new ScriptContextObject();
            contextObject9.setName("inventoryAllocationHelper");
            contextObject9.setType("javaBean");
            scriptType.getScriptContextObjects().put("inventoryAllocationHelper", contextObject9);

            //scriptTypeDAO.save(scriptType);
        }

        if(scriptSecurityPolicyDAO.getByRefName(refName, dataDomain) ==  null) {
            scriptSecurityPolicy = new ScriptSecurityPolicy();
            scriptSecurityPolicy.setId(UUID.randomUUID().toString());
            scriptSecurityPolicy.setRefName(refName);
            scriptSecurityPolicy.getAllowedPackages().put("comeisssitapiv1services",
                    "com.eis.ssit.api.v1.services");
            scriptSecurityPolicy.getAllowedPackages().put("comeisssitapiv1model",
                    "com.eis.ssit.api.v1.model");

            scriptSecurityPolicy.getAllowedPackages().put("comeiscoreapiv1model",
                    "com.eis.core.api.v1.model");
            scriptSecurityPolicy.getAllowedPackages().put("javalang",
                    "java.lang");
            scriptSecurityPolicy.getAllowedPackages().put("javautil",
                    "java.util");
            scriptSecurityPolicy.getAllowedPackages().put("comeisssitapiv1servicesimpl",
                    "com.eis.ssit.api.v1.services.impl");
            scriptSecurityPolicy.getAllowedPackages().put("comeiscoreapiv1service",
                    "com.eis.core.api.v1.service");

            scriptSecurityPolicy.getAllowedPackages().put("comeisssitapiv1allocengine",
                    "com.eis.ssit.api.v1.allocengine");

            scriptSecurityPolicy.getAllowedPackages().put("comfasterxmljacksondatabind",
                    "com.fasterxml.jackson.databind");

            scriptSecurityPolicy.getAllowedPackages().put("comeisutil",
                    "com.eis.util");

            scriptSecurityPolicy.getAllowedPackages().put("orgapachecommonsio",
                    "org.apache.commons.io");

            scriptSecurityPolicy.getAllowedPackages().put("comeisblobstoregridfs",
                    "com.eis.blobstore.gridfs");

            scriptSecurityPolicy.getAllowedPackages().put("comeissecuritymultitenancymodel",
                    "com.eis.security.multitenancy.model");

            scriptSecurityPolicy.getAllowedPackages().put("javaio",
                    "java.io");

            scriptSecurityPolicy.getAllowedPackages().put("comeisssitapiv1daosprngmongo",
                    "com.eis.ssit.api.v1.dao.sprngmongo");

            //scriptSecurityPolicyDAO.save(scriptSecurityPolicy);
        }

        if(scriptDAO.getByRefName(refName, dataDomain) ==  null) {
            script = new Script();
            script.setDataDomain(dataDomain);
            script.setScriptSecurityPolicy(scriptSecurityPolicy);
            script.setScriptSecurityPolicyId(scriptSecurityPolicy.getId());
            script.setType(scriptType);
            script.setScriptTypeId(scriptType.getId());


            script.setScript("// gets Site Clusters - will look to see if there is a default SiteCluster\n" +
                    "// if not will user the first one\n" +
                    "function getClusters() {\n" +
                    "    var searchRequest = new com.eis.core.api.v1.model.DynamicSearchRequest();\n" +
                    "    var list = new java.util.ArrayList();\n" +
                    "    list.add(\"id\");\n" +
                    "    searchRequest.setExactMatches(true);\n" +
                    "    \n" +
                    "    var da = new com.eis.core.api.v1.model.DynamicAttribute();\n" +
                    "    da.setType(com.eis.core.api.v1.model.DynamicAttributeType.Boolean);\n" +
                    "    da.setRefName(\"isDefault\");\n" +
                    "    da.setValue(true);\n" +
                    "    searchRequest.getSearchFields().getAttributes().put(\"isDefault\",da);\n" +
                    "    \n" +
                    "    var clusters = siteClusterService.getList(searchRequest, 0, 25, list);\n" +
                    "    if(clusters.size() === 0) {\n" +
                    "       clusters = siteClusterService.getList(0, 25, list, null); \n" +
                    "    }\n" +
                    "    return clusters;\n" +
                    "}\n" +
                    "\n" +
                    "// gets SiteGroups from clusters and returns a list of SiteGroups\n" +
                    "function getSiteGroups(clusters) {\n" +
                    "    var groupSearchRequest = new com.eis.core.api.v1.model.DynamicSearchRequest(); \n" +
                    "    var list = new java.util.ArrayList();\n" +
                    "    list.add(\"id\");\n" +
                    "    groupSearchRequest.setExactMatches(true);\n" +
                    "    \n" +
                    "    var cluster = clusters.get(0);\n" +
                    "    java.lang.System.out.println(\"Cluster:\"+cluster.getRefName());\n" +
                    "    var da2 = new com.eis.core.api.v1.model.DynamicAttribute();\n" +
                    "    da2.setType(com.eis.core.api.v1.model.DynamicAttributeType.String);\n" +
                    "    da2.setRefName(\"siteClusterId\");\n" +
                    "    da2.setValue(cluster.getId());\n" +
                    "    groupSearchRequest.getSearchFields().getAttributes().put(\"siteClusterId\",da2);\n" +
                    "    \n" +
                    "    var groups = siteGroupService.getList(groupSearchRequest,0, 25, list);\n" +
                    "    return groups; \n" +
                    "}\n" +
                    "\n" +
                    "// gets Sites from SiteGroups\n" +
                    "function getSites(groups) {\n" +
                    "    var sites = new java.util.ArrayList();\n" +
                    "    \n" +
                    "    for(var i = 0; i < groups.size(); i++) {\n" +
                    "        var group = groups.get(i);\n" +
                    "        java.lang.System.out.println(\"Group:\"+group.getRefName());\n" +
                    "        java.lang.System.out.println(\"Sites:\"+siteService.getSitesForGroupId(group.getId()));\n" +
                    "        var tempSites = siteService.getSitesForGroupId(group.getId());\n" +
                    "        sites.addAll(tempSites);\n" +
                    "    }\n" +
                    "    return sites;\n" +
                    "}\n" +
                    "\n" +
                    "// gets a Sales Order, right now by id - but should come from the camel header\n" +
                    "function getSalesOrder(orderId) {\n" +
                    "    var order = salesOrderService.getById(orderId);  \n" +
                    "    return order;\n" +
                    "}\n" +
                    "\n" +
                    "// gets a mailbox for the specified site Mailbox or will create one called\n" +
                    "// Shipment Requests\n" +
                    "function getMailbox(mailboxRefName) {\n" +
                    "       \n" +
                    "       java.lang.System.out.println(\"Mailbox Ref Name:\"+mailboxRefName);\n" +
                    "       var mailbox = mailboxService.getByRefName(mailboxRefName);\n" +
                    "       if(mailbox === null) {\n" +
                    "            mailbox = new com.eis.core.api.v1.model.Mailbox();\n" +
                    "            mailbox.setRefName(mailboxRefName);\n" +
                    "            mailbox.setId(java.util.UUID.randomUUID().toString());\n" +
                    "            mailbox.setMailboxRole(com.eis.core.api.v1.model.MailboxRole.INTERMEDIATE);\n" +
                    "            mailbox.setPinned(true);\n" +
                    "            mailbox.setSystemMailbox(false);\n" +
                    "            mailbox = mailboxService.save(mailbox);\n" +
                    "       }\n" +
                    "       return mailbox;\n" +
                    "        \n" +
                    "}\n" +
                    "\n" +
                    "// creates a mailbox entry\n" +
                    "function createMailboxEntry(mailbox,order,shipmentRequest) {\n" +
                    "    var me = new com.eis.core.api.v1.model.MailboxEntry();\n" +
                    "    me.setId(java.util.UUID.randomUUID().toString());\n" +
                    "    me.setRefName(java.util.UUID.randomUUID().toString());\n" +
                    "    \n" +
                    "    me.setFromUserId(\"orders@mycompanyxyz.com\");\n" +
                    "    me.setToUserId(\"shipping@mycompanyxyz.com\");\n" +
                    "    me.setSubject(\"Shipment Request for Order:\"+order.getHeader().getOrderNumber());\n" +
                    "    me.setMailboxId(mailbox.getId());\n" +
                    "    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();\n" +
                    "    mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);\n" +
                    "        \n" +
                    "    var json = mapper.writeValueAsString(shipmentRequest);\n" +
                    "    var fileName = \"shipmentrequest.json\";\n" +
                    "    var time = new java.util.Date();\n" +
                    "    var path = mailbox.getDataDomain().replace(\".\", \"/\") + \"/\" + mailbox.getRefName() + \"/\" +fileName+\"-\" + time;\n" +
                    "    \n" +
                    "    metaData = blobStore.createMetaDataObject();\n" +
                    "    metaData.setDataDomain(mailbox.getDataDomain());\n" +
                    "    \n" +
                    "    metaData.setPathString(path);\n" +
                    "    metaData.setTxId(com.eis.security.multitenancy.model.SecureSession.getTxId());\n" +
                    "    \n" +
                    "    refName = fileName;\n" +
                    "   \n" +
                    "    var blob = blobStore.createBlobFromStream(refName, org.apache.commons.io.IOUtils.toInputStream(json), \"application/json\", metaData);\n" +
                    "        \n" +
                    "    var att = new com.eis.core.api.v1.model.Attachment();\n" +
                    "    att.setInlinePayload(false);\n" +
                    "    fileId = blob.getIdAsString();\n" +
                    "    att.setPayloadId(fileId);\n" +
                    "    att.setFileSize(blob.getSize());\n" +
                    "    att.setDataDomain(mailbox.getDataDomain());\n" +
                    "    att.setRefName(me.getRefName());\n" +
                    "    att.setFileName(fileName);\n" +
                    "    att.setContentType(\"application/json\");\n" +
                    "    me.getAttachments().add(att);\n" +
                    "    me = mailboxEntryService.save(me);    \n" +
                    "    return me;    \n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "var allShipmentRequests= new java.util.LinkedHashMap();    \n" +
                    "var clusters = getClusters();\n" +
                    "if(clusters.size() > 0) {\n" +
                    "    var groups = getSiteGroups(clusters);\n" +
                    "    var sites = getSites(groups);\n" +
                    "    for(var x = 0; x < salesOrderIds.size(); x++) {\n" +
                    "        var orderId = salesOrderIds.get(x);\n" +
                    "        var order = getSalesOrder(orderId);\n" +
                    "        var salesOrderReleaseLog = inventoryAllocationHelper\n" +
                    "                .initSalesOrderReleaseLog(order);\n" +
                    "        var updatedSalesOrder = inventoryAllocationEngine.allocateInventoryV2(order, sites, 2, salesOrderReleaseLog);\n" +
                    "        inventoryAllocationHelper.closeLog(salesOrderReleaseLog);\n" +
                    "        \n" +
                    "        var shipmentRequests = updatedSalesOrder.getShipmentRequests();\n" +
                    "        java.lang.System.out.println(\"Shipment Request Size:\"+shipmentRequests.size());\n" +
                    "        for(var i = 0; i < shipmentRequests.size(); i++) {\n" +
                    "           var shipmentRequest = shipmentRequests.get(i);\n" +
                    "           var mailboxRefName = \"Shipment Requests\";\n" +
                    "           if(shipmentRequest.getHeader().getMailboxRefName() !== null) {\n" +
                    "              mailboxRefName = shipmentRequest.getHeader().getMailboxRefName(); \n" +
                    "           }\n" +
                    "           \n" +
                    "           var mailbox = getMailbox(mailboxRefName);\n" +
                    "           createMailboxEntry(mailbox,order,shipmentRequest);\n" +
                    "           \n" +
                    "        }\n" +
                    "        \n" +
                    "        allShipmentRequests.put(orderId, shipmentRequests);\n" +
                    "    }\n" +
                    "    allShipmentRequests;\n" +
                    "}\n" +
                    "\n");
            script.setRefName(refName);
            script.setFunctionType("salesOrderRelease");
            script.setActive(true);
            //scriptDAO.save(script);
        }
    }
}
